class SessionModel {
  final String id;
  final String matchingRequestId;
  final String requesterUserId;
  final String receiverUserId;
  final String status;
  final DateTime createdAt;
  final DateTime? acceptedAt;
  final DateTime? completedAt;
  final DateTime? ratingWindowOpenedAt;
  final DateTime? ratingWindowEndsAt;

  const SessionModel({
    required this.id,
    required this.matchingRequestId,
    required this.requesterUserId,
    required this.receiverUserId,
    required this.status,
    required this.createdAt,
    this.acceptedAt,
    this.completedAt,
    this.ratingWindowOpenedAt,
    this.ratingWindowEndsAt,
  });

  factory SessionModel.fromJson(Map<String, dynamic> json) {
    return SessionModel(
      id: json['id'],
      matchingRequestId: json['matchingRequestId'],
      requesterUserId: json['requesterUserId'],
      receiverUserId: json['receiverUserId'],
      status: json['status'],
      createdAt: DateTime.parse(json['createdAt']),
      acceptedAt: json['acceptedAt'] != null
          ? DateTime.parse(json['acceptedAt'])
          : null,
      completedAt: json['completedAt'] != null
          ? DateTime.parse(json['completedAt'])
          : null,
      ratingWindowOpenedAt: json['ratingWindowOpenedAt'] != null
          ? DateTime.parse(json['ratingWindowOpenedAt'])
          : null,
      ratingWindowEndsAt: json['ratingWindowEndsAt'] != null
          ? DateTime.parse(json['ratingWindowEndsAt'])
          : null,
    );
  }
}