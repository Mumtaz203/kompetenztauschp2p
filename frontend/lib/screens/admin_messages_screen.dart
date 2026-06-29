import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/message_model.dart';
import '../models/user_model.dart';
import '../providers/service_providers.dart';

class AdminMessagesScreen extends ConsumerStatefulWidget {
  const AdminMessagesScreen({super.key});

  @override
  ConsumerState<AdminMessagesScreen> createState() => _AdminMessagesScreenState();
}

class _AdminMessagesScreenState extends ConsumerState<AdminMessagesScreen> {
  final searchController = TextEditingController();

  List<MessageModel> messages = [];
  Map<String, UserModel> usersById = {};

  bool isLoading = true;
  bool showOnlyUnread = false;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadMessages();
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  Future<void> loadMessages() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final loadedMessages = await ref.read(adminServiceProvider).getAllMessages();
      final loadedUsers = await ref.read(adminServiceProvider).getAllUsers();

      final Map<String, UserModel> mappedUsers = {
        for (final user in loadedUsers) user.id: user,
      };

      if (!mounted) return;

      setState(() {
        messages = loadedMessages;
        usersById = mappedUsers;
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        errorMessage = e.toString().replaceAll('Exception: ', '');
        isLoading = false;
      });
    }
  }

  List<MessageModel> get filteredMessages {
    final query = searchController.text.trim().toLowerCase();

    return messages.where((message) {
      if (showOnlyUnread && message.read) return false;

      if (query.isEmpty) return true;

      final senderName = _userName(message.senderId).toLowerCase();
      final recipientName = _userName(message.recipientId).toLowerCase();

      final searchableText = [
        message.content,
        message.senderId,
        message.recipientId,
        message.conversationId,
        senderName,
        recipientName,
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  Future<void> _showDeleteDialog(BuildContext context, MessageModel msg) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Delete Message'),
          content: Text(
            'Are you sure you want to delete this message?\n\n"${msg.content}"',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.redAccent,
              ),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (shouldDelete != true) return;

    try {
      await ref.read(adminServiceProvider).deleteMessage(msg.id);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Message deleted successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadMessages();
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  String _userName(String userId) {
    final user = usersById[userId];

    if (user == null) {
      return 'Unknown User';
    }

    if (user.username.trim().isNotEmpty) {
      return user.username;
    }

    if (user.email.trim().isNotEmpty) {
      return user.email;
    }

    return 'Unknown User';
  }

  String _short(String value) {
    if (value.isEmpty) return '-';
    if (value.length <= 8) return value;
    return '${value.substring(0, 8)}...';
  }

  String _formatDate(DateTime? date) {
    if (date == null) return 'No date';

    final day = date.day.toString().padLeft(2, '0');
    final month = date.month.toString().padLeft(2, '0');
    final year = date.year.toString();
    final hour = date.hour.toString().padLeft(2, '0');
    final minute = date.minute.toString().padLeft(2, '0');

    return '$day.$month.$year $hour:$minute';
  }

  void _openMessageDetails(MessageModel message) {
    final senderName = _userName(message.senderId);
    final recipientName = _userName(message.recipientId);

    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Message Details',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 16),

                _detailRow('Message ID', message.id),
                _detailRow('Conversation ID', message.conversationId),
                _detailRow('From', '$senderName (${message.senderId})'),
                _detailRow('To', '$recipientName (${message.recipientId})'),
                _detailRow('Sent at', _formatDate(message.sentAt)),
                _detailRow('Read', message.read ? 'Yes' : 'No'),

                const SizedBox(height: 14),

                const Text(
                  'Content',
                  style: TextStyle(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 8),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppColors.primaryBlue.withOpacity(0.08),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Text(
                    message.content.isNotEmpty ? message.content : '[No Content]',
                  ),
                ),

                const SizedBox(height: 18),

                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () {
                      Navigator.pop(sheetContext);
                      _showDeleteDialog(context, message);
                    },
                    icon: const Icon(Icons.delete_outline),
                    label: const Text('Delete Message'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.redAccent,
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _detailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 9),
      child: RichText(
        text: TextSpan(
          style: const TextStyle(
            color: Colors.black87,
            fontSize: 14,
            height: 1.4,
          ),
          children: [
            TextSpan(
              text: '$label: ',
              style: const TextStyle(fontWeight: FontWeight.w900),
            ),
            TextSpan(text: value),
          ],
        ),
      ),
    );
  }

  Widget _statCard({
    required String title,
    required String value,
    required IconData icon,
    required Color color,
  }) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: color.withOpacity(0.12),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Column(
          children: [
            Icon(icon, color: color),
            const SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(
                color: color,
                fontSize: 20,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              title,
              textAlign: TextAlign.center,
              style: TextStyle(
                color: color.withOpacity(0.9),
                fontSize: 12,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _messageCard(MessageModel message) {
    final content = message.content.isNotEmpty ? message.content : '[No Content]';
    final senderName = _userName(message.senderId);
    final recipientName = _userName(message.recipientId);

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => _openMessageDetails(message),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: message.read
                        ? AppColors.primaryBlue.withOpacity(0.14)
                        : Colors.orange.withOpacity(0.16),
                    child: Icon(
                      message.read
                          ? Icons.mark_email_read_outlined
                          : Icons.mark_email_unread_outlined,
                      color: message.read
                          ? AppColors.primaryBlue
                          : Colors.orange,
                    ),
                  ),
                  const SizedBox(width: 12),

                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          senderName,
                          style: const TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 15,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'to $recipientName',
                          style: const TextStyle(
                            fontSize: 13,
                            color: Colors.grey,
                          ),
                        ),
                      ],
                    ),
                  ),

                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
                    decoration: BoxDecoration(
                      color: message.read
                          ? AppColors.primaryGreen.withOpacity(0.12)
                          : Colors.orange.withOpacity(0.14),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      message.read ? 'Read' : 'Unread',
                      style: TextStyle(
                        color: message.read
                            ? AppColors.primaryGreen
                            : Colors.orange,
                        fontSize: 11,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),

                  IconButton(
                    onPressed: () => _showDeleteDialog(context, message),
                    icon: const Icon(
                      Icons.delete_outline,
                      color: Colors.redAccent,
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 14),

              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppColors.primaryBlue.withOpacity(0.08),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Text(
                  content,
                  maxLines: 4,
                  overflow: TextOverflow.ellipsis,
                ),
              ),

              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Conversation: ${_short(message.conversationId)}',
                      style: const TextStyle(
                        color: Colors.grey,
                        fontSize: 12,
                      ),
                    ),
                  ),
                  Text(
                    _formatDate(message.sentAt),
                    style: const TextStyle(
                      color: Colors.grey,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final visibleMessages = filteredMessages;
    final unreadCount = messages.where((message) => !message.read).length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Message Management'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: loadMessages,
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Messages',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Review, search and delete messages from the system.',
            style: TextStyle(
              color: isDark
                  ? AppColors.subtitleDarkColor
                  : AppColors.subtitleBrightColor,
            ),
          ),

          const SizedBox(height: 18),

          Row(
            children: [
              _statCard(
                title: 'Total',
                value: messages.length.toString(),
                icon: Icons.message_outlined,
                color: AppColors.primaryBlue,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Unread',
                value: unreadCount.toString(),
                icon: Icons.mark_email_unread_outlined,
                color: Colors.orange,
              ),
            ],
          ),

          const SizedBox(height: 18),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search messages, users or conversation ID...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),

          const SizedBox(height: 10),

          SwitchListTile(
            value: showOnlyUnread,
            onChanged: (value) {
              setState(() {
                showOnlyUnread = value;
              });
            },
            contentPadding: EdgeInsets.zero,
            title: const Text(
              'Show only unread messages',
              style: TextStyle(fontWeight: FontWeight.w700),
            ),
          ),

          const SizedBox(height: 12),

          if (isLoading)
            const Padding(
              padding: EdgeInsets.all(40),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (errorMessage != null)
            Card(
              color: Colors.redAccent.withOpacity(0.12),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    const Icon(Icons.error_outline, color: Colors.redAccent),
                    const SizedBox(height: 8),
                    Text(
                      errorMessage!,
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 12),
                    ElevatedButton(
                      onPressed: loadMessages,
                      child: const Text('Try Again'),
                    ),
                  ],
                ),
              ),
            )
          else if (visibleMessages.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(
                    child: Text('No messages found.'),
                  ),
                ),
              )
            else ...[
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Text(
                    '${visibleMessages.length} message(s) found',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                ...visibleMessages.map(_messageCard),
              ],

          const SizedBox(height: 20),
        ],
      ),
    );
  }
}