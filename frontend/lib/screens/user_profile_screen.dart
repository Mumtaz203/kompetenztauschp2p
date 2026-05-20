import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';
import '../services/user_service.dart';

class UserProfileScreen extends StatefulWidget {
  const UserProfileScreen({super.key});

  @override
  State<UserProfileScreen> createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends State<UserProfileScreen> {
  final UserService userService = UserService();

  UserModel? user;
  bool isLoading = true;
  String? errorMessage;
  bool didStartLoading = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (didStartLoading) return;
    didStartLoading = true;
    loadUserProfile();
  }

  Future<void> loadUserProfile() async {
    final routeArgs = ModalRoute.of(context)?.settings.arguments;
    final args = routeArgs is Map ? routeArgs : const {};
    final otherUserId = args['userId']?.toString() ?? '';
    final token = await AuthService.getStoredToken();

    if (otherUserId.isEmpty) {
      setState(() {
        isLoading = false;
        errorMessage = 'User id is missing.';
      });
      return;
    }

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
      final loadedUser = await userService.getUserProfileById(
        userId: otherUserId,
      );

      if (!mounted) return;
      setState(() {
        user = loadedUser;
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;

      final username = args['username']?.toString() ?? 'User';
      final email = args['email']?.toString() ?? '';
      final offeredSkills = _readSkills(args['offeredSkills']);
      final wantedSkills = _readSkills(args['wantedSkills']);

      setState(() {
        user = UserModel(
          id: otherUserId,
          username: username,
          email: email,
          offeredSkills: offeredSkills,
          wantedSkills: wantedSkills,
        );
        isLoading = false;
        errorMessage = null;
      });
    }
  }

  void _goHome() {
    Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
  }

  @override
  Widget build(BuildContext context) {
    final routeArgs = ModalRoute.of(context)?.settings.arguments;
    final args = routeArgs is Map ? routeArgs : const {};
    final otherUserId = args['userId']?.toString() ?? '';

    Widget body;

    if (isLoading) {
      body = const Center(child: CircularProgressIndicator());
    } else if (errorMessage != null && user == null) {
      body = Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(errorMessage!, textAlign: TextAlign.center),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: loadUserProfile,
                child: const Text('Try again'),
              ),
            ],
          ),
        ),
      );
    } else {
      body = ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const CircleAvatar(radius: 42, child: Icon(Icons.person, size: 40)),
          const SizedBox(height: 16),
          Center(
            child: Text(
              user!.username,
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 6),
          Center(
            child: Text(
              user!.email.isEmpty ? 'No email available' : user!.email,
            ),
          ),
          const SizedBox(height: 24),
          const Text(
            'Skills I can teach',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: user!.offeredSkills.isEmpty
                ? const [Chip(label: Text('No skills added'))]
                : user!.offeredSkills
                      .map((skill) => Chip(label: Text(skill)))
                      .toList(),
          ),
          const SizedBox(height: 20),
          const Text(
            'Skills I want to learn',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: user!.wantedSkills.isEmpty
                ? const [Chip(label: Text('No skills added'))]
                : user!.wantedSkills
                      .map((skill) => Chip(label: Text(skill)))
                      .toList(),
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                AuthService.getStoredUserId().then((currentUserId) {
                  if (!mounted) return;
                  if (currentUserId == null ||
                      currentUserId.isEmpty ||
                      otherUserId.isEmpty) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                        content: Text(
                          'Chat cannot be opened without a real user id.',
                        ),
                      ),
                    );
                    return;
                  }

                  Navigator.pushNamed(
                    context,
                    '/chat',
                    arguments: {
                      'currentUserId': currentUserId,
                      'otherUserId': otherUserId,
                      'otherUserName': user!.username,
                    },
                  );
                });
              },
              child: const Text('Send Message'),
            ),
          ),
        ],
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
          title: const Text('User Profile'),
          leading: IconButton(
            onPressed: _goHome,
            icon: const Icon(Icons.arrow_back),
          ),
        ),
        body: body,
      ),
    );
  }

  List<String> _readSkills(Object? value) {
    if (value is List) {
      return value.map((item) => item.toString()).toList();
    }
    return [];
  }
}
