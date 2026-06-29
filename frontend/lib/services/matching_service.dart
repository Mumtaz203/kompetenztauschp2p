import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user/user_model.dart';
import 'package:flutter/material.dart';
import '../models/matching/discover_user_model.dart';

class MatchingService {
  static const String baseUrl = 'http://10.0.2.2:8081';


  Future<List<UserModel>> searchUsersBySkill(String skill) async {
    String normalizedSkill = skill.trim();
    if (normalizedSkill.length < 3) {
      throw Exception('Search query must be at least 3 characters long. Please provide a valid skill.');
    }

    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token') ?? '';

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/users/search?skill=$normalizedSkill'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => UserModel.fromJson(json)).toList();
      } else if (response.statusCode == 400) {
        throw Exception('Unvalid search query. Please provide a valid skill.');
      } else {
        throw Exception('Server failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection failed: $e');
    }
  }

  Future<List<DiscoverUserModel>> discoverUsers(String userId) async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token') ?? '';

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/users/$userId/discover'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => DiscoverUserModel.fromJson(json)).toList();
      } else {
        throw Exception('Discover failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection failed: $e');
    }
  }

  Future<List<UserModel>> getRandom10Users() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('jwt_token') ?? '';

    try {
      final response = await http.get(
        Uri.parse('$baseUrl/users/getRandom10Users'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final decodedData = jsonDecode(response.body);
        List<dynamic> usersList = [];

        if (decodedData is Map && decodedData.containsKey('users')) {
          usersList = decodedData['users'];
        } else if (decodedData is List) {
          usersList = decodedData;
        }

        return usersList.map((json) => UserModel.fromJson(json)).toList();
      } else {
        throw Exception('Failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection failed: $e');
    }
  }
} 