import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class Session {
  static const String _jwtTokenKey = 'jwt_token';
  static const String _userIdKey = 'my_user_id';
  static String? token;
  static String? currentUserId;

  static Future<void> setToken(String value) async {
    token = value;
    currentUserId = _readSubjectFromJwt(value) ?? currentUserId;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_jwtTokenKey, value);
    if (currentUserId != null && currentUserId!.isNotEmpty) {
      await prefs.setString(_userIdKey, currentUserId!);
    }
  }

  static Future<void> setCurrentUserId(String value) async {
    currentUserId = value;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userIdKey, value);
  }

  static Future<String?> getToken() async {
    if (token != null && token!.isNotEmpty) {
      return token;
    }
    final prefs = await SharedPreferences.getInstance();
    token = prefs.getString(_jwtTokenKey);
    currentUserId ??= prefs.getString(_userIdKey);
    return token;
  }

  static Future<String?> getCurrentUserId() async {
    if (currentUserId != null && currentUserId!.isNotEmpty) {
      return currentUserId;
    }
    final prefs = await SharedPreferences.getInstance();
    currentUserId = prefs.getString(_userIdKey);
    token ??= prefs.getString(_jwtTokenKey);
    return currentUserId;
  }

  static Future<void> restore() async {
    final prefs = await SharedPreferences.getInstance();
    token = prefs.getString(_jwtTokenKey);
    currentUserId =
        prefs.getString(_userIdKey) ?? _readSubjectFromJwt(token ?? '');
  }

  static Future<void> clear() async {
    token = null;
    currentUserId = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_jwtTokenKey);
    await prefs.remove(_userIdKey);
  }

  static String? _readSubjectFromJwt(String value) {
    final parts = value.split('.');
    if (parts.length != 3) return null;

    try {
      final payload = utf8.decode(
        base64Url.decode(base64Url.normalize(parts[1])),
      );
      final data = jsonDecode(payload);
      return data['sub']?.toString();
    } catch (_) {
      return null;
    }
  }
}
