import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/user_model.dart';
import '../providers/service_providers.dart';
import '../services/auth_service.dart';
import '../core/app_colors.dart';

class SearchResultsScreen extends ConsumerStatefulWidget {
  final String skillQuery;

  const SearchResultsScreen({super.key, required this.skillQuery});

  @override
  ConsumerState<SearchResultsScreen> createState() => _SearchResultsScreenState();
}

class _SearchResultsScreenState extends ConsumerState<SearchResultsScreen> {
  late Future<List<UserModel>> searchResults;

  final Set<String> sentRequests = {};
  final Set<String> incomingRequests = {};
  final Set<String> matchedUsers = {};

  @override
  void initState() {
    super.initState();
    searchResults = ref.read(matchingServiceProvider).searchUsersBySkill(widget.skillQuery);
    _loadUserConnections();
  }

  Future<void> _loadUserConnections() async {
    try {
      final currentUserId = await AuthService.getStoredUserId();
      if (currentUserId != null) {
        final requestService = ref.read(matchRequestServiceProvider);
        final outgoing = await requestService.getOutgoingRequests(currentUserId);
        final incoming = await requestService.getIncomingRequests(currentUserId);
        final matches = await requestService.getMatches(currentUserId);

        if (mounted) {
          setState(() {
            for (var req in outgoing) {
              sentRequests.add(req.receiverId);
            }
            for (var req in incoming) {
              incomingRequests.add(req.senderId);
            }
            for (var match in matches) {
              final otherId = match.senderId == currentUserId ? match.receiverId : match.senderId;
              matchedUsers.add(otherId);
            }
          });
        }
      }
    } catch (e) {
      debugPrint("Connections load failed: $e");
    }
  }

  Future<void> _sendMatchRequest(String receiverId) async {
    try {
      final currentUserId = await AuthService.getStoredUserId();
      if (currentUserId == null) throw Exception("User not logged in");

      setState(() {
        sentRequests.add(receiverId);
      });

      await ref.read(matchRequestServiceProvider).sendRequest(currentUserId, receiverId);

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Match request sent successfully!'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );
    } catch (e) {
      final errorMsg = e.toString().toLowerCase();
      if (errorMsg.contains('pending') || errorMsg.contains('already')) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Request is already pending.')),
        );
      } else {
        setState(() {
          sentRequests.remove(receiverId);
        });
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(e.toString().replaceAll('Exception: ', '')),
            backgroundColor: Colors.redAccent,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F172A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
            'Results for "${widget.skillQuery}"',
            style: const TextStyle(color: Colors.white, fontSize: 18)
        ),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: FutureBuilder<List<UserModel>>(
        future: searchResults,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(
              child: CircularProgressIndicator(color: AppColors.primaryBlue),
            );
          } else if (snapshot.hasError) {
            String errorMsg = snapshot.error.toString().replaceAll('Exception: ', '');
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Text(errorMsg, style: const TextStyle(color: Colors.redAccent, fontSize: 16)),
              ),
            );
          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
            return const Center(
              child: Text(
                  'No users found with that skill.',
                  style: TextStyle(color: Colors.white54, fontSize: 16)
              ),
            );
          }

          return FutureBuilder<String?>(
            future: AuthService.getStoredUserId(),
            builder: (context, userIdSnapshot) {
              if (userIdSnapshot.connectionState == ConnectionState.waiting) {
                return const Center(
                  child: CircularProgressIndicator(color: AppColors.primaryBlue),
                );
              }

              final myId = userIdSnapshot.data;

              final users = snapshot.data!
                  .where((user) => user.id != myId)
                  .toList();

              if (users.isEmpty) {
                return const Center(
                  child: Text(
                    'No users found with that skill.',
                    style: TextStyle(color: Colors.white54, fontSize: 16),
                  ),
                );
              }

              return ListView.builder(
                padding: const EdgeInsets.all(16),
                itemCount: users.length,
                itemBuilder: (context, index) {
                  final user = users[index];
                  return _buildResultCard(user);
                },
              );
            },
          );
        },
      ),
    );
  }

  Widget _buildResultCard(UserModel user) {
    String teaches = user.offeredSkills.isNotEmpty ? user.offeredSkills.join(', ') : 'None';
    String wants = user.wantedSkills.isNotEmpty ? user.wantedSkills.join(', ') : 'None';

    bool isMatched = matchedUsers.contains(user.id);
    bool hasSentRequest = sentRequests.contains(user.id);
    bool hasIncoming = incomingRequests.contains(user.id);

    String buttonText = 'Send Request';
    bool isDisabled = false;

    if (isMatched) {
      buttonText = 'Already Matched';
      isDisabled = true;
    } else if (hasSentRequest) {
      buttonText = 'Request Sent';
      isDisabled = true;
    } else if (hasIncoming) {
      buttonText = 'Check Requests Tab';
      isDisabled = true;
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            user.username,
            style: const TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Text('Teaches: $teaches', style: const TextStyle(color: Colors.white70, fontSize: 14)),
          const SizedBox(height: 4),
          Text('Wants to learn: $wants', style: const TextStyle(color: Colors.white70, fontSize: 14)),
          const SizedBox(height: 16),
          Center(
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: isDisabled ? Colors.grey : AppColors.primaryBlue,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
              ),
              onPressed: isDisabled ? null : () => _sendMatchRequest(user.id),
              child: Text(
                buttonText,
                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }
}