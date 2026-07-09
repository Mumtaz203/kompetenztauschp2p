import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../providers/auth_provider.dart';
import '../providers/service_providers.dart';

class AdminScreen extends ConsumerStatefulWidget {
  const AdminScreen({super.key});

  @override
  ConsumerState<AdminScreen> createState() => _AdminScreenState();
}

class _AdminScreenState extends ConsumerState<AdminScreen> {
  String userCount = '--';
  String messageCount = '--';
  String reportCount = '--';
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadDashboardStats();
    });
  }

  Future<void> _loadDashboardStats() async {
    if (!mounted) return;
    setState(() => isLoading = true);

    try {
      final adminService = ref.read(adminServiceProvider);

      final users = await adminService.getAllUsers();
      final messages = await adminService.getAllMessages();
      final reports = await adminService.getAllPrivateSessionReports();

      if (mounted) {
        setState(() {
          userCount = users.length.toString();
          messageCount = messages.length.toString();
          reportCount = reports.length.toString();
          isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          userCount = 'Error';
          messageCount = 'Error';
          reportCount = 'Error';
          isLoading = false;
        });
      }
    }
  }

  void _handleLogout(BuildContext context) async {
    await ref.read(authProvider.notifier).logout();
    if (!context.mounted) return;
    Navigator.pushNamedAndRemoveUntil(context, '/welcome', (route) => false);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Admin Dashboard'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.redAccent),
            onPressed: () => _handleLogout(context),
            tooltip: 'Logout',
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _loadDashboardStats,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text(
              'Welcome, Admin',
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w900,
                color: isDark ? AppColors.textColor : Colors.black87,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Manage protected resources and administrative actions.',
              style: TextStyle(
                fontSize: 15,
                color: isDark
                    ? AppColors.subtitleDarkColor
                    : AppColors.subtitleBrightColor,
              ),
            ),

            const SizedBox(height: 24),

            Row(
              children: [
                Expanded(
                  child: _AdminStatCard(
                    title: 'Users',
                    value: isLoading ? '...' : userCount,
                    icon: Icons.people_alt_outlined,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _AdminStatCard(
                    title: 'Messages',
                    value: isLoading ? '...' : messageCount,
                    icon: Icons.message_outlined,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 12),

            const _AdminStatCard(
              title: 'Authorization',
              value: 'ADMIN',
              icon: Icons.verified_user_outlined,
            ),

            const SizedBox(height: 12),

            _AdminStatCard(
              title: 'Private Reports',
              value: isLoading ? '...' : reportCount,
              icon: Icons.flag_outlined,
            ),

            const SizedBox(height: 28),

            Text(
              'Management',
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: isDark ? AppColors.textColor : Colors.black87,
              ),
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.people_alt_outlined,
              title: 'User Management',
              description: 'View users and prepare delete actions.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-users',
                ).then((_) => _loadDashboardStats());
              },
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.chat_bubble_outline,
              title: 'Message Management',
              description: 'View messages and prepare delete actions.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-messages',
                ).then((_) => _loadDashboardStats());
              },
            ),
            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.forum_outlined,
              title: 'Conversation Management',
              description: 'View, inspect and delete conversations.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-conversations',
                ).then((_) => _loadDashboardStats());
              },
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.handshake_outlined,
              title: 'Match Request Management',
              description: 'Update or delete match requests.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-match-requests',
                ).then((_) => _loadDashboardStats());
              },
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.event_available_outlined,
              title: 'Session Management',
              description: 'Create sessions and manage rating windows.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-sessions',
                ).then((_) => _loadDashboardStats());
              },
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.star_rate_outlined,
              title: 'Rating Management',
              description: 'View rating summaries and publish ratings.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-ratings',
                ).then((_) => _loadDashboardStats());
              },
            ),

            const SizedBox(height: 14),

            _AdminNavigationCard(
              icon: Icons.flag_outlined,
              title: 'Report Management',
              description: 'Review private session reports and user flags.',
              onTap: () {
                Navigator.pushNamed(
                  context,
                  '/admin-reports',
                ).then((_) => _loadDashboardStats());
              },
            ),
            const SizedBox(height: 24),
          ],
        ),
      ),
    );
  }
}

class _AdminStatCard extends StatelessWidget {
  final String title;
  final String value;
  final IconData icon;

  const _AdminStatCard({
    required this.title,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1.5,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            CircleAvatar(
              backgroundColor: AppColors.primaryBlue.withOpacity(0.12),
              child: Icon(icon, color: AppColors.primaryBlue),
            ),
            const SizedBox(width: 14),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 21,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 2),
                Text(title),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _AdminNavigationCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String description;
  final VoidCallback onTap;

  const _AdminNavigationCard({
    required this.icon,
    required this.title,
    required this.description,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: InkWell(
        borderRadius: BorderRadius.circular(20),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(
            children: [
              CircleAvatar(
                radius: 26,
                backgroundColor: AppColors.primaryGreen.withOpacity(0.14),
                child: Icon(icon, color: AppColors.primaryGreen),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(description),
                  ],
                ),
              ),
              const Icon(Icons.arrow_forward_ios, size: 17),
            ],
          ),
        ),
      ),
    );
  }
}
