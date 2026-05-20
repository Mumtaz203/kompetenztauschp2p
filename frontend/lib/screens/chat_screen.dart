import 'package:flutter/material.dart';
import '../models/message_model.dart';
import '../services/chat_service.dart';
import '../services/auth_service.dart';

class ChatScreen extends StatefulWidget {
  final String? conversationId;
  final String? currentUserId;
  final String? otherUserId;
  final String? otherUserName;

  const ChatScreen({
    super.key,
    this.conversationId,
    this.currentUserId,
    this.otherUserId,
    this.otherUserName,
  });

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final ChatService chatService = ChatService();
  final TextEditingController messageController = TextEditingController();
  final ScrollController scrollController = ScrollController();

  bool isLoading = true;
  bool isSending = false;

  String chatPartnerName = 'Chat';
  String conversationId = '';
  String currentUserId = '';
  String recipientId = '';
  String? errorMessage;
  List<MessageModel> messages = [];

  @override
  void initState() {
    super.initState();
    loadConversation();
  }

  Future<void> loadConversation() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      currentUserId =
          widget.currentUserId ?? await AuthService.getStoredUserId() ?? '';
      conversationId = widget.conversationId ?? '';

      if (currentUserId.isEmpty) {
        throw Exception('Current user id is missing. Please login again.');
      }

      if (conversationId.isEmpty) {
        final otherUserId = widget.otherUserId ?? '';
        if (otherUserId.isEmpty) {
          throw Exception('Conversation id or other user id is required.');
        }

        final createdConversation = await chatService.createConversation(
          currentUserId: currentUserId,
          otherUserId: otherUserId,
        );
        conversationId = createdConversation['id']?.toString() ?? '';
      }

      if (conversationId.isEmpty) {
        throw Exception('Conversation could not be created.');
      }

      final data = await chatService.getConversationDetails(conversationId);

      final user1Id = data['user1Id']?.toString() ?? '';
      final user2Id = data['user2Id']?.toString() ?? '';
      final user1Name = data['user1Name']?.toString() ?? 'User 1';
      final user2Name = data['user2Name']?.toString() ?? 'User 2';

      if (currentUserId == user1Id) {
        chatPartnerName = user2Name;
        recipientId = user2Id;
      } else {
        chatPartnerName = user1Name;
        recipientId = user1Id;
      }

      final List<dynamic> rawMessages = data['messages'] ?? [];
      messages = rawMessages
          .map((json) => MessageModel.fromJson(json))
          .toList();

      if (!mounted) return;
      setState(() => isLoading = false);
      _scrollToBottom();
    } catch (e) {
      if (!mounted) return;
      setState(() {
        isLoading = false;
        errorMessage = 'Chat could not be loaded: $e';
        chatPartnerName = widget.otherUserName ?? 'Chat';
      });
    }
  }

  Future<void> sendMessage() async {
    final text = messageController.text.trim();

    if (text.isEmpty || recipientId.isEmpty || conversationId.isEmpty) return;

    setState(() => isSending = true);

    try {
      await chatService.sendMessage(
        conversationId: conversationId,
        senderId: currentUserId,
        recipientId: recipientId,
        content: text,
      );

      messageController.clear();

      await loadConversation();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Message could not be sent: $e')));
    } finally {
      if (mounted) setState(() => isSending = false);
    }
  }

  void openChatPartnerProfile() {
    if (recipientId.isEmpty) return;
    Navigator.pushNamed(
      context,
      '/user-profile',
      arguments: {'userId': recipientId, 'username': chatPartnerName},
    );
  }

  void _goHome() {
    Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!scrollController.hasClients) return;
      scrollController.animateTo(
        scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 250),
        curve: Curves.easeOut,
      );
    });
  }

  @override
  void dispose() {
    messageController.dispose();
    scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _goHome();
      },
      child: Scaffold(
        appBar: AppBar(
          leading: IconButton(
            onPressed: _goHome,
            icon: const Icon(Icons.arrow_back),
          ),
          title: InkWell(
            onTap: openChatPartnerProfile,
            child: Row(
              children: [
                const CircleAvatar(
                  radius: 18,
                  child: Icon(Icons.person, size: 20),
                ),
                const SizedBox(width: 10),
                Text(chatPartnerName),
              ],
            ),
          ),
        ),
        body: isLoading
            ? const Center(child: CircularProgressIndicator())
            : errorMessage != null
            ? Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(errorMessage!, textAlign: TextAlign.center),
                      const SizedBox(height: 12),
                      ElevatedButton(
                        onPressed: loadConversation,
                        child: const Text('Try again'),
                      ),
                    ],
                  ),
                ),
              )
            : Column(
                children: [
                  Expanded(
                    child: messages.isEmpty
                        ? const Center(child: Text('No messages yet.'))
                        : ListView.builder(
                            controller: scrollController,
                            padding: const EdgeInsets.all(16),
                            itemCount: messages.length,
                            itemBuilder: (context, index) {
                              final message = messages[index];
                              final isMe = message.senderId == currentUserId;

                              return Align(
                                alignment: isMe
                                    ? Alignment.centerRight
                                    : Alignment.centerLeft,
                                child: Padding(
                                  padding: const EdgeInsets.only(bottom: 10),
                                  child: _MessageBubble(
                                    text: message.content,
                                    isMe: isMe,
                                  ),
                                ),
                              );
                            },
                          ),
                  ),
                  SafeArea(
                    top: false,
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: messageController,
                              minLines: 1,
                              maxLines: 4,
                              textInputAction: TextInputAction.send,
                              onSubmitted: (_) => sendMessage(),
                              decoration: const InputDecoration(
                                hintText: 'Type a message...',
                                border: OutlineInputBorder(),
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          IconButton(
                            onPressed: isSending ? null : sendMessage,
                            icon: isSending
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                      strokeWidth: 2,
                                    ),
                                  )
                                : const Icon(Icons.send),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final String text;
  final bool isMe;

  const _MessageBubble({required this.text, required this.isMe});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      constraints: const BoxConstraints(maxWidth: 260),
      decoration: BoxDecoration(
        color: isMe ? Color(0xD30FE4E4) : Color(0xFF0E50C8),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Text(text),
    );
  }
}
