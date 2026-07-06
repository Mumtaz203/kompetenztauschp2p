import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/session/session_model.dart';
import 'auth_service.dart';

class SessionService {
  static String baseUrl = AuthService.baseUrl;

  Future<Map<String, String>> _headers() async {
    final token = await AuthService.getStoredToken();
    return {
      'Content-Type': 'application/json',
      if (token != null && token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  Future<SessionModel> getSessionByMatchRequestId(String matchRequestId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/sessions/by-match-request/$matchRequestId'),
        headers: await _headers(),
      );

      if (response.statusCode == 200) {
        return SessionModel.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Session failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection error: $e');
    }
  }

  Future<void> submitCompletionResponse({
    required String sessionId,
    required String answer,
    String? reason,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/sessions/$sessionId/completion-response'),
        headers: await _headers(),
        body: jsonEncode({
          'answer': answer,
          if (reason != null) 'reason': reason,
        }),
      );

      if (response.statusCode != 200) {
        throw Exception('Failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection Error: $e');
    }
  }

  Future<void> createPrivateReport({
    required String sessionId,
    required String reportedUserId,
    required String reasonCode,
    String? description,
  }) async {
    try {
      final trimmedDescription = description?.trim();
      final response = await http.post(
        Uri.parse('$baseUrl/sessions/$sessionId/private-reports'),
        headers: await _headers(),
        body: jsonEncode({
          'reportedUserId': reportedUserId,
          'reasonCode': reasonCode,
          if (trimmedDescription != null && trimmedDescription.isNotEmpty)
            'description': trimmedDescription,
        }),
      );

      if (response.statusCode != 201) {
        final message = _extractErrorMessage(response.body);
        throw Exception(message ?? 'Report failed: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception(e.toString().replaceAll('Exception: ', ''));
    }
  }

  Future<bool> hasPrivateReport({
    required String sessionId,
    required String reportedUserId,
  }) async {
    try {
      final response = await http.get(
        Uri.parse(
          '$baseUrl/sessions/$sessionId/private-reports/me/reported-users/$reportedUserId',
        ),
        headers: await _headers(),
      );

      if (response.statusCode == 200) {
        final decoded = jsonDecode(response.body);
        if (decoded is Map<String, dynamic>) {
          return decoded['reported'] == true;
        }
      }

      throw Exception('Report status failed: ${response.statusCode}');
    } catch (e) {
      throw Exception(e.toString().replaceAll('Exception: ', ''));
    }
  }

  String? _extractErrorMessage(String body) {
    if (body.isEmpty) return null;

    try {
      final decoded = jsonDecode(body);
      if (decoded is Map<String, dynamic>) {
        return decoded['message']?.toString() ??
            decoded['error']?.toString() ??
            decoded['details']?.toString();
      }
    } catch (_) {
      return body;
    }

    return null;
  }
}
