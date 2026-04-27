import 'package:flutter/material.dart';
import '../widgets/app_bottom_nav.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  void _onNavTap(BuildContext context, int index) {
    if (index == 0) return;
    if (index == 1) {
      Navigator.pushReplacementNamed(context, '/matches');
    } else if (index == 2) {
      Navigator.pushReplacementNamed(context, '/my-profile');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 90,
        centerTitle: true,
        title: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              'KompetenzTausch',
              style: TextStyle(color: Color(0xFF1E053E)),
            ),
            const SizedBox(height: 6),
            ShaderMask(
              shaderCallback: (bounds) => const LinearGradient(
                colors: [
                  Color(0xFF2BA0F3),
                  Color(0xFFBFED4A),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ).createShader(bounds),
              child: const Text(
                '∞',
                style: TextStyle(
                  fontSize: 30,
                  color: Colors.white,
                ),
              ),
            ),
          ],
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          TextField(
            decoration: InputDecoration(
              hintText: 'Search skills or users',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Color(0xFFD7D4DC),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Complete your profile',
                  style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Add your skills to get better matches.',
                ),
                const SizedBox(height: 12),
                ElevatedButton(
                  onPressed: () {
                    Navigator.pushNamed(context, '/my-profile');
                  },
                  child: const Text('Complete Profile'),
                ),
              ],
            ),
          ),
          const SizedBox(height: 24),
          const Text(
            'Suggested Matches',
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(
                0xFF431097)),
          ),
          const SizedBox(height: 12),
          _buildUserCard(
            context,
            name: 'Cihan Can',
            teaches: 'Python',
            learns: 'Photography',
          ),
          _buildUserCard(
            context,
            name: 'Mumtaz Erdogan',
            teaches: 'Math Tutoring',
            learns: 'Design',
          ),
        ],
      ),
      bottomNavigationBar: AppBottomNav(
        currentIndex: 0,
        onTap: (index) => _onNavTap(context, index),
      ),
    );
  }

  Widget _buildUserCard(
      BuildContext context, {
        required String name,
        required String teaches,
        required String learns,
      }) {
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
            const SizedBox(height: 6),
            Text('Teaches: $teaches'),
            Text('Wants to learn: $learns'),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () {
                  Navigator.pushNamed(context, '/user-profile');
                },
                child: const Text('Connect'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}