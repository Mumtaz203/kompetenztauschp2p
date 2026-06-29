import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/match_request_model.dart';
import '../models/user_model.dart';
import '../providers/service_providers.dart';

class AdminSessionsScreen extends ConsumerStatefulWidget {
  const AdminSessionsScreen({super.key});

  @override
  ConsumerState<AdminSessionsScreen> createState() =>
      _AdminSessionsScreenState();
}

class _AdminSessionsScreenState extends ConsumerState<AdminSessionsScreen> {
  final searchController = TextEditingController();
  final sessionIdController = TextEditingController();
  final matchRequestIdController = TextEditingController();

  List<Map<String, dynamic>> sessions = [];
  List<UserModel> users = [];
  List<MatchRequestModel> matchRequests = [];
  Map<String, UserModel> usersById = {};

  bool isLoading = true;
  String? errorMessage;
  String? selectedMatchRequestId;

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
    sessionIdController.dispose();
    matchRequestIdController.dispose();
    super.dispose();
  }

  Future<void> loadInitialData() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final adminService = ref.read(adminServiceProvider);

      final loadedUsers = await adminService.getAllUsers();
      final loadedSessions = await adminService.getAllSessions();
      final loadedRequests =
      await adminService.getAllVisibleMatchRequestsForAdmin(
        users: loadedUsers,
      );

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        usersById = {for (final user in loadedUsers) user.id: user};
        sessions = loadedSessions;
        matchRequests = loadedRequests;
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

  Future<void> loadSessionById() async {
    final sessionId = sessionIdController.text.trim();
    if (sessionId.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final session = await ref.read(adminServiceProvider).getSessionById(sessionId);

      if (!mounted) return;

      setState(() {
        sessions = [session];
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

  Future<void> loadSessionByMatchRequestId() async {
    final matchRequestId = matchRequestIdController.text.trim();
    if (matchRequestId.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final session = await ref
          .read(adminServiceProvider)
          .getSessionByMatchRequestId(matchRequestId);

      if (!mounted) return;

      if (session == null) {
        setState(() {
          sessions = [];
          errorMessage = 'No session found for this match request.';
          isLoading = false;
        });
        return;
      }

      setState(() {
        sessions = [session];
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

  Future<void> createSessionFromSelectedMatchRequest() async {
    final requestId = selectedMatchRequestId;
    if (requestId == null || requestId.isEmpty) return;

    final request = matchRequests.firstWhere(
          (item) => item.id == requestId,
      orElse: () => MatchRequestModel(
        id: '',
        senderId: '',
        receiverId: '',
        status: '',
      ),
    );

    if (request.id.isEmpty) return;

    try {
      final createdSession = await ref.read(adminServiceProvider).createSession(
        matchingRequestId: request.id,
        requesterUserId: request.senderId,
        receiverUserId: request.receiverId,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Session created successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      setState(() {
        sessions = [createdSession, ...sessions];
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

  Future<void> openRatingWindow(String sessionId) async {
    try {
      await ref.read(adminServiceProvider).openRatingWindow(sessionId);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Rating window opened.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadInitialData();
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

  Future<void> expireRatingWindow(String sessionId) async {
    try {
      await ref.read(adminServiceProvider).expireRatingWindow(sessionId);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Rating window expired.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadInitialData();
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

  Future<void> publishRatings(String sessionId) async {
    try {
      await ref.read(adminServiceProvider).publishRatingsForSession(sessionId);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Ratings for this session were published.'),
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

  List<Map<String, dynamic>> get filteredSessions {
    final query = searchController.text.trim().toLowerCase();

    if (query.isEmpty) return sessions;

    return sessions.where((session) {
      final requesterId = _read(session, [
        'requesterUserId',
        'requesterId',
        'senderId',
        'user1Id',
      ]);

      final receiverId = _read(session, [
        'receiverUserId',
        'receiverId',
        'user2Id',
      ]);

      final searchableText = [
        session.values.join(' '),
        _userName(requesterId),
        _userName(receiverId),
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  String _read(Map<String, dynamic> item, List<String> keys) {
    for (final key in keys) {
      final value = item[key];
      if (value != null && value.toString().trim().isNotEmpty) {
        return value.toString();
      }
    }
    return '';
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

  String _formatDateText(String value) {
    if (value.isEmpty) return '-';

    final parsed = DateTime.tryParse(value);
    if (parsed == null) return value;

    final day = parsed.day.toString().padLeft(2, '0');
    final month = parsed.month.toString().padLeft(2, '0');
    final year = parsed.year.toString();
    final hour = parsed.hour.toString().padLeft(2, '0');
    final minute = parsed.minute.toString().padLeft(2, '0');

    return '$day.$month.$year $hour:$minute';
  }

  Color _statusColor(String status) {
    final normalized = status.toUpperCase();

    if (normalized.contains('OPEN')) return AppColors.primaryGreen;
    if (normalized.contains('EXPIRED')) return Colors.redAccent;
    if (normalized.contains('COMPLETED')) return AppColors.primaryBlue;
    if (normalized.contains('CREATED')) return Colors.orange;

    return Colors.grey;
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

  Widget _actionCard({
    required String title,
    required List<Widget> children,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
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

  Widget _matchRequestDropdown() {
    final acceptedRequests = matchRequests
        .where((request) => request.status.toUpperCase() == 'ACCEPTED')
        .toList();

    return DropdownButtonFormField<String>(
      value: selectedMatchRequestId,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: 'Accepted Match Request',
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(14),
        ),
      ),
      items: acceptedRequests.map((request) {
        return DropdownMenuItem<String>(
          value: request.id,
          child: Text(
            '${_userName(request.senderId)} → ${_userName(request.receiverId)}',
            overflow: TextOverflow.ellipsis,
          ),
        );
      }).toList(),
      onChanged: (value) {
        setState(() {
          selectedMatchRequestId = value;
        });
      },
    );
  }

  void _openSessionDetails(Map<String, dynamic> session) {
    final sessionId = _read(session, ['id', 'sessionId']);
    final matchRequestId = _read(session, [
      'matchingRequestId',
      'matchRequestId',
      'requestId',
    ]);

    final requesterId = _read(session, [
      'requesterUserId',
      'requesterId',
      'senderId',
      'user1Id',
    ]);

    final receiverId = _read(session, [
      'receiverUserId',
      'receiverId',
      'user2Id',
    ]);

    final status = _read(session, ['status', 'sessionStatus']);
    final ratingDeadline = _read(session, [
      'ratingDeadline',
      'ratingWindowDeadline',
      'ratingWindowUntil',
      'ratingWindowOpenUntil',
      'ratingEndsAt',
    ]);

    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Session Details',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                    ),
                  ),

                  const SizedBox(height: 16),

                  _detailTitle('Session ID'),
                  SelectableText(sessionId),

                  const SizedBox(height: 14),

                  _detailTitle('Match Request ID'),
                  SelectableText(matchRequestId.isEmpty ? '-' : matchRequestId),

                  const SizedBox(height: 14),

                  _detailTitle('Requester'),
                  SelectableText(
                    '${_userName(requesterId)}\n$requesterId',
                  ),

                  const SizedBox(height: 14),

                  _detailTitle('Receiver'),
                  SelectableText(
                    '${_userName(receiverId)}\n$receiverId',
                  ),

                  const SizedBox(height: 14),

                  _detailTitle('Status'),
                  Text(status.isEmpty ? '-' : status),

                  const SizedBox(height: 14),

                  _detailTitle('Rating deadline'),
                  Text(_formatDateText(ratingDeadline)),

                  const SizedBox(height: 24),

                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          onPressed: sessionId.isEmpty
                              ? null
                              : () {
                            Navigator.pop(sheetContext);
                            openRatingWindow(sessionId);
                          },
                          child: const Text('Open Rating'),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: OutlinedButton(
                          onPressed: sessionId.isEmpty
                              ? null
                              : () {
                            Navigator.pop(sheetContext);
                            expireRatingWindow(sessionId);
                          },
                          child: const Text('Expire Rating'),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 10),

                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: sessionId.isEmpty
                          ? null
                          : () {
                        Navigator.pop(sheetContext);
                        publishRatings(sessionId);
                      },
                      icon: const Icon(Icons.publish_rounded),
                      label: const Text('Publish Ratings For Session'),
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

  Widget _sessionCard(Map<String, dynamic> session) {
    final sessionId = _read(session, ['id', 'sessionId']);
    final matchRequestId = _read(session, [
      'matchingRequestId',
      'matchRequestId',
      'requestId',
    ]);

    final requesterId = _read(session, [
      'requesterUserId',
      'requesterId',
      'senderId',
      'user1Id',
    ]);

    final receiverId = _read(session, [
      'receiverUserId',
      'receiverId',
      'user2Id',
    ]);

    final status = _read(session, ['status', 'sessionStatus']);
    final statusColor = _statusColor(status);

    final ratingDeadline = _read(session, [
      'ratingDeadline',
      'ratingWindowDeadline',
      'ratingWindowUntil',
      'ratingWindowOpenUntil',
      'ratingEndsAt',
    ]);

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => _openSessionDetails(session),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: statusColor.withOpacity(0.14),
                    child: Icon(
                      Icons.event_available_outlined,
                      color: statusColor,
                    ),
                  ),
                  const SizedBox(width: 12),

                  Expanded(
                    child: Text(
                      '${_userName(requesterId)} ↔ ${_userName(receiverId)}',
                      style: const TextStyle(
                        fontWeight: FontWeight.w900,
                        fontSize: 15,
                      ),
                    ),
                  ),

                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 10,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: statusColor.withOpacity(0.12),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      status.isEmpty ? 'SESSION' : status.toUpperCase(),
                      style: TextStyle(
                        color: statusColor,
                        fontSize: 11,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              Text(
                'Session ID: ${_short(sessionId)}',
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),

              const SizedBox(height: 4),

              Text(
                'Match Request: ${_short(matchRequestId)}',
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),

              const SizedBox(height: 4),

              Text(
                'Rating deadline: ${_formatDateText(ratingDeadline)}',
                style: const TextStyle(color: Colors.grey, fontSize: 12),
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
    final visibleSessions = filteredSessions;

    final acceptedRequests = matchRequests
        .where((request) => request.status.toUpperCase() == 'ACCEPTED')
        .length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Session Management'),
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
            'Sessions',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Create sessions from accepted matches and manage rating windows.',
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
                title: 'Sessions',
                value: sessions.length.toString(),
                icon: Icons.event_available_outlined,
                color: AppColors.primaryBlue,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Accepted',
                value: acceptedRequests.toString(),
                icon: Icons.check_circle_outline,
                color: AppColors.primaryGreen,
              ),
            ],
          ),

          const SizedBox(height: 18),

          _actionCard(
            title: 'Create session from accepted match',
            children: [
              _matchRequestDropdown(),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed:
                  isLoading ? null : createSessionFromSelectedMatchRequest,
                  icon: const Icon(Icons.add),
                  label: const Text('Create Session'),
                ),
              ),
            ],
          ),

          _actionCard(
            title: 'Find session by ID',
            children: [
              TextField(
                controller: sessionIdController,
                decoration: InputDecoration(
                  labelText: 'Session ID',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed: isLoading ? null : loadSessionById,
                  icon: const Icon(Icons.search),
                  label: const Text('Load Session'),
                ),
              ),
            ],
          ),

          _actionCard(
            title: 'Find session by match request ID',
            children: [
              TextField(
                controller: matchRequestIdController,
                decoration: InputDecoration(
                  labelText: 'Match Request ID',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed: isLoading ? null : loadSessionByMatchRequestId,
                  icon: const Icon(Icons.sync_alt_rounded),
                  label: const Text('Load By Match Request'),
                ),
              ),
            ],
          ),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search sessions by user, id or status...',
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
          else if (visibleSessions.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(
                    child: Text('No sessions found.'),
                  ),
                ),
              )
            else ...[
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Text(
                    '${visibleSessions.length} session(s) found',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                ...visibleSessions.map(_sessionCard),
              ],

          const SizedBox(height: 20),
        ],
      ),
    );
  }
}