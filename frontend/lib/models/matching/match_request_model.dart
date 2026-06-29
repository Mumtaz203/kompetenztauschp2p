class MatchRequestModel {
  final String id;
  final String senderId;
  final String receiverId;
  final String status;
  final DateTime? createdAt;

  MatchRequestModel({
    required this.id,
    required this.senderId,
    required this.receiverId,
    required this.status,
    this.createdAt,
  });

  factory MatchRequestModel.fromJson(Map<String, dynamic> json) {
    return MatchRequestModel(
      id: json['id']?.toString() ?? '',
      senderId: json['senderId']?.toString() ?? '',
      receiverId: json['receiverId']?.toString() ?? '',
      status: json['status']?.toString() ?? 'PENDING',
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
    );
  }
}