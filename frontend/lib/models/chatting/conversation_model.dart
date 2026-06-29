class ConversationModel {
  final String id;
  final String user1Id;
  final String user2Id;
  final DateTime? createdAt;
  final DateTime? lastMessageAt;

  ConversationModel({
    required this.id,
    required this.user1Id,
    required this.user2Id,
    this.createdAt,
    this.lastMessageAt,
  });

  factory ConversationModel.fromJson(Map<String, dynamic> json) {
    String parsedId = '';
    if (json['id'] != null) {
      parsedId = json['id'].toString();
    }

    String parsedUser1Id = '';
    if (json['user1Id'] != null) {
      parsedUser1Id = json['user1Id'].toString();
    }

    String parsedUser2Id = '';
    if (json['user2Id'] != null) {
      parsedUser2Id = json['user2Id'].toString();
    }

    DateTime? parsedCreatedAt = null;
    if (json['createdAt'] != null) {
      parsedCreatedAt = DateTime.tryParse(json['createdAt'].toString());
    }

    DateTime? parsedLastMessageAt = null;
    if (json['lastMessageAt'] != null) {
      parsedLastMessageAt = DateTime.tryParse(json['lastMessageAt'].toString());
    }

    return ConversationModel(
      id: parsedId,
      user1Id: parsedUser1Id,
      user2Id: parsedUser2Id,
      createdAt: parsedCreatedAt,
      lastMessageAt: parsedLastMessageAt,
    );
  }
}