class PrivateSessionReportModel {
  final String id;
  final String sessionId;
  final String reporterUserId;
  final String reportedUserId;
  final String reasonCode;
  final String description;
  final DateTime? createdAt;

  const PrivateSessionReportModel({
    required this.id,
    required this.sessionId,
    required this.reporterUserId,
    required this.reportedUserId,
    required this.reasonCode,
    this.description = '',
    this.createdAt,
  });

  factory PrivateSessionReportModel.fromJson(Map<String, dynamic> json) {
    return PrivateSessionReportModel(
      id: json['id']?.toString() ?? '',
      sessionId: json['sessionId']?.toString() ?? '',
      reporterUserId: json['reporterUserId']?.toString() ?? '',
      reportedUserId: json['reportedUserId']?.toString() ?? '',
      reasonCode: json['reasonCode']?.toString() ?? '',
      description: json['description']?.toString() ?? '',
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
    );
  }
}
