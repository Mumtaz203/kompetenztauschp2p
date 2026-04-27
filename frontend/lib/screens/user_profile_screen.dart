import 'package:flutter/material.dart';

class UserProfileScreen extends StatelessWidget {
  const UserProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('User Profile'),
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
              'Gokce Gencoglu',
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
            ),
          ),
          const SizedBox(height: 6),
          const Center(child: Text('Würzburg, Germany')),
          const SizedBox(height: 20),
          const Text(
            'French literature student. Loves teaching languages and exploring different cultures.',
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
              Chip(label: Text('French')),
              Chip(label: Text('Spanish')),
              Chip(label: Text('Writing')),
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
              Chip(label: Text('Photography')),
              Chip(label: Text('Video Editing')),
              Chip(label: Text('Web Design')),
            ],
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                Navigator.pushNamed(context, '/chat');
              },
              child: const Text('Send Message'),
            ),
          ),
        ],
      ),
    );
  }
}