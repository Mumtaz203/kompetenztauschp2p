import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user_model.dart';
import '../models/match_request_model.dart';
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

      final incomingRequests = await matchRequestService.getIncomingRequests(currentUserId);
      final acceptedMatches = await matchRequestService.getMatches(currentUserId);

      List<Map<String, dynamic>> tempRequests = [];
      for (var req in incomingRequests) {
        try {
          final user = await userService.getUserProfileById(userId: req.senderId);
          tempRequests.add({'request': req, 'user': user});
        } catch (e) {
          debugPrint("Failed to load user info for sender: ${req.senderId}, Error: $e");
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
          tempMatches.add({'request': match, 'user': user});
        } catch (e) {
          debugPrint("Failed to load user info for match, Error: $e");
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
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Request Accepted!'), backgroundColor: AppColors.primaryGreen),
      );
      loadData();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.redAccent),
      );
    }
  }

  Future<void> _handleReject(String requestId) async {
    try {
      await ref.read(matchRequestServiceProvider).rejectRequest(requestId, currentUserId);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Request Rejected.')),
      );
      loadData();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.redAccent),
      );
    }
  }

  void _onNavTap(BuildContext context, int index) {
    if (index == 1) return;
    if (index == 0) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (index == 2) {
      Navigator.pushReplacementNamed(context, '/my-profile');
    }
  }

  void _openChat(UserModel user) {
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
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Connections'),
          centerTitle: true,
          bottom: const TabBar(
            indicatorColor: AppColors.primaryBlue,
            labelColor: AppColors.primaryBlue,
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
    if (matchesData.isEmpty) {
      return const Center(child: Text("You don't have any matches yet."));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: matchesData.length,
      itemBuilder: (context, index) {
        final item = matchesData[index];
        final UserModel user = item['user'];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: ListTile(
            leading: const CircleAvatar(child: Icon(Icons.person)),
            title: Text(user.username, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text('Teaches: ${user.offeredSkills.join(", ")}'),
            trailing: IconButton(
              icon: const Icon(Icons.chat_bubble_outline, color: AppColors.primaryBlue),
              onPressed: () => _openChat(user),
            ),
          ),
        );
      },
    );
  }

  Widget _buildRequestsTab() {
    if (requestsData.isEmpty) {
      return const Center(child: Text("No incoming requests at the moment."));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: requestsData.length,
      itemBuilder: (context, index) {
        final item = requestsData[index];
        final MatchRequestModel request = item['request'];
        final UserModel user = item['user'];

        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const CircleAvatar(child: Icon(Icons.person)),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(user.username, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                          Text('Wants to learn: ${user.wantedSkills.join(", ")}', style: const TextStyle(fontSize: 13, color: Colors.grey)),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        style: OutlinedButton.styleFrom(foregroundColor: Colors.red),
                        onPressed: () => _handleReject(request.id),
                        child: const Text('Reject'),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryGreen),
                        onPressed: () => _handleAccept(request.id),
                        child: const Text('Accept'),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}