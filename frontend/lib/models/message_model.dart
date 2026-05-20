class MessageModel {
  final String id;
  final String conversationId;
  final String senderId;
  final String recipientId;
  final String content;
  final DateTime? sentAt;
  final bool read;

  MessageModel({
    required this.id,
    required this.conversationId,
    required this.senderId,
    required this.recipientId,
    required this.content,
    this.sentAt,
    required this.read,
  });

  factory MessageModel.fromJson(Map<String, dynamic> json) {
    String parsedId = '';
    if (json['id'] != null) {
      parsedId = json['id'].toString();
    }

    String parsedConversationId = '';
    if (json['conversationId'] != null) {
      parsedConversationId = json['conversationId'].toString();
    }

    String parsedSenderId = '';
    if (json['senderId'] != null) {
      parsedSenderId = json['senderId'].toString();
    }

    String parsedRecipientId = '';
    if (json['recipientId'] != null) {
      parsedRecipientId = json['recipientId'].toString();
    }

    String parsedContent = '';
    if (json['content'] != null) {
      parsedContent = json['content'].toString();
    }

    DateTime? parsedSentAt = null;
    if (json['sentAt'] != null) {
      parsedSentAt = DateTime.tryParse(json['sentAt'].toString());
    }

    bool parsedRead = false;
    if (json['read'] != null) {
      if (json['read'] == true) {
        parsedRead = true;
      }
    }

    return MessageModel(
      id: parsedId,
      conversationId: parsedConversationId,
      senderId: parsedSenderId,
      recipientId: parsedRecipientId,
      content: parsedContent,
      sentAt: parsedSentAt,
      read: parsedRead,
    );
  }
}