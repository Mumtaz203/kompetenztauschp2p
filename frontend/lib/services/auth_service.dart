import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:jwt_decoder/jwt_decoder.dart';
import '../models/auth/auth_response_model.dart';
import 'package:flutter/foundation.dart';

class AuthService {
  static String get baseUrl {
    if (kIsWeb) {
      return 'http://localhost:8080';  // Docker port
    }
    return 'http://10.0.2.2:8081';   // Android emulator
  }

  static const String _jwtTokenKey = 'jwt_token';
  static const String _userIdKey = 'my_user_id';
  static const String _userRoleKey = 'my_user_role';

  Future<AuthResponseModel> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'password': password}),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = jsonDecode(response.body);
        final authResponse = AuthResponseModel.fromJson(data);

        final prefs = await SharedPreferences.getInstance();

        await prefs.setString(_jwtTokenKey, authResponse.token);
        await prefs.setString(_userRoleKey, authResponse.role);

        Map<String, dynamic> decodedToken = JwtDecoder.decode(
          authResponse.token,
        );

        String myId =
            decodedToken['sub']?.toString() ??
                decodedToken['id']?.toString() ??
                decodedToken['upn']?.toString() ??
                '';

        await prefs.setString(_userIdKey, myId);

        return authResponse;
      } else if (response.statusCode == 401 || response.statusCode == 403) {
        throw Exception('Email or password is incorrect. Please try again.');
      } else {
        throw Exception(
          'Failed to login. Server responded with status code: ${response.statusCode}',
        );
      }
    } catch (e) {
      throw Exception(e.toString());
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_jwtTokenKey);
    await prefs.remove(_userIdKey);
    await prefs.remove(_userRoleKey);
  }

  static Future<String?> getStoredToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_jwtTokenKey);
  }

  static Future<String?> getStoredUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_userIdKey);
  }

  static Future<String?> getStoredUserRole() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_userRoleKey);
  }

  Future<void> register({
    required String username,
    required String email,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'username': username,
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception(response.body);
    }
  }
}