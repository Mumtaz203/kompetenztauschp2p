import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/auth_response_model.dart';
import '../services/auth_service.dart';
import 'service_providers.dart';

class AuthState {
  final bool isAuthenticated;
  final String? role;
  final bool isLoading;
  final String? error;

  AuthState({
    this.isAuthenticated = false,
    this.role,
    this.isLoading = false,
    this.error,
  });

  AuthState copyWith({
    bool? isAuthenticated,
    String? role,
    bool? isLoading,
    String? error,
  }) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      role: role ?? this.role,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class AuthNotifier extends Notifier<AuthState> {

  @override
  AuthState build() {
    _checkInitialAuth();
    return AuthState();
  }

  Future<void> _checkInitialAuth() async {
    final token = await AuthService.getStoredToken();
    final role = await AuthService.getStoredUserRole();
    if (token != null && token.isNotEmpty) {
      state = state.copyWith(isAuthenticated: true, role: role);
    }
  }

  Future<AuthResponseModel?> login(String email, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final authService = ref.read(authServiceProvider);
      final response = await authService.login(email: email, password: password);

      state = state.copyWith(
        isAuthenticated: true,
        role: response.role,
        isLoading: false,
      );
      return response;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: e.toString().replaceAll('Exception: ', ''),
      );
      throw e;
    }
  }

  Future<void> logout() async {
    await ref.read(authServiceProvider).logout();
    state = AuthState();
  }
}

final authProvider = NotifierProvider<AuthNotifier, AuthState>(() {
  return AuthNotifier();
});