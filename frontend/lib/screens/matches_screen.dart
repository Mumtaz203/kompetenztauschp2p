import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user/user_model.dart';
import '../models/matching/match_request_model.dart';
import '../models/session/session_model.dart';
import '../models/rating/create_rating_request_model.dart';
import '../services/auth_service.dart';
import '../providers/service_providers.dart';
import '../widgets/app_bottom_nav.dart';
import '../core/app_colors.dart';

class MatchesScreen extends ConsumerStatefulWidget {
  const MatchesScreen({super.key});

  @override
  ConsumerState<MatchesScreen> createState() => _MatchesScreenState();
}

class _MatchesScreenState extends ConsumerState<MatchesScreen> {
  bool isLoading = true;
  String? errorMessage;
  String currentUserId = '';

  List<Map<String, dynamic>> requestsData = [];
  List<Map<String, dynamic>> matchesData = [];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadData();
    });
  }

  Future<void> loadData() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final userId = await AuthService.getStoredUserId();
      if (userId == null || userId.isEmpty) {
        throw Exception('User ID not found. Please log in again.');
      }
      currentUserId = userId;

      final matchRequestService = ref.read(matchRequestServiceProvider);
      final userService = ref.read(userServiceProvider);
      final sessionService = ref.read(sessionServiceProvider);

      final incomingRequests = await matchRequestService.getIncomingRequests(currentUserId);
      final acceptedMatches = await matchRequestService.getMatches(currentUserId);

      List<Map<String, dynamic>> tempRequests = [];
      for (var req in incomingRequests) {
        try {
          final user = await userService.getUserProfileById(userId: req.senderId);
          tempRequests.add({'request': req, 'user': user});
        } catch (e) {
          final fallbackUser = UserModel(
            id: req.senderId,
            username: 'Unknown User',
            email: '',
            offeredSkills: ['Hidden'],
            wantedSkills: ['Hidden'],
          );
          tempRequests.add({'request': req, 'user': fallbackUser});
        }
      }

      List<Map<String, dynamic>> tempMatches = [];
      for (var match in acceptedMatches) {
        try {
          final otherId = match.senderId == currentUserId ? match.receiverId : match.senderId;
          final user = await userService.getUserProfileById(userId: otherId);
          final session = await sessionService.getSessionByMatchRequestId(match.id);
          tempMatches.add({'request': match, 'user': user, 'session': session});
        } catch (e) {
          final otherId = match.senderId == currentUserId ? match.receiverId : match.senderId;
          final fallbackUser = UserModel(
            id: otherId,
            username: 'Unknown User',
            email: '',
            offeredSkills: [],
            wantedSkills: [],
          );
          tempMatches.add({'request': match, 'user': fallbackUser});
        }
      }

      if (!mounted) return;
      setState(() {
        requestsData = tempRequests;
        matchesData = tempMatches;
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

  Future<void> _handleAccept(String requestId) async {
    try {
      await ref.read(matchRequestServiceProvider).acceptRequest(requestId, currentUserId);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Request Accepted!'), backgroundColor: AppColors.primaryGreen),
      );
      loadData();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.redAccent),
      );
    }
  }

  Future<void> _handleReject(String requestId) async {
    try {
      await ref.read(matchRequestServiceProvider).rejectRequest(requestId, currentUserId);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Request Rejected.')),
      );
      loadData();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.redAccent),
      );
    }
  }

  Future<void> _showRatingDialog(SessionModel session, UserModel user) async {
    double selectedPoints = 3;
    final commentController = TextEditingController();

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
              title: Text('Rate ${user.username}', textAlign: TextAlign.center),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Text('Select points:', style: TextStyle(fontWeight: FontWeight.w600)),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(5, (index) {
                      return IconButton(
                        icon: Icon(
                          index < selectedPoints ? Icons.star_rounded : Icons.star_outline_rounded,
                          color: Colors.amber,
                          size: 32,
                        ),
                        onPressed: () {
                          setDialogState(() {
                            selectedPoints = index + 1;
                          });
                        },
                      );
                    }),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: commentController,
                    decoration: InputDecoration(
                      hintText: 'Add a comment (optional)',
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                    maxLines: 3,
                  ),
                ],
              ),
              actions: [
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => Navigator.pop(context),
                        style: OutlinedButton.styleFrom(
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                        child: const Text('Cancel'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.primaryGreen,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                          padding: const EdgeInsets.symmetric(vertical: 14),
                        ),
                        onPressed: () async {
                          try {
                            final request = CreateRatingRequestModel(
                              sessionId: session.id,
                              receiverUserId: user.id,
                              points: selectedPoints,
                              comment: commentController.text.isEmpty ? null : commentController.text,
                            );
                            await ref.read(ratingServiceProvider).createRating(request);
                            if (!mounted) return;
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Rating sent!'),
                                backgroundColor: AppColors.primaryGreen,
                              ),
                            );
                          } catch (e) {
                            if (!mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text('Error: $e'), backgroundColor: Colors.redAccent),
                            );
                          }
                        },
                        child: const Text('Send', style: TextStyle(color: Colors.white)),
                      ),
                    ),
                  ],
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _onNavTap(BuildContext context, int index) {
    if (index == 1) return;
    if (index == 0) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (index == 2) {
      Navigator.pushReplacementNamed(context, '/my-profile');
    }
  }

  void _openChat(UserModel user, MatchRequestModel match) {
    if (user.username == 'Unknown User') {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Cannot chat with an unknown user.')),
      );
      return;
    }
    Navigator.pushNamed(
      context,
      '/chat',
      arguments: {
        'currentUserId': currentUserId,
        'otherUserId': user.id,
        'otherUserName': user.username,
        'matchingRequestId': match.id,
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Connections', style: TextStyle(fontWeight: FontWeight.w900)),
          centerTitle: true,
          bottom: const TabBar(
            indicatorColor: AppColors.primaryBlue,
            labelColor: AppColors.primaryBlue,
            unselectedLabelColor: Colors.grey,
            tabs: [
              Tab(text: 'My Matches'),
              Tab(text: 'Requests'),
            ],
          ),
        ),
        body: isLoading
            ? const Center(child: CircularProgressIndicator())
            : errorMessage != null
            ? Center(child: Text(errorMessage!, style: const TextStyle(color: Colors.redAccent)))
            : TabBarView(
          children: [
            _buildMatchesTab(),
            _buildRequestsTab(),
          ],
        ),
        bottomNavigationBar: AppBottomNav(
          currentIndex: 1,
          onTap: (index) => _onNavTap(context, index),
        ),
      ),
    );
  }

  Widget _buildMatchesTab() {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    if (matchesData.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.people_outline, size: 64, color: isDark ? Colors.white24 : Colors.black12),
            const SizedBox(height: 16),
            Text(
              "No matches yet",
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: isDark ? Colors.white54 : Colors.black45,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              "Start connecting with people!",
              style: TextStyle(color: isDark ? Colors.white38 : Colors.black38),
            ),
          ],
        ),
      );
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: matchesData.length,
      itemBuilder: (context, index) {
        final item = matchesData[index];
        final UserModel user = item['user'];
        final SessionModel? session = item['session'];
        final MatchRequestModel match = item['request'];

        return Container(
          margin: const EdgeInsets.only(bottom: 14),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1E293B).withOpacity(0.85) : Colors.white.withOpacity(0.95),
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(isDark ? 0.18 : 0.05),
                blurRadius: 18,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: Row(
            children: [
              CircleAvatar(
                radius: 28,
                backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
                child: Text(
                  user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                  style: const TextStyle(color: AppColors.primaryBlue, fontSize: 20, fontWeight: FontWeight.w900),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      user.username,
                      style: TextStyle(
                        color: isDark ? Colors.white : Colors.black87,
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    if (user.offeredSkills.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text(
                        'Teaches: ${user.offeredSkills.take(2).join(", ")}',
                        style: TextStyle(
                          color: isDark ? Colors.white54 : Colors.black54,
                          fontSize: 13,
                        ),
                      ),
                    ],
                    if (session != null && session.status == 'RATING_OPEN') ...[
                      const SizedBox(height: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.primaryGreen.withOpacity(0.15),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: const Text(
                          '⭐ Rate now',
                          style: TextStyle(color: AppColors.primaryGreen, fontSize: 12, fontWeight: FontWeight.w700),
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Column(
                children: [
                  IconButton(
                    icon: const Icon(Icons.chat_bubble_outline_rounded, color: AppColors.primaryBlue),
                    onPressed: () => _openChat(user, match),
                  ),
                  if (session != null && session.status == 'RATING_OPEN')
                    IconButton(
                      icon: const Icon(Icons.star_rounded, color: AppColors.primaryGreen),
                      onPressed: () => _showRatingDialog(session, user),
                    ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildRequestsTab() {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    if (requestsData.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.inbox_outlined, size: 64, color: isDark ? Colors.white24 : Colors.black12),
            const SizedBox(height: 16),
            Text(
              "No incoming requests",
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: isDark ? Colors.white54 : Colors.black45,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              "Check back later!",
              style: TextStyle(color: isDark ? Colors.white38 : Colors.black38),
            ),
          ],
        ),
      );
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: requestsData.length,
      itemBuilder: (context, index) {
        final item = requestsData[index];
        final MatchRequestModel request = item['request'];
        final UserModel user = item['user'];

        return Container(
          margin: const EdgeInsets.only(bottom: 14),
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1E293B).withOpacity(0.85) : Colors.white.withOpacity(0.95),
            borderRadius: BorderRadius.circular(22),
            border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(isDark ? 0.18 : 0.05),
                blurRadius: 18,
                offset: const Offset(0, 6),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    radius: 26,
                    backgroundColor: AppColors.primaryGreen.withOpacity(0.15),
                    child: Text(
                      user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                      style: const TextStyle(color: AppColors.primaryGreen, fontSize: 18, fontWeight: FontWeight.w900),
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          user.username,
                          style: TextStyle(
                            color: isDark ? Colors.white : Colors.black87,
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                        if (user.wantedSkills.isNotEmpty) ...[
                          const SizedBox(height: 4),
                          Text(
                            'Wants to learn: ${user.wantedSkills.take(2).join(", ")}',
                            style: TextStyle(
                              color: isDark ? Colors.white54 : Colors.black54,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      icon: const Icon(Icons.close_rounded, size: 18),
                      label: const Text('Decline'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.redAccent,
                        side: const BorderSide(color: Colors.redAccent),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                      onPressed: () => _handleReject(request.id),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton.icon(
                      icon: const Icon(Icons.check_rounded, size: 18, color: Colors.white),
                      label: const Text('Accept', style: TextStyle(color: Colors.white)),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryGreen,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                        padding: const EdgeInsets.symmetric(vertical: 12),
                      ),
                      onPressed: () => _handleAccept(request.id),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }
}