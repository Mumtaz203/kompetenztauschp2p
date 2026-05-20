import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/message_model.dart';
import 'auth_service.dart';

class ChatService {
  static const String baseUrl = AuthService.baseUrl;

  Future<Map<String, String>> get _headers async {
    final headers = {'Content-Type': 'application/json'};
    final token = await AuthService.getStoredToken();
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }

  Future<Map<String, dynamic>> getConversationDetails(
    String conversationId,
  ) async {
    final response = await http.get(
      Uri.parse('$baseUrl/conversations/$conversationId/details'),
      headers: await _headers,
    );

    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    }

    throw Exception(response.body);
  }

  Future<Map<String, dynamic>> createConversation({
    required String currentUserId,
    required String otherUserId,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/conversations'),
      headers: await _headers,
      body: jsonEncode({
        'currentUserId': currentUserId,
        'otherUserId': otherUserId,
      }),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return jsonDecode(response.body);
    }

    throw Exception(response.body);
  }

  Future<MessageModel> sendMessage({
    required String conversationId,
    required String senderId,
    required String recipientId,
    required String content,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/messages'),
      headers: await _headers,
      body: jsonEncode({
        'conversationId': conversationId,
        'senderId': senderId,
        'recipientId': recipientId,
        'content': content,
      }),
    );

    if (response.statusCode == 201) {
      return MessageModel.fromJson(jsonDecode(response.body));
    }

    throw Exception(response.body);
  }
}
