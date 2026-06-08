import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/match_request_model.dart';
import 'auth_service.dart';

class MatchRequestService {
  static const String baseUrl = AuthService.baseUrl;

  Future<Map<String, String>> _headers() async {
    final token = await AuthService.getStoredToken();
    return {
      'Content-Type': 'application/json',
      if (token != null && token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  //sending request
  Future<MatchRequestModel> sendRequest(String senderId, String receiverId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/match-requests/send/senderId/$senderId/receiverId/$receiverId'),
      headers: await _headers(),
    );

    if (response.statusCode == 201) {
      return MatchRequestModel.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to send request: ${response.body}');
  }

  // Incoming Requests
  Future<List<MatchRequestModel>> getIncomingRequests(String userId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/match-requests/incoming/$userId'),
      headers: await _headers(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => MatchRequestModel.fromJson(json)).toList();
    }
    throw Exception('Failed to load incoming requests');
  }

  // Outgoing Requests
  Future<List<MatchRequestModel>> getOutgoingRequests(String userId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/match-requests/outgoing/$userId'),
      headers: await _headers(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => MatchRequestModel.fromJson(json)).toList();
    }
    throw Exception('Failed to load outgoing requests');
  }

  // Accepting Request
  Future<void> acceptRequest(String requestId, String actingUserId) async {
    final response = await http.patch(
      Uri.parse('$baseUrl/match-requests/$requestId/accept/$actingUserId'),
      headers: await _headers(),
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to accept request: ${response.body}');
    }
  }

  // Rejecting Request
  Future<void> rejectRequest(String requestId, String actingUserId) async {
    final response = await http.patch(
      Uri.parse('$baseUrl/match-requests/$requestId/reject/$actingUserId'),
      headers: await _headers(),
    );

    if (response.statusCode != 204) {
      throw Exception('Failed to reject request: ${response.body}');
    }
  }

  // All Accepted Requests (Matches)
  Future<List<MatchRequestModel>> getMatches(String userId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/match-requests/matches/$userId'),
      headers: await _headers(),
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => MatchRequestModel.fromJson(json)).toList();
    }
    throw Exception('Failed to load matches');
  }
}