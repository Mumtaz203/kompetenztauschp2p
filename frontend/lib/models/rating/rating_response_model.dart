class RatingResponseModel {
  final String id;
  final String sessionId;
  final String senderUserId;
  final String receiverUserId;
  final String status;
  final double points;
  final String? comment;
  final DateTime createdAt;
  final DateTime? publishedAt;

  const RatingResponseModel({
    required this.id,
    required this.sessionId,
    required this.senderUserId,
    required this.receiverUserId,
    required this.status,
    required this.points,
    this.comment,
    required this.createdAt,
    this.publishedAt,
  });

  factory RatingResponseModel.fromJson(Map<String, dynamic> json) {
    return RatingResponseModel(
      id: json['id'],
      sessionId: json['sessionId'],
      senderUserId: json['senderUserId'],
      receiverUserId: json['receiverUserId'],
      status: json['status'],
      points: (json['points'] as num).toDouble(),
      comment: json['comment'],
      createdAt: DateTime.parse(json['createdAt']),
      publishedAt: json['publishedAt'] != null
          ? DateTime.parse(json['publishedAt'])
          : null,
    );
  }
}