class CreateRatingRequestModel {
  final String sessionId;
  final String receiverUserId;
  final double points;
  final String? comment;

  const CreateRatingRequestModel({
    required this.sessionId,
    required this.receiverUserId,
    required this.points,
    this.comment,
  });

  Map<String, dynamic> toJson() {
    return {
      'sessionId': sessionId,
      'receiverUserId': receiverUserId,
      'points': points,
      if (comment != null) 'comment': comment,
    };
  }
}