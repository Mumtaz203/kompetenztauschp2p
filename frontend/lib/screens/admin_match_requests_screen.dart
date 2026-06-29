import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/match_request_model.dart';
import '../models/user_model.dart';
import '../providers/service_providers.dart';

class AdminMatchRequestsScreen extends ConsumerStatefulWidget {
  const AdminMatchRequestsScreen({super.key});

  @override
  ConsumerState<AdminMatchRequestsScreen> createState() =>
      _AdminMatchRequestsScreenState();
}

class _AdminMatchRequestsScreenState
    extends ConsumerState<AdminMatchRequestsScreen> {
  final searchController = TextEditingController();

  List<UserModel> users = [];
  Map<String, UserModel> usersById = {};
  List<MatchRequestModel> requests = [];

  bool isLoading = true;
  String? errorMessage;
  String selectedFilter = 'all';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadRequests();
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  Future<void> loadRequests() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final adminService = ref.read(adminServiceProvider);

      final loadedUsers = await adminService.getAllUsers();
      final loadedRequests =
      await adminService.getAllVisibleMatchRequestsForAdmin(
        users: loadedUsers,
      );

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        usersById = {
          for (final user in loadedUsers) user.id: user,
        };
        requests = loadedRequests;
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

  List<MatchRequestModel> get filteredRequests {
    final query = searchController.text.trim().toLowerCase();

    return requests.where((request) {
      final status = request.status.toUpperCase();

      if (selectedFilter == 'pending' && status != 'PENDING') return false;
      if (selectedFilter == 'accepted' && status != 'ACCEPTED') return false;

      if (query.isEmpty) return true;

      final senderName = _userName(request.senderId);
      final receiverName = _userName(request.receiverId);

      final searchableText = [
        request.id,
        request.senderId,
        request.receiverId,
        request.status,
        senderName,
        receiverName,
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  Future<void> updateStatus({
    required MatchRequestModel request,
    required String status,
  }) async {
    try {
      await ref.read(adminServiceProvider).updateMatchRequest(
        requestId: request.id,
        senderId: request.senderId,
        receiverId: request.receiverId,
        status: status,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Match request updated to $status.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadRequests();
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

  Future<void> deleteRequest(MatchRequestModel request) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Delete Match Request'),
          content: Text(
            'Are you sure you want to delete the match request from '
                '${_userName(request.senderId)} to ${_userName(request.receiverId)}?',
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
      await ref.read(adminServiceProvider).deleteMatchRequest(request.id);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Match request deleted successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadRequests();
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

    if (user == null) return 'Unknown User';
    if (user.username.trim().isNotEmpty) return user.username;
    if (user.email.trim().isNotEmpty) return user.email;

    return 'Unknown User';
  }

  String _userEmail(String userId) {
    final user = usersById[userId];
    if (user == null || user.email.trim().isEmpty) return 'No email';
    return user.email;
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

  Color _statusColor(String status) {
    final normalized = status.toUpperCase();

    if (normalized == 'ACCEPTED') return AppColors.primaryGreen;
    if (normalized == 'PENDING') return Colors.orange;
    if (normalized == 'REJECTED') return Colors.redAccent;

    return Colors.grey;
  }

  IconData _statusIcon(String status) {
    final normalized = status.toUpperCase();

    if (normalized == 'ACCEPTED') return Icons.check_circle_outline;
    if (normalized == 'PENDING') return Icons.pending_actions;
    if (normalized == 'REJECTED') return Icons.cancel_outlined;

    return Icons.help_outline;
  }

  Widget _filterChip({
    required String label,
    required String value,
  }) {
    final selected = selectedFilter == value;

    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) {
        setState(() {
          selectedFilter = value;
        });
      },
      selectedColor: AppColors.primaryBlue.withOpacity(0.18),
      labelStyle: TextStyle(
        color: selected ? AppColors.primaryBlue : null,
        fontWeight: selected ? FontWeight.w800 : FontWeight.normal,
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
          border: Border.all(
            color: color.withOpacity(0.22),
          ),
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

  void _openDetails(MatchRequestModel request) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        final status = request.status.toUpperCase();
        final color = _statusColor(status);

        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Match Request Details',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                    ),
                  ),

                  const SizedBox(height: 16),

                  Row(
                    children: [
                      Icon(_statusIcon(status), color: color),
                      const SizedBox(width: 8),
                      Text(
                        status,
                        style: TextStyle(
                          color: color,
                          fontSize: 16,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 18),

                  _detailTitle('Request ID'),
                  SelectableText(request.id),

                  const SizedBox(height: 16),

                  _detailTitle('Sender'),
                  SelectableText(
                    '${_userName(request.senderId)}\n${_userEmail(request.senderId)}\n${request.senderId}',
                  ),

                  const SizedBox(height: 16),

                  _detailTitle('Receiver'),
                  SelectableText(
                    '${_userName(request.receiverId)}\n${_userEmail(request.receiverId)}\n${request.receiverId}',
                  ),

                  const SizedBox(height: 16),

                  _detailTitle('Created At'),
                  Text(_formatDate(request.createdAt)),

                  const SizedBox(height: 24),

                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          onPressed: status == 'PENDING'
                              ? null
                              : () {
                            Navigator.pop(sheetContext);
                            updateStatus(
                              request: request,
                              status: 'PENDING',
                            );
                          },
                          child: const Text('Set Pending'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: OutlinedButton(
                          onPressed: status == 'ACCEPTED'
                              ? null
                              : () {
                            Navigator.pop(sheetContext);
                            updateStatus(
                              request: request,
                              status: 'ACCEPTED',
                            );
                          },
                          child: const Text('Set Accepted'),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 10),

                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.pop(sheetContext);
                        deleteRequest(request);
                      },
                      icon: const Icon(Icons.delete_outline),
                      label: const Text('Delete Match Request'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.redAccent,
                        side: const BorderSide(color: Colors.redAccent),
                        padding: const EdgeInsets.symmetric(vertical: 14),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _detailTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w900,
      ),
    );
  }

  Widget _requestCard(MatchRequestModel request) {
    final status = request.status.toUpperCase();
    final color = _statusColor(status);

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => _openDetails(request),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: color.withOpacity(0.14),
                    child: Icon(
                      _statusIcon(status),
                      color: color,
                    ),
                  ),
                  const SizedBox(width: 12),

                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${_userName(request.senderId)} → ${_userName(request.receiverId)}',
                          style: const TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 15,
                          ),
                        ),
                        const SizedBox(height: 3),
                        Text(
                          'Request ID: ${_short(request.id)}',
                          style: const TextStyle(
                            color: Colors.grey,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),

                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: color.withOpacity(0.12),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      status,
                      style: TextStyle(
                        color: color,
                        fontSize: 11,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 14),

              Text(
                'Sender: ${_userEmail(request.senderId)}',
                style: const TextStyle(fontSize: 13, color: Colors.grey),
              ),
              const SizedBox(height: 3),
              Text(
                'Receiver: ${_userEmail(request.receiverId)}',
                style: const TextStyle(fontSize: 13, color: Colors.grey),
              ),

              const SizedBox(height: 12),

              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Created: ${_formatDate(request.createdAt)}',
                      style: const TextStyle(
                        color: Colors.grey,
                        fontSize: 12,
                      ),
                    ),
                  ),
                  IconButton(
                    onPressed: () => deleteRequest(request),
                    icon: const Icon(
                      Icons.delete_outline,
                      color: Colors.redAccent,
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
    final visibleRequests = filteredRequests;

    final pendingCount = requests
        .where((request) => request.status.toUpperCase() == 'PENDING')
        .length;

    final acceptedCount = requests
        .where((request) => request.status.toUpperCase() == 'ACCEPTED')
        .length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Match Request Management'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: loadRequests,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Match Requests',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Review, filter and manage visible match requests.',
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
                value: requests.length.toString(),
                icon: Icons.sync_alt_rounded,
                color: AppColors.primaryBlue,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Pending',
                value: pendingCount.toString(),
                icon: Icons.pending_actions,
                color: Colors.orange,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Accepted',
                value: acceptedCount.toString(),
                icon: Icons.check_circle_outline,
                color: AppColors.primaryGreen,
              ),
            ],
          ),

          const SizedBox(height: 18),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search by user, status or request ID...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),

          const SizedBox(height: 12),

          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _filterChip(label: 'All', value: 'all'),
              _filterChip(label: 'Pending', value: 'pending'),
              _filterChip(label: 'Accepted', value: 'accepted'),
            ],
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
          else if (visibleRequests.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(
                    child: Text('No match requests found.'),
                  ),
                ),
              )
            else ...[
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Text(
                    '${visibleRequests.length} request(s) found',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                ...visibleRequests.map(_requestCard),
              ],

          const SizedBox(height: 20),
        ],
      ),
    );
  }
}