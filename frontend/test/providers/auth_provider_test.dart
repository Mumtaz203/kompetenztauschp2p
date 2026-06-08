import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:frontend/services/auth_service.dart';
import 'package:frontend/providers/service_providers.dart';

void main() {
  const jwtTokenKey = 'jwt_token';
  const userIdKey = 'my_user_id';
  const userRoleKey = 'my_user_role';

  setUp(() {
    SharedPreferences.setMockInitialValues({
      jwtTokenKey: 'fake_jwt_token_12345',
      userIdKey: 'user_123',
      userRoleKey: 'USER',
    });
  });

  test('authServiceProvider successfully provides an instance of AuthService', () {
    final container = ProviderContainer();
    addTearDown(container.dispose);

    final authService = container.read(authServiceProvider);

    expect(authService, isA<AuthService>());
  });

  test('AuthService static methods retrieve stored data correctly', () async {
    final token = await AuthService.getStoredToken();
    final userId = await AuthService.getStoredUserId();
    final role = await AuthService.getStoredUserRole();

    expect(token, 'fake_jwt_token_12345');
    expect(userId, 'user_123');
    expect(role, 'USER');
  });

  test('logout method clears all auth related keys from SharedPreferences', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final authService = container.read(authServiceProvider);

    await authService.logout();

    final prefs = await SharedPreferences.getInstance();

    expect(prefs.getString(jwtTokenKey), isNull);
    expect(prefs.getString(userIdKey), isNull);
    expect(prefs.getString(userRoleKey), isNull);
  });
}