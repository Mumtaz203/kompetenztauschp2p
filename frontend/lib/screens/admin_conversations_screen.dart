import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/chatting/conversation_model.dart';
import '../models/chatting/message_model.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';

class AdminConversationsScreen extends ConsumerStatefulWidget {
  const AdminConversationsScreen({super.key});

  @override
  ConsumerState<AdminConversationsScreen> createState() =>
      _AdminConversationsScreenState();
}

class _AdminConversationsScreenState
    extends ConsumerState<AdminConversationsScreen> {
  final searchController = TextEditingController();
  final conversationIdController = TextEditingController();

  List<UserModel> users = [];
  Map<String, UserModel> usersById = {};

  List<ConversationModel> conversations = [];
  List<MessageModel> messages = [];
  ConversationModel? selectedConversation;

  String? selectedUserId;
  String? selectedUser1Id;
  String? selectedUser2Id;

  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadInitialData();
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    conversationIdController.dispose();
    super.dispose();
  }

  Future<void> loadInitialData() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final loadedUsers = await ref.read(adminServiceProvider).getAllUsers();

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        usersById = {
          for (final user in loadedUsers) user.id: user,
        };
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

  Future<void> loadConversationById() async {
    final id = conversationIdController.text.trim();
    if (id.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedConversation = null;
      messages = [];
    });

    try {
      final conversation =
      await ref.read(adminServiceProvider).getConversationById(id);

      final loadedMessages =
      await ref.read(adminServiceProvider).getMessagesByConversationId(id);

      if (!mounted) return;

      setState(() {
        selectedConversation = conversation;
        messages = loadedMessages;
        conversations = [conversation];
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

  Future<void> loadConversationsOfSelectedUser() async {
    final userId = selectedUserId;
    if (userId == null || userId.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedConversation = null;
      messages = [];
    });

    try {
      final loadedConversations =
      await ref.read(adminServiceProvider).getConversationsOfUser(userId);

      if (!mounted) return;

      setState(() {
        conversations = loadedConversations;
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

  Future<void> findBetweenSelectedUsers() async {
    final user1Id = selectedUser1Id;
    final user2Id = selectedUser2Id;

    if (user1Id == null || user2Id == null || user1Id == user2Id) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedConversation = null;
      messages = [];
      conversations = [];
    });

    try {
      final conversation =
      await ref.read(adminServiceProvider).findConversationBetweenUsers(
        user1Id: user1Id,
        user2Id: user2Id,
      );

      if (!mounted) return;

      if (conversation == null) {
        setState(() {
          errorMessage = 'No conversation found between these users.';
          isLoading = false;
        });
        return;
      }

      final loadedMessages = await ref
          .read(adminServiceProvider)
          .getMessagesByConversationId(conversation.id);

      if (!mounted) return;

      setState(() {
        selectedConversation = conversation;
        conversations = [conversation];
        messages = loadedMessages;
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

  Future<void> openConversation(ConversationModel conversation) async {
    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedConversation = conversation;
      messages = [];
    });

    try {
      final loadedMessages = await ref
          .read(adminServiceProvider)
          .getMessagesByConversationId(conversation.id);

      if (!mounted) return;

      setState(() {
        messages = loadedMessages;
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

  Future<void> deleteConversation(String conversationId) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Delete Conversation'),
          content: const Text(
            'Are you sure you want to delete this conversation?',
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
                foregroundColor: Colors.white,
              ),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (shouldDelete != true) return;

    try {
      await ref.read(adminServiceProvider).deleteConversation(conversationId);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Conversation deleted successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      setState(() {
        conversations.removeWhere((c) => c.id == conversationId);
        if (selectedConversation?.id == conversationId) {
          selectedConversation = null;
          messages = [];
        }
      });
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

  List<ConversationModel> get filteredConversations {
    final query = searchController.text.trim().toLowerCase();

    if (query.isEmpty) return conversations;

    return conversations.where((conversation) {
      final user1Name = _userName(conversation.user1Id);
      final user2Name = _userName(conversation.user2Id);

      final searchableText = [
        conversation.id,
        conversation.user1Id,
        conversation.user2Id,
        user1Name,
        user2Name,
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  String _userName(String userId) {
    final user = usersById[userId];

    if (user == null) return 'Unknown User';
    if (user.username.trim().isNotEmpty) return user.username;
    if (user.email.trim().isNotEmpty) return user.email;

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

  Widget _userDropdown({
    required String label,
    required String? value,
    required void Function(String?) onChanged,
  }) {
    return DropdownButtonFormField<String>(
      value: value,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
        ),
      ),
      items: users.map((user) {
        final name = user.username.isEmpty ? 'Unknown User' : user.username;
        final email = user.email.isEmpty ? 'No email' : user.email;

        return DropdownMenuItem<String>(
          value: user.id,
          child: Text(
            '$name — $email',
            overflow: TextOverflow.ellipsis,
          ),
        );
      }).toList(),
      onChanged: onChanged,
    );
  }

  Widget _actionCard({
    required String title,
    required List<Widget> children,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.w900,
                fontSize: 16,
              ),
            ),
            const SizedBox(height: 12),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _conversationCard(ConversationModel conversation) {
    final user1Name = _userName(conversation.user1Id);
    final user2Name = _userName(conversation.user2Id);

    final isSelected = selectedConversation?.id == conversation.id;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: isSelected ? 3 : 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
        side: BorderSide(
          color: isSelected
              ? AppColors.primaryBlue.withOpacity(0.5)
              : Colors.transparent,
        ),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => openConversation(conversation),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: AppColors.primaryBlue.withOpacity(0.14),
                    child: const Icon(
                      Icons.forum_outlined,
                      color: AppColors.primaryBlue,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      '$user1Name ↔ $user2Name',
                      style: const TextStyle(
                        fontWeight: FontWeight.w900,
                        fontSize: 15,
                      ),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(
                      Icons.delete_outline,
                      color: Colors.redAccent,
                    ),
                    onPressed: () => deleteConversation(conversation.id),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'ID: ${_short(conversation.id)}',
                      style: const TextStyle(
                        color: Colors.grey,
                        fontSize: 12,
                      ),
                    ),
                  ),
                  Text(
                    _formatDate(conversation.lastMessageAt),
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

  Widget _messageCard(MessageModel message) {
    final senderName = _userName(message.senderId);
    final recipientName = _userName(message.recipientId);
    final content = message.content.isEmpty ? '[No content]' : message.content;

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppColors.primaryGreen.withOpacity(0.14),
          child: const Icon(
            Icons.chat_bubble_outline,
            color: AppColors.primaryGreen,
          ),
        ),
        title: Text(content),
        subtitle: Text(
          'From: $senderName\nTo: $recipientName\nSent: ${_formatDate(message.sentAt)}',
        ),
        isThreeLine: true,
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
          border: Border.all(color: color.withOpacity(0.22)),
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

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final visibleConversations = filteredConversations;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Conversation Management'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: loadInitialData,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Conversations',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Inspect conversations by selecting users instead of entering UUIDs manually.',
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
                title: 'Loaded Users',
                value: users.length.toString(),
                icon: Icons.people_outline,
                color: AppColors.primaryBlue,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Conversations',
                value: conversations.length.toString(),
                icon: Icons.forum_outlined,
                color: AppColors.primaryGreen,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Messages',
                value: messages.length.toString(),
                icon: Icons.message_outlined,
                color: Colors.orange,
              ),
            ],
          ),

          const SizedBox(height: 18),

          _actionCard(
            title: 'Find conversations of one user',
            children: [
              _userDropdown(
                label: 'Select User',
                value: selectedUserId,
                onChanged: (value) {
                  setState(() {
                    selectedUserId = value;
                  });
                },
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed:
                  isLoading ? null : loadConversationsOfSelectedUser,
                  icon: const Icon(Icons.search),
                  label: const Text('Load User Conversations'),
                ),
              ),
            ],
          ),

          _actionCard(
            title: 'Find conversation between two users',
            children: [
              _userDropdown(
                label: 'User 1',
                value: selectedUser1Id,
                onChanged: (value) {
                  setState(() {
                    selectedUser1Id = value;
                  });
                },
              ),
              const SizedBox(height: 10),
              _userDropdown(
                label: 'User 2',
                value: selectedUser2Id,
                onChanged: (value) {
                  setState(() {
                    selectedUser2Id = value;
                  });
                },
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: isLoading ? null : findBetweenSelectedUsers,
                  icon: const Icon(Icons.compare_arrows_rounded),
                  label: const Text('Find Between Users'),
                ),
              ),
            ],
          ),

          _actionCard(
            title: 'Search by conversation ID',
            children: [
              TextField(
                controller: conversationIdController,
                decoration: InputDecoration(
                  labelText: 'Conversation ID',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed: isLoading ? null : loadConversationById,
                  icon: const Icon(Icons.tag),
                  label: const Text('Load Conversation By ID'),
                ),
              ),
            ],
          ),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Filter loaded conversations...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),

          const SizedBox(height: 18),

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
                child: Text(
                  errorMessage!,
                  textAlign: TextAlign.center,
                ),
              ),
            )
          else if (visibleConversations.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(
                    child: Text('No conversations loaded yet.'),
                  ),
                ),
              )
            else ...[
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Text(
                    '${visibleConversations.length} conversation(s) found',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                ...visibleConversations.map(_conversationCard),
              ],

          if (selectedConversation != null) ...[
            const SizedBox(height: 22),
            const Text(
              'Messages in selected conversation',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 10),
            if (messages.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(18),
                  child: Text('No messages found for this conversation.'),
                ),
              )
            else
              ...messages.map(_messageCard),
          ],

          const SizedBox(height: 20),
        ],
      ),
    );
  }
}