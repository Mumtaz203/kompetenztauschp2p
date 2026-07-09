import 'package:shared_preferences/shared_preferences.dart';

class SessionReportStatusStore {
  static String _key({
    required String reporterUserId,
    required String sessionId,
    required String reportedUserId,
  }) {
    return 'private_session_reported_${reporterUserId}_${sessionId}_$reportedUserId';
  }

  static Future<bool> hasReported({
    required String reporterUserId,
    required String sessionId,
    required String reportedUserId,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(
          _key(
            reporterUserId: reporterUserId,
            sessionId: sessionId,
            reportedUserId: reportedUserId,
          ),
        ) ??
        false;
  }

  static Future<void> markReported({
    required String reporterUserId,
    required String sessionId,
    required String reportedUserId,
  }) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(
      _key(
        reporterUserId: reporterUserId,
        sessionId: sessionId,
        reportedUserId: reportedUserId,
      ),
      true,
    );
  }
}
