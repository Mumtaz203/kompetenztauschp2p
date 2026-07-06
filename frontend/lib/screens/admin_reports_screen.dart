import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/chatting/message_model.dart';
import '../models/session/private_session_report_model.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';
import '../utils/report_reason_labels.dart';

class AdminReportsScreen extends ConsumerStatefulWidget {
  const AdminReportsScreen({super.key});

  @override
  ConsumerState<AdminReportsScreen> createState() => _AdminReportsScreenState();
}

class _AdminReportsScreenState extends ConsumerState<AdminReportsScreen> {
  final searchController = TextEditingController();

  List<PrivateSessionReportModel> reports = [];
  List<UserModel> users = [];
  Map<String, UserModel> usersById = {};

  bool isLoading = true;
  String? errorMessage;
  String selectedFilter = 'all';
  String? loadingConversationReportId;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadReports();
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  Future<void> loadReports() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final adminService = ref.read(adminServiceProvider);
      final loadedUsers = await adminService.getAllUsers();
      final loadedReports = await adminService.getAllPrivateSessionReports();

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        usersById = {for (final user in loadedUsers) user.id: user};
        reports = loadedReports;
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

  List<_ReportedUserSummary> get reportedUserSummaries {
    final groupedReports = <String, List<PrivateSessionReportModel>>{};

    for (final report in reports) {
      groupedReports.putIfAbsent(report.reportedUserId, () => []).add(report);
    }

    final summaries = groupedReports.entries.map((entry) {
      final userReports = [...entry.value]
        ..sort((a, b) {
          final dateA = a.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
          final dateB = b.createdAt ?? DateTime.fromMillisecondsSinceEpoch(0);
          return dateB.compareTo(dateA);
        });

      return _ReportedUserSummary(
        userId: entry.key,
        user: usersById[entry.key],
        reports: userReports,
      );
    }).toList();

    summaries.sort((a, b) {
      if (a.isFlagged != b.isFlagged) return a.isFlagged ? -1 : 1;
      if (a.reportCount != b.reportCount) {
        return b.reportCount.compareTo(a.reportCount);
      }

      final dateA = a.latestReportAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      final dateB = b.latestReportAt ?? DateTime.fromMillisecondsSinceEpoch(0);
      return dateB.compareTo(dateA);
    });

    return summaries;
  }

  List<_ReportedUserSummary> get filteredSummaries {
    final query = searchController.text.trim().toLowerCase();

    return reportedUserSummaries.where((summary) {
      if (selectedFilter == 'threshold' && !summary.thresholdReached) {
        return false;
      }
      if (selectedFilter == 'flagged' && !summary.isFlagged) return false;

      if (query.isEmpty) return true;

      final searchableText = [
        summary.userId,
        summary.displayName,
        summary.email,
        ...summary.reasons,
        ...summary.reasons.map(ReportReasonLabels.label),
        ...summary.reports.map((report) => report.sessionId),
        ...summary.reports.map((report) => report.description),
        ...summary.reports.map((report) => _userLabel(report.reporterUserId)),
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  Future<void> _setInternalFlag(
    _ReportedUserSummary summary,
    bool value,
  ) async {
    final user = summary.user;
    if (user == null) return;

    try {
      final updatedUser = await ref
          .read(adminServiceProvider)
          .updateUserInternalFlag(userId: user.id, internallyFlagged: value);

      if (!mounted) return;

      setState(() {
        users = users
            .map((item) => item.id == updatedUser.id ? updatedUser : item)
            .toList();
        usersById[updatedUser.id] = updatedUser;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(value ? 'User flagged.' : 'User unflagged.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );
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

  void _openReportDetails(_ReportedUserSummary summary) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        return SafeArea(
          child: DraggableScrollableSheet(
            expand: false,
            initialChildSize: 0.75,
            minChildSize: 0.45,
            maxChildSize: 0.95,
            builder: (context, scrollController) {
              return ListView(
                controller: scrollController,
                padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 26,
                        backgroundColor: Colors.redAccent.withOpacity(0.14),
                        child: Text(
                          summary.displayName.isNotEmpty
                              ? summary.displayName[0].toUpperCase()
                              : '?',
                          style: const TextStyle(
                            color: Colors.redAccent,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              summary.displayName,
                              style: const TextStyle(
                                fontSize: 21,
                                fontWeight: FontWeight.w900,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              summary.email.isEmpty
                                  ? _short(summary.userId)
                                  : summary.email,
                              style: const TextStyle(color: Colors.grey),
                            ),
                          ],
                        ),
                      ),
                      _StatusPill(
                        label: '${summary.reportCount} reports',
                        color: summary.thresholdReached
                            ? Colors.orange
                            : AppColors.primaryBlue,
                        icon: Icons.flag_outlined,
                      ),
                    ],
                  ),
                  const SizedBox(height: 18),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: summary.reasons.map((reason) {
                      return _ReasonChip(
                        label: ReportReasonLabels.label(reason),
                      );
                    }).toList(),
                  ),
                  const SizedBox(height: 20),
                  Text(
                    'Report Details',
                    style: TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w900,
                      color: Theme.of(context).brightness == Brightness.dark
                          ? AppColors.textColor
                          : Colors.black87,
                    ),
                  ),
                  const SizedBox(height: 12),
                  ...summary.reports.map(
                    (report) => _ReportDetailCard(
                      report: report,
                      reporterLabel: _userLabel(report.reporterUserId),
                      createdAt: _formatDate(report.createdAt),
                      sessionLabel: _short(report.sessionId),
                      isLoadingConversation:
                          loadingConversationReportId == report.id,
                      onViewConversation: () =>
                          _openConversationForReport(report),
                    ),
                  ),
                ],
              );
            },
          ),
        );
      },
    );
  }

  Future<void> _openConversationForReport(
    PrivateSessionReportModel report,
  ) async {
    setState(() => loadingConversationReportId = report.id);

    try {
      final adminService = ref.read(adminServiceProvider);
      final conversation = await adminService.findConversationBetweenUsers(
        user1Id: report.reporterUserId,
        user2Id: report.reportedUserId,
      );

      if (!mounted) return;

      if (conversation == null || conversation.id.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('No conversation found between these users.'),
            backgroundColor: Colors.orange,
          ),
        );
        return;
      }

      final messages = await adminService.getMessagesByConversationId(
        conversation.id,
      );

      if (!mounted) return;

      _showConversationSheet(report: report, messages: messages);
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    } finally {
      if (mounted) {
        setState(() => loadingConversationReportId = null);
      }
    }
  }

  void _showConversationSheet({
    required PrivateSessionReportModel report,
    required List<MessageModel> messages,
  }) {
    final sortedMessages = [...messages]
      ..sort((a, b) {
        final dateA = a.sentAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        final dateB = b.sentAt ?? DateTime.fromMillisecondsSinceEpoch(0);
        return dateA.compareTo(dateB);
      });

    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        return SafeArea(
          child: DraggableScrollableSheet(
            expand: false,
            initialChildSize: 0.82,
            minChildSize: 0.45,
            maxChildSize: 0.95,
            builder: (context, scrollController) {
              return ListView(
                controller: scrollController,
                padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
                children: [
                  Text(
                    'Conversation Review',
                    style: TextStyle(
                      fontSize: 21,
                      fontWeight: FontWeight.w900,
                      color: Theme.of(context).brightness == Brightness.dark
                          ? AppColors.textColor
                          : Colors.black87,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '${_userLabel(report.reporterUserId)} reported ${_userLabel(report.reportedUserId)}',
                    style: const TextStyle(color: Colors.grey),
                  ),
                  const SizedBox(height: 14),
                  _InfoRow(
                    label: 'Reason',
                    value: ReportReasonLabels.label(report.reasonCode),
                  ),
                  _InfoRow(label: 'Session', value: _short(report.sessionId)),
                  const SizedBox(height: 14),
                  if (sortedMessages.isEmpty)
                    const _AdminReportMessageBox(
                      icon: Icons.chat_bubble_outline,
                      message: 'No messages found in this conversation.',
                      color: Colors.grey,
                    )
                  else
                    ...sortedMessages.map(
                      (message) => _ConversationMessageTile(
                        message: message,
                        senderLabel: _userLabel(message.senderId),
                        isReporter: message.senderId == report.reporterUserId,
                        sentAt: _formatDate(message.sentAt),
                      ),
                    ),
                ],
              );
            },
          ),
        );
      },
    );
  }

  String _userLabel(String userId) {
    final user = usersById[userId];
    if (user == null) return _short(userId);
    if (user.username.isNotEmpty) return user.username;
    if (user.email.isNotEmpty) return user.email;
    return _short(userId);
  }

  String _short(String value) {
    if (value.isEmpty) return '-';
    if (value.length <= 8) return value;
    return '${value.substring(0, 8)}...';
  }

  String _formatDate(DateTime? value) {
    if (value == null) return '-';
    final local = value.toLocal();
    return '${local.year.toString().padLeft(4, '0')}-'
        '${local.month.toString().padLeft(2, '0')}-'
        '${local.day.toString().padLeft(2, '0')} '
        '${local.hour.toString().padLeft(2, '0')}:'
        '${local.minute.toString().padLeft(2, '0')}';
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

  Widget _filterChip({required String label, required String value}) {
    final selected = selectedFilter == value;

    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) {
        setState(() => selectedFilter = value);
      },
      selectedColor: AppColors.primaryBlue.withOpacity(0.18),
      labelStyle: TextStyle(
        color: selected ? AppColors.primaryBlue : null,
        fontWeight: selected ? FontWeight.w800 : FontWeight.normal,
      ),
    );
  }

  Widget _summaryCard(_ReportedUserSummary summary) {
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: Colors.redAccent.withOpacity(0.14),
                  child: Text(
                    summary.displayName.isNotEmpty
                        ? summary.displayName[0].toUpperCase()
                        : '?',
                    style: const TextStyle(
                      color: Colors.redAccent,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        summary.displayName,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        summary.email.isEmpty
                            ? _short(summary.userId)
                            : summary.email,
                        style: const TextStyle(
                          color: Colors.grey,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
                if (summary.isFlagged)
                  _StatusPill(
                    label: 'Flagged',
                    color: Colors.redAccent,
                    icon: Icons.warning_amber_rounded,
                  )
                else if (summary.thresholdReached)
                  _StatusPill(
                    label: '3+ reports',
                    color: Colors.orange,
                    icon: Icons.priority_high_rounded,
                  ),
              ],
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                _CompactMetric(
                  label: 'Reports',
                  value: summary.reportCount.toString(),
                  icon: Icons.flag_outlined,
                  color: Colors.redAccent,
                ),
                const SizedBox(width: 10),
                _CompactMetric(
                  label: 'Reasons',
                  value: summary.reasons.length.toString(),
                  icon: Icons.list_alt_outlined,
                  color: AppColors.primaryBlue,
                ),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: summary.reasons.take(3).map((reason) {
                return _ReasonChip(label: ReportReasonLabels.label(reason));
              }).toList(),
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _openReportDetails(summary),
                    icon: const Icon(Icons.visibility_outlined),
                    label: const Text('View Reports'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.primaryBlue,
                      side: const BorderSide(color: AppColors.primaryBlue),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                  ),
                ),
                if (summary.user != null) ...[
                  const SizedBox(width: 10),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () =>
                          _setInternalFlag(summary, !summary.isFlagged),
                      icon: Icon(
                        summary.isFlagged
                            ? Icons.flag_circle_outlined
                            : Icons.flag_outlined,
                      ),
                      label: Text(
                        summary.isFlagged ? 'Unflag User' : 'Flag User',
                      ),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: summary.isFlagged
                            ? Colors.grey
                            : Colors.redAccent,
                        side: BorderSide(
                          color: summary.isFlagged
                              ? Colors.grey
                              : Colors.redAccent,
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final visibleSummaries = filteredSummaries;
    final flaggedUsers = users.where((user) => user.internallyFlagged).length;
    final thresholdUsers = reportedUserSummaries
        .where((summary) => summary.thresholdReached)
        .length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Report Management'),
        centerTitle: true,
        actions: [
          IconButton(onPressed: loadReports, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: loadReports,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Text(
              'Reported Users',
              style: TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.w900,
                color: isDark ? AppColors.textColor : Colors.black87,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Review flagged users and inspect their individual reports.',
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
                  title: 'Reports',
                  value: reports.length.toString(),
                  icon: Icons.flag_outlined,
                  color: Colors.redAccent,
                ),
                const SizedBox(width: 10),
                _statCard(
                  title: '3+ Reports',
                  value: thresholdUsers.toString(),
                  icon: Icons.priority_high_rounded,
                  color: Colors.orange,
                ),
                const SizedBox(width: 10),
                _statCard(
                  title: 'Flagged',
                  value: flaggedUsers.toString(),
                  icon: Icons.warning_amber_rounded,
                  color: AppColors.primaryBlue,
                ),
              ],
            ),
            const SizedBox(height: 18),
            TextField(
              controller: searchController,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'Search by user, reporter, reason or session...',
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
                _filterChip(label: '3+ Reports', value: 'threshold'),
                _filterChip(label: 'Flagged Users', value: 'flagged'),
              ],
            ),
            const SizedBox(height: 18),
            if (isLoading)
              const Padding(
                padding: EdgeInsets.all(40),
                child: Center(child: CircularProgressIndicator()),
              )
            else if (errorMessage != null)
              _AdminReportMessageBox(
                icon: Icons.error_outline,
                message: errorMessage!,
                color: Colors.redAccent,
                onRetry: loadReports,
              )
            else if (visibleSummaries.isEmpty)
              const _AdminReportMessageBox(
                icon: Icons.inbox_outlined,
                message: 'No reported users found.',
                color: Colors.grey,
              )
            else ...[
              Padding(
                padding: const EdgeInsets.only(bottom: 10),
                child: Text(
                  '${visibleSummaries.length} reported user(s) found',
                  style: TextStyle(
                    color: isDark
                        ? AppColors.subtitleDarkColor
                        : AppColors.subtitleBrightColor,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              ...visibleSummaries.map(_summaryCard),
            ],
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }
}

class _ReportedUserSummary {
  final String userId;
  final UserModel? user;
  final List<PrivateSessionReportModel> reports;

  const _ReportedUserSummary({
    required this.userId,
    required this.user,
    required this.reports,
  });

  int get reportCount => reports.length;
  bool get thresholdReached => reportCount >= 3;
  bool get isFlagged => user?.internallyFlagged == true;
  DateTime? get latestReportAt =>
      reports.isEmpty ? null : reports.first.createdAt;

  String get displayName {
    if (user == null) return 'Unknown User';
    if (user!.username.isNotEmpty) return user!.username;
    if (user!.email.isNotEmpty) return user!.email;
    return 'Unknown User';
  }

  String get email => user?.email ?? '';

  List<String> get reasons {
    final values = reports.map((report) => report.reasonCode).toSet().toList();
    values.sort();
    return values;
  }
}

class _CompactMetric extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color color;

  const _CompactMetric({
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 9),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(14),
        ),
        child: Row(
          children: [
            Icon(icon, color: color, size: 18),
            const SizedBox(width: 8),
            Text(
              value,
              style: TextStyle(color: color, fontWeight: FontWeight.w900),
            ),
            const SizedBox(width: 5),
            Expanded(
              child: Text(
                label,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: color.withOpacity(0.9),
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ReasonChip extends StatelessWidget {
  final String label;

  const _ReasonChip({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.primaryBlue.withOpacity(0.12),
        borderRadius: BorderRadius.circular(30),
      ),
      child: Text(
        label,
        style: const TextStyle(
          color: AppColors.primaryBlue,
          fontSize: 12,
          fontWeight: FontWeight.w800,
        ),
      ),
    );
  }
}

class _ReportDetailCard extends StatelessWidget {
  final PrivateSessionReportModel report;
  final String reporterLabel;
  final String createdAt;
  final String sessionLabel;
  final bool isLoadingConversation;
  final VoidCallback onViewConversation;

  const _ReportDetailCard({
    required this.report,
    required this.reporterLabel,
    required this.createdAt,
    required this.sessionLabel,
    required this.isLoadingConversation,
    required this.onViewConversation,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0.8,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    ReportReasonLabels.label(report.reasonCode),
                    style: const TextStyle(fontWeight: FontWeight.w900),
                  ),
                ),
                Text(
                  createdAt,
                  style: const TextStyle(color: Colors.grey, fontSize: 12),
                ),
              ],
            ),
            const SizedBox(height: 10),
            _InfoRow(label: 'Reporter', value: reporterLabel),
            _InfoRow(label: 'Session', value: sessionLabel),
            if (report.description.trim().isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(report.description, style: const TextStyle(height: 1.35)),
            ],
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton.icon(
                onPressed: isLoadingConversation ? null : onViewConversation,
                icon: isLoadingConversation
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.forum_outlined),
                label: const Text('View Conversation'),
                style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.primaryBlue,
                  side: const BorderSide(color: AppColors.primaryBlue),
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ConversationMessageTile extends StatelessWidget {
  final MessageModel message;
  final String senderLabel;
  final bool isReporter;
  final String sentAt;

  const _ConversationMessageTile({
    required this.message,
    required this.senderLabel,
    required this.isReporter,
    required this.sentAt,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final color = isReporter ? Colors.orange : AppColors.primaryBlue;

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E293B).withOpacity(0.85)
            : Colors.white.withOpacity(0.95),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: color.withOpacity(0.24)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              _StatusPill(
                label: isReporter ? 'Reporter' : 'Reported',
                color: color,
                icon: isReporter
                    ? Icons.person_search_outlined
                    : Icons.person_outline,
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  senderLabel,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
              Text(
                sentAt,
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),
            ],
          ),
          const SizedBox(height: 10),
          SelectableText(
            message.content.isEmpty ? '-' : message.content,
            style: const TextStyle(height: 1.35),
          ),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;

  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        children: [
          SizedBox(
            width: 86,
            child: Text(
              label,
              style: const TextStyle(
                color: Colors.grey,
                fontSize: 12,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          Expanded(
            child: SelectableText(
              value,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusPill extends StatelessWidget {
  final String label;
  final Color color;
  final IconData icon;

  const _StatusPill({
    required this.label,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity(0.12),
        borderRadius: BorderRadius.circular(30),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 15),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: 12,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    );
  }
}

class _AdminReportMessageBox extends StatelessWidget {
  final IconData icon;
  final String message;
  final Color color;
  final VoidCallback? onRetry;

  const _AdminReportMessageBox({
    required this.icon,
    required this.message,
    required this.color,
    this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: color.withOpacity(0.12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          children: [
            Icon(icon, color: color),
            const SizedBox(height: 8),
            Text(message, textAlign: TextAlign.center),
            if (onRetry != null) ...[
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: onRetry,
                child: const Text('Try Again'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
