import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';
import '../services/user_service.dart';
import '../widgets/app_bottom_nav.dart';

class MatchesScreen extends StatefulWidget {
  const MatchesScreen({super.key});

  @override
  State<MatchesScreen> createState() => _MatchesScreenState();
}

class _MatchesScreenState extends State<MatchesScreen> {
  final UserService userService = UserService();

  UserModel? currentUser;
  List<UserModel> matches = [];
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    loadMatches();
  }

  Future<void> loadMatches() async {
    final token = await AuthService.getStoredToken();

    if (token == null || token.isEmpty) {
      setState(() {
        isLoading = false;
        errorMessage = 'No token found. Please login again.';
      });
      return;
    }

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final profile = await userService.getMyProfile();
      final Map<String, UserModel> uniqueMatches = {};

      for (final skill in profile.wantedSkills) {
        final normalizedSkill = skill.trim();
        if (normalizedSkill.length < 3) continue;

        final users = await userService.searchUsersBySkill(
          skill: normalizedSkill,
        );

        for (final user in users) {
          if (user.id == profile.id) continue;
          uniqueMatches[user.id] = user;
        }
      }

      if (!mounted) return;
      setState(() {
        currentUser = profile;
        matches = uniqueMatches.values.toList();
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        isLoading = false;
        errorMessage = 'Could not load matches: $e';
      });
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

  void _goHome() {
    Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
  }

  void _openChat(UserModel user) {
    AuthService.getStoredUserId().then((currentUserId) {
      if (!mounted) return;
      final resolvedCurrentUserId = currentUser?.id ?? currentUserId;
      if (resolvedCurrentUserId == null ||
          resolvedCurrentUserId.isEmpty ||
          user.id.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Chat cannot be opened without user ids.'),
          ),
        );
        return;
      }

      Navigator.pushNamed(
        context,
        '/chat',
        arguments: {
          'currentUserId': resolvedCurrentUserId,
          'otherUserId': user.id,
          'otherUserName': user.username,
        },
      );
    });
  }

  void _openProfile(UserModel user) {
    Navigator.pushNamed(
      context,
      '/user-profile',
      arguments: {
        'userId': user.id,
        'username': user.username,
        'email': user.email,
        'offeredSkills': user.offeredSkills,
        'wantedSkills': user.wantedSkills,
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    Widget body;

    if (isLoading) {
      body = const Center(child: CircularProgressIndicator());
    } else if (errorMessage != null) {
      body = Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(errorMessage!, textAlign: TextAlign.center),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: loadMatches,
                child: const Text('Try again'),
              ),
            ],
          ),
        ),
      );
    } else if ((currentUser?.wantedSkills ?? []).isEmpty) {
      body = const Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            'Add skills you want to learn in your profile to find real matches.',
            textAlign: TextAlign.center,
          ),
        ),
      );
    } else if (matches.isEmpty) {
      body = const Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            'No matches found for your wanted skills yet.',
            textAlign: TextAlign.center,
          ),
        ),
      );
    } else {
      body = RefreshIndicator(
        onRefresh: loadMatches,
        child: ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: matches.length,
          itemBuilder: (context, index) {
            return _buildMatchCard(matches[index]);
          },
        ),
      );
    }

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _goHome();
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Matches'),
          centerTitle: true,
          leading: IconButton(
            onPressed: _goHome,
            icon: const Icon(Icons.arrow_back),
          ),
        ),
        body: body,
        bottomNavigationBar: AppBottomNav(
          currentIndex: 1,
          onTap: (index) => _onNavTap(context, index),
        ),
      ),
    );
  }

  Widget _buildMatchCard(UserModel user) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () => _openProfile(user),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                user.username,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: user.offeredSkills
                    .map((skill) => Chip(label: Text(skill)))
                    .toList(),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () => _openChat(user),
                  child: const Text('Start Chat'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
