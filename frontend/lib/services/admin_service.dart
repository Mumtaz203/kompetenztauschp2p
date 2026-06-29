import 'dart:convert';
import 'package:http/http.dart' as http;

import '../models/user_model.dart';
import '../models/message_model.dart';
import '../models/conversation_model.dart';
import '../models/match_request_model.dart';
import 'auth_service.dart';

class AdminService {
  static const String baseUrl = AuthService.baseUrl;

  Map<String, String> _headers(String? token) {
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  void _handleError(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) return;
    if (response.statusCode == 401) throw Exception('Unauthorized. Please login again.');
    if (response.statusCode == 403) throw Exception('You are not allowed to perform this action.');
    if (response.statusCode == 404) throw Exception('Resource not found.');
    throw Exception('Request failed. Status code: ${response.statusCode}. ${response.body}');
  }

  Future<http.Response> _get(String path) async {
    final token = await AuthService.getStoredToken();
    final response = await http.get(Uri.parse('$baseUrl$path'), headers: _headers(token));
    _handleError(response);
    return response;
  }

  Future<http.Response> _post(String path, Map<String, dynamic> body) async {
    final token = await AuthService.getStoredToken();
    final response = await http.post(
      Uri.parse('$baseUrl$path'),
      headers: _headers(token),
      body: jsonEncode(body),
    );
    _handleError(response);
    return response;
  }

  Future<http.Response> _put(String path, Map<String, dynamic> body) async {
    final token = await AuthService.getStoredToken();
    final response = await http.put(
      Uri.parse('$baseUrl$path'),
      headers: _headers(token),
      body: jsonEncode(body),
    );
    _handleError(response);
    return response;
  }

  Future<http.Response> _patch(String path, {Map<String, dynamic>? body}) async {
    final token = await AuthService.getStoredToken();
    final response = await http.patch(
      Uri.parse('$baseUrl$path'),
      headers: _headers(token),
      body: body == null ? null : jsonEncode(body),
    );
    _handleError(response);
    return response;
  }

  Future<http.Response> _delete(String path) async {
    final token = await AuthService.getStoredToken();
    final response = await http.delete(Uri.parse('$baseUrl$path'), headers: _headers(token));
    _handleError(response);
    return response;
  }

  Future<List<UserModel>> getAllUsers() async {
    final response = await _get('/users/getAllUsers');

    print('ADMIN GET ALL USERS STATUS: ${response.statusCode}');
    print('ADMIN GET ALL USERS BODY: ${response.body}');

    final data = jsonDecode(response.body);
    final rawUsers =
    data is Map && data.containsKey('users') ? data['users'] : data;

    if (rawUsers is List) {
      return rawUsers
          .map((user) => UserModel.fromJson(user as Map<String, dynamic>))
          .toList();
    }

    return [];
  }

  Future<void> deleteUser(String userId) async {
    await _delete('/users/deleteUser/$userId');
  }

  Future<List<MessageModel>> getAllMessages() async {
    final users = await getAllUsers();
    List<MessageModel> allMessages = [];

    for (final user in users) {
      try {
        final response = await _get('/messages/messagesFromUser/${user.id}');
        final List<dynamic> data = jsonDecode(response.body);
        allMessages.addAll(data.map((msg) => MessageModel.fromJson(msg)));
      } catch (e) {
        continue;
      }
    }

    final uniqueMessages = {for (final msg in allMessages) msg.id: msg}.values.toList();
    uniqueMessages.sort((a, b) => (b.sentAt ?? DateTime.now()).compareTo(a.sentAt ?? DateTime.now()));
    return uniqueMessages;
  }

  Future<void> deleteMessage(String messageId) async {
    await _delete('/messages/deleteMessage/$messageId');
  }

  Future<MessageModel> getMessageById(String messageId) async {
    final response = await _get('/messages/$messageId');
    return MessageModel.fromJson(jsonDecode(response.body));
  }

  Future<List<MessageModel>> getMessagesByConversationId(String conversationId) async {
    final response = await _get('/messages/conversation/$conversationId');
    final List<dynamic> data = jsonDecode(response.body);
    return data.map((msg) => MessageModel.fromJson(msg)).toList();
  }

  Future<void> updateMessage({
    required String messageId,
    required String conversationId,
    required String senderId,
    required String recipientId,
    required String content,
  }) async {
    await _put('/messages/updateMessage/$messageId', {
      'conversationId': conversationId,
      'senderId': senderId,
      'recipientId': recipientId,
      'content': content,
    });
  }

  Future<ConversationModel> getConversationById(String conversationId) async {
    final response = await _get('/conversations/$conversationId');
    return ConversationModel.fromJson(jsonDecode(response.body));
  }

  Future<List<ConversationModel>> getConversationsOfUser(String userId) async {
    final response = await _get('/conversations/user/$userId');
    final List<dynamic> data = jsonDecode(response.body);
    return data.map((c) => ConversationModel.fromJson(c)).toList();
  }

  Future<ConversationModel?> findConversationBetweenUsers({
    required String user1Id,
    required String user2Id,
  }) async {
    final token = await AuthService.getStoredToken();

    final response = await http.get(
      Uri.parse('$baseUrl/conversations/between?user1Id=$user1Id&user2Id=$user2Id'),
      headers: _headers(token),
    );

    if (response.statusCode == 404) return null;
    _handleError(response);

    return ConversationModel.fromJson(jsonDecode(response.body));
  }

  Future<void> deleteConversation(String conversationId) async {
    await _delete('/conversations/$conversationId');
  }

  Future<Map<String, dynamic>> createSession({
    required String matchingRequestId,
    required String requesterUserId,
    required String receiverUserId,
  }) async {
    final response = await _post('/sessions', {
      'matchingRequestId': matchingRequestId,
      'requesterUserId': requesterUserId,
      'receiverUserId': receiverUserId,
    });
    return jsonDecode(response.body);
  }

  Future<Map<String, dynamic>> getSessionById(String sessionId) async {
    final response = await _get('/sessions/$sessionId');
    return jsonDecode(response.body);
  }
  Future<List<Map<String, dynamic>>> getAllSessions() async {
    final response = await _get('/sessions/get-all-sessions');
    final decoded = jsonDecode(response.body);

    if (decoded is List) {
      return decoded
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }

    return [];
  }

  Future<bool> isSessionParticipant({
    required String sessionId,
    required String userId,
  }) async {
    final response = await _get('/sessions/$sessionId/participants/$userId');
    return jsonDecode(response.body) == true;
  }

  Future<Map<String, dynamic>> expireRatingWindow(String sessionId) async {
    final response = await _patch('/sessions/$sessionId/expire-rating-window-temp-testing');
    return jsonDecode(response.body);
  }

  Future<Map<String, dynamic>> openRatingWindow(String sessionId) async {
    final response = await _patch('/sessions/$sessionId/open-rating-window');
    return jsonDecode(response.body);
  }

  Future<Map<String, dynamic>?> getSessionByMatchRequestId(String matchingRequestId) async {
    final token = await AuthService.getStoredToken();

    final response = await http.get(
      Uri.parse('$baseUrl/sessions/by-match-request/$matchingRequestId'),
      headers: _headers(token),
    );

    if (response.statusCode == 404) return null;
    _handleError(response);

    return jsonDecode(response.body);
  }

  Future<List<dynamic>> publishRatingsForSession(String sessionId) async {
    final response = await _post('/ratings/sessions/$sessionId/publish', {});
    return jsonDecode(response.body);
  }

  Future<Map<String, dynamic>> getRatingSummaryForUser(String userId) async {
    final response = await _get('/ratings/users/$userId/summary');
    return jsonDecode(response.body);
  }

  Future<void> updateMatchRequest({
    required String requestId,
    required String senderId,
    required String receiverId,
    required String status,
  }) async {
    await _patch('/match-requests/updateMatchRequest/$requestId', body: {
      'id': requestId,
      'senderId': senderId,
      'receiverId': receiverId,
      'status': status,
    });
  }

  Future<void> deleteMatchRequest(String requestId) async {
    await _delete('/match-requests/deleteMatchRequest/$requestId');
  }
  Future<List<Map<String, dynamic>>> getAllRatings() async {
    final response = await _get('/ratings/get-all-ratings');
    final decoded = jsonDecode(response.body);

    if (decoded is List) {
      return decoded
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }

    return [];
  }
  Future<List<MatchRequestModel>> getAllVisibleMatchRequestsForAdmin({
    List<UserModel>? users,
  }) async {
    final loadedUsers = users ?? await getAllUsers();

    final Map<String, MatchRequestModel> uniqueRequests = {};

    for (final user in loadedUsers) {
      final paths = [
        '/match-requests/incoming/${user.id}',
        '/match-requests/outgoing/${user.id}',
        '/match-requests/matches/${user.id}',
      ];

      for (final path in paths) {
        try {
          final response = await _get(path);
          final decoded = jsonDecode(response.body);

          if (decoded is List) {
            for (final item in decoded) {
              final request = MatchRequestModel.fromJson(
                item as Map<String, dynamic>,
              );

              if (request.id.isNotEmpty) {
                uniqueRequests[request.id] = request;
              }
            }
          }
        } catch (e) {
          continue;
        }
      }
    }

    final requests = uniqueRequests.values.toList();

    requests.sort((a, b) {
      final dateA = a.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      final dateB = b.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      return dateB.compareTo(dateA);
    });

    return requests;
  }
  Future<List<Map<String, dynamic>>> getPublishedRatings() async {
    final response = await _get('/ratings/admin/published-ratings');
    final decoded = jsonDecode(response.body);

    if (decoded is List) {
      return decoded
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }

    return [];
  }

  Future<List<Map<String, dynamic>>> getNonPublishedRatings() async {
    final response = await _get('/ratings/admin/non-published');
    final decoded = jsonDecode(response.body);

    if (decoded is List) {
      return decoded
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }

    return [];
  }

  Future<List<Map<String, dynamic>>> getAllRatingsForUser(String userId) async {
    final response = await _get('/ratings/admin/users/$userId');
    final decoded = jsonDecode(response.body);

    if (decoded is List) {
      return decoded
          .map((item) => Map<String, dynamic>.from(item as Map))
          .toList();
    }

    return [];
  }

  Future<Map<String, dynamic>> getRatingByIdForAdmin(String ratingId) async {
    final response = await _get('/ratings/admin/$ratingId');
    return Map<String, dynamic>.from(jsonDecode(response.body) as Map);
  }

  Future<Map<String, dynamic>> updateRatingStatus({
    required String ratingId,
    required String status,
  }) async {
    final response = await _patch(
      '/ratings/admin/$ratingId/status',
      body: {
        'status': status,
      },
    );

    return Map<String, dynamic>.from(jsonDecode(response.body) as Map);

    return Map<String, dynamic>.from(jsonDecode(response.body) as Map);
  }
}