import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/auth_service.dart';
import '../services/user_service.dart';
import '../services/admin_service.dart';
import '../services/matching_service.dart';
import '../services/match_request_service.dart';
import '../services/chat_service.dart';
import '../services/rating_service.dart';
import '../services/session_service.dart';

final authServiceProvider = Provider<AuthService>((ref) => AuthService());
final userServiceProvider = Provider<UserService>((ref) => UserService());
final adminServiceProvider = Provider<AdminService>((ref) => AdminService());
final matchingServiceProvider = Provider<MatchingService>((ref) => MatchingService());
final matchRequestServiceProvider = Provider<MatchRequestService>((ref) => MatchRequestService());
final chatServiceProvider = Provider<ChatService>((ref) => ChatService());
final ratingServiceProvider = Provider<RatingService>((ref) => RatingService());
final sessionServiceProvider = Provider<SessionService>((ref) => SessionService());