import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/user_model.dart';
import 'package:flutter/material.dart';

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
} 