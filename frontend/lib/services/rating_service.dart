import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/rating/rating_response_model.dart';
import '../models/rating/rating_summary_model.dart';
import '../models/rating/create_rating_request_model.dart';
import 'auth_service.dart';

class RatingService {
  static String baseUrl = AuthService.baseUrl;

  Future<Map<String, String>> _headers() async {
    final token = await AuthService.getStoredToken();
    return {
      'Content-Type': 'application/json',
      if (token != null && token.isNotEmpty) 'Authorization': 'Bearer $token',
    };
  }

  Future<RatingResponseModel> createRating(CreateRatingRequestModel request) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/ratings/create/'),
        headers: await _headers(),
        body: jsonEncode(request.toJson()),
      );

      if (response.statusCode == 201) {
        return RatingResponseModel.fromJson(jsonDecode(response.body));
      } else {
    final body = jsonDecode(response.body);
    final message = body['message'] ?? body['error'] ?? 'Fail in Rating Process';
    throw Exception(message);
    }
    } catch (e) {
      throw Exception('Connection Error: $e');
    }
  }

  Future<List<RatingResponseModel>> getMyRatings() async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/ratings/me'),
        headers: await _headers(),
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => RatingResponseModel.fromJson(json)).toList();
      } else {
        throw Exception('Fail in Rating Process: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection Error: $e');
    }
  }

  Future<RatingSummaryModel> getRatingSummary(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/ratings/users/$userId/summary'),
        headers: await _headers(),
      );

      if (response.statusCode == 200) {
        return RatingSummaryModel.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Fail in Summary Process: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('Connection Error: $e');
    }
  }
}