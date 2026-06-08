import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../models/message_model.dart';
import '../providers/service_providers.dart';
import '../services/auth_service.dart';

class ChatScreen extends ConsumerStatefulWidget {
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
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
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
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadConversation();
    });
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

      final chatService = ref.read(chatServiceProvider);

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
      await ref.read(chatServiceProvider).sendMessage(
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
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _goHome();
      },
      child: Scaffold(
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        appBar: AppBar(
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
          elevation: 0,
          leading: IconButton(
            onPressed: _goHome,
            icon: Icon(
              Icons.arrow_back_ios_new_rounded,
              color: isDark ? Colors.white : Colors.black87,
            ),
          ),
          titleSpacing: 0,
          title: InkWell(
            onTap: openChatPartnerProfile,
            borderRadius: BorderRadius.circular(18),
            child: Row(
              children: [
                CircleAvatar(
                  radius: 20,
                  backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
                  child: Text(
                    chatPartnerName.isNotEmpty
                        ? chatPartnerName[0].toUpperCase()
                        : '?',
                    style: const TextStyle(
                      color: AppColors.primaryBlue,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    chatPartnerName,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: isDark ? AppColors.textColor : Colors.black87,
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        body: SafeArea(
          child: isLoading
              ? const Center(
            child: CircularProgressIndicator(
              color: AppColors.primaryBlue,
            ),
          )
              : errorMessage != null
              ? _ErrorState(
            isDark: isDark,
            message: errorMessage!,
            onPressed: loadConversation,
          )
              : Column(
            children: [
              Expanded(
                child: messages.isEmpty
                    ? _EmptyChatState(isDark: isDark)
                    : ListView.builder(
                  controller: scrollController,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 18,
                    vertical: 16,
                  ),
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final message = messages[index];
                    final isMe =
                        message.senderId == currentUserId;

                    return Align(
                      alignment: isMe
                          ? Alignment.centerRight
                          : Alignment.centerLeft,
                      child: Padding(
                        padding:
                        const EdgeInsets.only(bottom: 10),
                        child: _MessageBubble(
                          text: message.content,
                          isMe: isMe,
                        ),
                      ),
                    );
                  },
                ),
              ),
              _MessageInputArea(
                controller: messageController,
                isDark: isDark,
                isSending: isSending,
                onSend: sendMessage,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _MessageInputArea extends StatelessWidget {
  final TextEditingController controller;
  final bool isDark;
  final bool isSending;
  final VoidCallback onSend;

  const _MessageInputArea({
    required this.controller,
    required this.isDark,
    required this.isSending,
    required this.onSend,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(14, 10, 14, 12),
      decoration: BoxDecoration(
        color: Theme.of(context).scaffoldBackgroundColor,
        border: Border(
          top: BorderSide(
            color: isDark ? Colors.white12 : Colors.black12,
          ),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: controller,
              minLines: 1,
              maxLines: 4,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => onSend(),
              style: TextStyle(
                color: isDark ? Colors.white : Colors.black87,
              ),
              decoration: InputDecoration(
                hintText: 'Type a message...',
                hintStyle: TextStyle(
                  color: isDark ? Colors.white54 : Colors.black45,
                ),
                filled: true,
                fillColor: isDark
                    ? const Color(0xFF1E293B)
                    : Colors.white.withOpacity(0.95),
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 18,
                  vertical: 14,
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(22),
                  borderSide: BorderSide.none,
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(22),
                  borderSide: const BorderSide(
                    color: AppColors.primaryBlue,
                    width: 1.4,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: AppColors.primaryBlueGradient,
              boxShadow: [
                BoxShadow(
                  color: AppColors.primaryBlue.withOpacity(0.25),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: IconButton(
              onPressed: isSending ? null : onSend,
              icon: isSending
                  ? const SizedBox(
                width: 19,
                height: 19,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: Colors.white,
                ),
              )
                  : const Icon(
                Icons.send_rounded,
                color: Colors.white,
                size: 21,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final String text;
  final bool isMe;

  const _MessageBubble({
    required this.text,
    required this.isMe,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      constraints: const BoxConstraints(maxWidth: 280),
      decoration: BoxDecoration(
        gradient: isMe ? AppColors.primaryBlueGradient : null,
        color: isMe
            ? null
            : isDark
            ? const Color(0xFF1E293B)
            : Colors.white,
        borderRadius: BorderRadius.only(
          topLeft: const Radius.circular(18),
          topRight: const Radius.circular(18),
          bottomLeft: Radius.circular(isMe ? 18 : 4),
          bottomRight: Radius.circular(isMe ? 4 : 18),
        ),
        border: isMe
            ? null
            : Border.all(
          color: isDark ? Colors.white12 : Colors.black12,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(isDark ? 0.16 : 0.05),
            blurRadius: 12,
            offset: const Offset(0, 5),
          ),
        ],
      ),
      child: Text(
        text,
        style: TextStyle(
          color: isMe
              ? Colors.white
              : isDark
              ? AppColors.textColor
              : Colors.black87,
          fontSize: 14.5,
          height: 1.35,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}

class _EmptyChatState extends StatelessWidget {
  final bool isDark;

  const _EmptyChatState({required this.isDark});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Text(
        'No messages yet.',
        style: TextStyle(
          color: isDark
              ? AppColors.subtitleDarkColor
              : AppColors.subtitleBrightColor,
          fontSize: 15,
        ),
      ),
    );
  }
}

class _ErrorState extends StatelessWidget {
  final bool isDark;
  final String message;
  final VoidCallback onPressed;

  const _ErrorState({
    required this.isDark,
    required this.message,
    required this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: isDark
                ? const Color(0xFF1E293B).withOpacity(0.85)
                : Colors.white.withOpacity(0.95),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: isDark ? Colors.white12 : Colors.black12,
            ),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(
                Icons.error_outline_rounded,
                color: Colors.redAccent,
                size: 42,
              ),
              const SizedBox(height: 14),
              Text(
                message,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: isDark ? AppColors.textColor : Colors.black87,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 16),
              TextButton(
                onPressed: onPressed,
                child: const Text(
                  'Try again',
                  style: TextStyle(
                    color: AppColors.primaryBlue,
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}