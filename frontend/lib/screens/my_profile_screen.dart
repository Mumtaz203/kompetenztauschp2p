import 'package:flutter/material.dart';
import '../widgets/app_bottom_nav.dart';

class MyProfileScreen extends StatelessWidget {
  const MyProfileScreen({super.key});

  void _onNavTap(BuildContext context, int index) {
    if (index == 2) return;
    if (index == 0) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (index == 1) {
      Navigator.pushReplacementNamed(context, '/matches');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile'),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const CircleAvatar(
            radius: 42,
            child: Icon(Icons.person, size: 40),
          ),
          const SizedBox(height: 16),
          const Center(
            child: Text(
              'Erkin Caliskan',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 6),
          const Center(child: Text('Würzburg, Germany')),
          const SizedBox(height: 16),
          const Text(
            'Computer science student passionate about learning new skills and helping others grow.',
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () {},
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
            children: const [
              Chip(label: Text('Python')),
              Chip(label: Text('Web Development')),
              Chip(label: Text('Math')),
            ],
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
            children: const [
              Chip(label: Text('Spanish')),
              Chip(label: Text('Photography')),
              Chip(label: Text('Design')),
            ],
          ),
        ],
      ),
      bottomNavigationBar: AppBottomNav(
        currentIndex: 2,
        onTap: (index) => _onNavTap(context, index),
      ),
    );
  }
}