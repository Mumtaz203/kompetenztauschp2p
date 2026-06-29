import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../models/user/user_model.dart';
import 'auth_service.dart';

class UserService {
  Future<Map<String, String>> _authorizedHeaders() async {
    final token = await AuthService.getStoredToken() ?? '';

    return {
      'Content-Type': 'application/json',
      if (token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  Future<UserModel> getMyProfile() async {
    final userId = await AuthService.getStoredUserId();

    if (userId == null || userId.isEmpty) {
      throw Exception('No stored user id found. Please login again.');
    }

    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/users/getUser/$userId'),
      headers: await _authorizedHeaders(),
    );

    debugPrint('GET MY PROFILE USER ID: $userId');
    debugPrint('GET MY PROFILE STATUS: ${response.statusCode}');
    debugPrint('GET MY PROFILE BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'Empty response from server';
    throw Exception('HTTP ${response.statusCode}: $errorMsg');
  }

  Future<UserModel> getUserProfileById({required String userId}) async {
    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/users/getUser/$userId'),
      headers: await _authorizedHeaders(),
    );

    debugPrint('GET USER ID: $userId');
    debugPrint('GET USER STATUS: ${response.statusCode}');
    debugPrint('GET USER BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'Empty response from server';
    throw Exception('HTTP ${response.statusCode}: $errorMsg');
  }
  Future<List<UserModel>> getRandom10Users() async {
    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/users/getRandom10Users'),
      headers: await _authorizedHeaders(),
    );

    debugPrint('GET RANDOM USERS STATUS: ${response.statusCode}');
    debugPrint('GET RANDOM USERS BODY: ${response.body}');

    if (response.statusCode == 200) {
      final decodedData = jsonDecode(response.body);
      final rawUsers =
      decodedData is Map && decodedData.containsKey('users')
          ? decodedData['users']
          : decodedData;

      if (rawUsers is List) {
        return rawUsers
            .map((item) => UserModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }

      return [];
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'Empty response from server';
    throw Exception('HTTP ${response.statusCode}: $errorMsg');
  }

  Future<List<UserModel>> searchUsersBySkill({required String skill}) async {
    final normalizedSkill = skill.trim();

    if (normalizedSkill.length < 3) {
      throw Exception('Search query must be at least 3 characters long.');
    }

    final response = await http.get(
      Uri.parse('${AuthService.baseUrl}/users/search')
          .replace(queryParameters: {'skill': normalizedSkill}),
      headers: await _authorizedHeaders(),
    );

    debugPrint('SEARCH USERS STATUS: ${response.statusCode}');
    debugPrint('SEARCH USERS BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body) as List<dynamic>;

      return data
          .map((item) => UserModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'Empty response from server';
    throw Exception('HTTP ${response.statusCode}: $errorMsg');
  }

  Future<UserModel> updateMyName({
    required String userId,
    required String name,
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/$userId/updateName'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'name': name,
      }),
    );

    debugPrint('UPDATE NAME STATUS: ${response.statusCode}');
    debugPrint('UPDATE NAME BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'No details provided by backend.';
    throw Exception('HTTP ${response.statusCode} - $errorMsg');
  }

  Future<UserModel> updateMySkills({
    required String userId,
    required List<String> offeredSkills,
    required List<String> wantedSkills,
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/$userId/updateSkills'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'offeredSkills': offeredSkills,
        'wantedSkills': wantedSkills,
      }),
    );

    debugPrint('UPDATE SKILLS STATUS: ${response.statusCode}');
    debugPrint('UPDATE SKILLS BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'No details provided by backend.';
    throw Exception('HTTP ${response.statusCode} - $errorMsg');
  }

  Future<UserModel> updateMyUniversity({
    required String userId,
    required String university,
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/$userId/updateUni'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'university': university,
      }),
    );

    debugPrint('UPDATE UNIVERSITY STATUS: ${response.statusCode}');
    debugPrint('UPDATE UNIVERSITY BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'No details provided by backend.';
    throw Exception('HTTP ${response.statusCode} - $errorMsg');
  }

  Future<UserModel> updateMyProfile({
    required String userId,
    required String username,
    required List<String> offeredSkills,
    required List<String> wantedSkills,
    required String university,
    String profileImageUrl = '',
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/$userId/updateUser'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'username': username,
        'offeredSkills': offeredSkills,
        'wantedSkills': wantedSkills,
        'profileImageUrl': profileImageUrl,
        'university': university,
      }),
    );

    debugPrint('UPDATE PROFILE STATUS: ${response.statusCode}');
    debugPrint('UPDATE PROFILE BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'No details provided by backend.';
    throw Exception('HTTP ${response.statusCode} - $errorMsg');
  }

  Future<UserModel> updateProfileImageUrl({
    required String userId,
    required String profileImageUrl,
  }) async {
    final response = await http.put(
      Uri.parse('${AuthService.baseUrl}/users/$userId/updateProfileImage'),
      headers: await _authorizedHeaders(),
      body: jsonEncode({
        'profileImageUrl': profileImageUrl,
      }),
    );

    debugPrint('UPDATE PROFILE IMAGE STATUS: ${response.statusCode}');
    debugPrint('UPDATE PROFILE IMAGE BODY: ${response.body}');

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return UserModel.fromJson(data);
    }

    final errorMsg =
    response.body.isNotEmpty ? response.body : 'No details provided by backend.';
    throw Exception('HTTP ${response.statusCode} - $errorMsg');
  }
}