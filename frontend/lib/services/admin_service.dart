import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/user_model.dart';
import '../models/message_model.dart';
import 'auth_service.dart';

class AdminService {
  static const String baseUrl = AuthService.baseUrl;


  Future<List<UserModel>> getAllUsers() async {
    final token = await AuthService.getStoredToken();

    final response = await http.get(
      Uri.parse('$baseUrl/users/getAllUsers'),
      headers: {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      final rawUsers = data is Map && data.containsKey('users') ? data['users'] : data;

      if (rawUsers is List) {
        return rawUsers.map((user) => UserModel.fromJson(user)).toList();
      }
      return [];
    }
    if (response.statusCode == 401) throw Exception('Unauthorized. Please login again.');
    if (response.statusCode == 403) throw Exception('You are not allowed to view users.');

    throw Exception('Failed to load users. Status code: ${response.statusCode}');
  }

  Future<void> deleteUser(String userId) async {
    final token = await AuthService.getStoredToken();

    final response = await http.delete(
      Uri.parse('$baseUrl/users/deleteUser/$userId'),
      headers: {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200 || response.statusCode == 204) return;
    if (response.statusCode == 401) throw Exception('Unauthorized. Please login again.');
    if (response.statusCode == 403) throw Exception('You are not allowed to delete users.');

    throw Exception('Failed to delete user. Status code: ${response.statusCode}');
  }


  Future<List<MessageModel>> getAllMessages() async {
    final token = await AuthService.getStoredToken();
    final headers = {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    final users = await getAllUsers();
    List<MessageModel> allMessages = [];


    for (var user in users) {
      try {
        final response = await http.get(
          Uri.parse('$baseUrl/messages/messagesFromUser/${user.id}'),
          headers: headers,
        );

        if (response.statusCode == 200) {
          final List<dynamic> data = jsonDecode(response.body);
          allMessages.addAll(data.map((msg) => MessageModel.fromJson(msg)));
        }
      } catch (e) {
        continue;
      }
    }

    final uniqueMessages = {for (var msg in allMessages) msg.id: msg}.values.toList();

    uniqueMessages.sort((a, b) => (b.sentAt ?? DateTime.now()).compareTo(a.sentAt ?? DateTime.now()));

    return uniqueMessages;
  }

  Future<void> deleteMessage(String messageId) async {
    final token = await AuthService.getStoredToken();

    final response = await http.delete(
      Uri.parse('$baseUrl/messages/deleteMessage/$messageId'),
      headers: {
        'Content-Type': 'application/json',
        if (token != null) 'Authorization': 'Bearer $token',
      },
    );

    if (response.statusCode == 200 || response.statusCode == 204) return;
    if (response.statusCode == 401) throw Exception('Unauthorized. Please login again.');
    if (response.statusCode == 403) throw Exception('You are not allowed to delete messages.');

    throw Exception('Failed to delete message. Status code: ${response.statusCode}');
  }
}