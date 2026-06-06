import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../services/matching_service.dart';
import '../services/auth_service.dart';
import '../core/app_colors.dart';
import 'chat_screen.dart';

class SearchResultsScreen extends StatefulWidget {
  final String skillQuery;

  const SearchResultsScreen({super.key, required this.skillQuery});

  @override
  State<SearchResultsScreen> createState() => _SearchResultsScreenState();
}

class _SearchResultsScreenState extends State<SearchResultsScreen> {
  late Future<List<UserModel>> searchResults;

  @override
  void initState() {
    super.initState();
    searchResults = MatchingService().searchUsersBySkill(widget.skillQuery);
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
          }

          else if (snapshot.hasError) {
            String errorMsg = snapshot.error.toString().replaceAll('Exception: ', '');
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(20.0),
                child: Text(errorMsg, style: const TextStyle(color: Colors.redAccent, fontSize: 16)),
              ),
            );
          }

          else if (!snapshot.hasData || snapshot.data!.isEmpty) {
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
            child: TextButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ChatScreen(
                      otherUserId: user.id,
                      otherUserName: user.username,
                    ),
                  ),
                );
              },
              child: const Text(
                'Connect',
                style: TextStyle(color: Color(0xFFD8B4E2), fontWeight: FontWeight.bold),
              ),
            ),
          ),
        ],
      ),
    );
  }
}