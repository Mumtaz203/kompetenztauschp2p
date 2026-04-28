import 'package:flutter/material.dart';
import '../widgets/app_bottom_nav.dart';

class MatchesScreen extends StatelessWidget {
  const MatchesScreen({super.key});

  void _onNavTap(BuildContext context, int index) {
    if (index == 1) return;
    if (index == 0) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (index == 2) {
      Navigator.pushReplacementNamed(context, '/my-profile');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Matches'),
        centerTitle: true,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildMatchCard(context, 'Halime Can', ['Guitar', 'Spanish']),
          _buildMatchCard(context, 'Umut Avci', ['Python', 'Photography']),
          _buildMatchCard(context, 'Yusuf Colak', ['French', 'Photography']),
        ],
      ),
      bottomNavigationBar: AppBottomNav(
        currentIndex: 1,
        onTap: (index) => _onNavTap(context, index),
      ),
    );
  }

  Widget _buildMatchCard(BuildContext context, String name, List<String> skills) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              name,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: skills.map((skill) => Chip(label: Text(skill))).toList(),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () {
                  Navigator.pushNamed(context, '/chat');
                },
                child: const Text('Start Chat'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}