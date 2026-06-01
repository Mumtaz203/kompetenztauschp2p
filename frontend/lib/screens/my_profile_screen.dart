import 'package:flutter/material.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';
import '../services/user_service.dart';
import '../widgets/app_bottom_nav.dart';

class MyProfileScreen extends StatefulWidget {
  const MyProfileScreen({super.key});

  @override
  State<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends State<MyProfileScreen> {
  UserModel? user;
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    loadProfile();
  }

  Future<void> loadProfile() async {
    final token = await AuthService.getStoredToken();

    if (token == null) {
      setState(() {
        user = null;
        isLoading = false;
        errorMessage = 'No token found. Please login again.';
      });
      return;
    }

    try {
      final loadedUser = await UserService().getMyProfile();

      setState(() {
        user = loadedUser;
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        isLoading = false;
        errorMessage = 'Could not load profile: $e';
      });
    }
  }

  void _onNavTap(BuildContext context, int index) {
    if (index == 2) return;
    if (index == 0) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (index == 1) {
      Navigator.pushReplacementNamed(context, '/matches');
    }
  }

  void _goHome() {
    Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
  }

  @override
  Widget build(BuildContext context) {
    Widget bodyContent;

    if (isLoading) {
      bodyContent = const Center(child: CircularProgressIndicator());
    } else if (errorMessage != null) {
      bodyContent = Center(child: Text(errorMessage!));
    } else {
      bodyContent = ListView(
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
          Center(child: Text(user!.email)),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () async {
              final updatedUser = await Navigator.pushNamed(
                context,
                '/edit-profile',
                arguments: user,
              );

              if (updatedUser is UserModel) {
                setState(() {
                  user = updatedUser;
                });
              }
            },
            child: const Text('Edit Profile'),
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
            children: user!.offeredSkills
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
            children: user!.wantedSkills
                .map((skill) => Chip(label: Text(skill)))
                .toList(),
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
          title: const Text('My Profile'),
          centerTitle: true,
          leading: IconButton(
            onPressed: _goHome,
            icon: const Icon(Icons.arrow_back),
          ),
        ),
        body: bodyContent,
        bottomNavigationBar: AppBottomNav(
          currentIndex: 2,
          onTap: (index) => _onNavTap(context, index),
        ),
      ),
    );
  }
}
