import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user_model.dart';
import 'auth_service.dart';

class UserService {
  Future<Map<String, String>> _authorizedHeaders() async {
    final token = await AuthService.getStoredToken() ?? '';
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  Future<UserModel> getMyProfile() async {
    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/auth/me'),
      headers: await _authorizedHeaders(),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    throw Exception(response.body);
  }

  Future<UserModel> getUserProfileById({required String userId}) async {
    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/users/getUser/$userId'),
      headers: await _authorizedHeaders(),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    throw Exception(response.body);
  }

  Future<List<UserModel>> searchUsersBySkill({required String skill}) async {
    final response = await http.get(
      Uri.parse(
        '${AuthService.baseUrl}/users/search',
      ).replace(queryParameters: {'skill': skill}),
      headers: await _authorizedHeaders(),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body) as List<dynamic>;
      return data
          .map((item) => UserModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }

    throw Exception(response.body);
  }

  Future<UserModel> updateMyProfile({
    required String userId,
    required String username,
    required List<String> offeredSkills,
    required List<String> wantedSkills,
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/updateUser/$userId'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'username': username,
        'offeredSkills': offeredSkills,
        'wantedSkills': wantedSkills,
      }),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    throw Exception(response.body);
  }
}