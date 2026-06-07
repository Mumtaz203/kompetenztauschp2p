import 'package:flutter/material.dart';
import '../core/app_colors.dart';
import '../models/user_model.dart';
import '../services/admin_service.dart';

class AdminUsersScreen extends StatefulWidget {
  const AdminUsersScreen({super.key});

  @override
  State<AdminUsersScreen> createState() => _AdminUsersScreenState();
}

class _AdminUsersScreenState extends State<AdminUsersScreen> {
  final AdminService adminService = AdminService();

  List<UserModel> users = [];
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    loadUsers();
  }

  Future<void> loadUsers() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final loadedUsers = await adminService.getAllUsers();

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        errorMessage = e.toString().replaceAll('Exception: ', '');
        isLoading = false;
      });
    }
  }

  Future<void> _showDeleteDialog(BuildContext context, UserModel user) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Delete User'),
          content: Text(
            'Are you sure you want to delete ${user.username}?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (shouldDelete != true) return;

    try {
      await adminService.deleteUser(user.id);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('User deleted successfully.')),
      );

      await loadUsers();
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('User Management'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: loadUsers,
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const _AdminSectionHeader(
            title: 'Users',
            subtitle: 'Users are loaded from the protected backend endpoint.',
          ),
          const SizedBox(height: 16),

          if (isLoading)
            const Padding(
              padding: EdgeInsets.all(40),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (errorMessage != null)
            _AdminErrorBox(
              message: errorMessage!,
              onRetry: loadUsers,
            )
          else if (users.isEmpty)
              const _AdminEmptyBox()
            else
              ...users.map(
                    (user) => _AdminUserCard(
                  name: user.username,
                  email: user.email,
                  role: 'USER',
                  onDelete: () => _showDeleteDialog(context, user),
                ),
              ),

          const SizedBox(height: 20),

        ],
      ),
    );
  }
}

class _AdminSectionHeader extends StatelessWidget {
  final String title;
  final String subtitle;

  const _AdminSectionHeader({
    required this.title,
    required this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: TextStyle(
            fontSize: 25,
            fontWeight: FontWeight.w900,
            color: isDark ? AppColors.textColor : Colors.black87,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          subtitle,
          style: TextStyle(
            color: isDark
                ? AppColors.subtitleDarkColor
                : AppColors.subtitleBrightColor,
          ),
        ),
      ],
    );
  }
}

class _AdminUserCard extends StatelessWidget {
  final String name;
  final String email;
  final String role;
  final VoidCallback onDelete;

  const _AdminUserCard({
    required this.name,
    required this.email,
    required this.role,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final isAdmin = role == 'ADMIN';

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 10,
        ),
        leading: CircleAvatar(
          backgroundColor: isAdmin
              ? AppColors.primaryBlue.withOpacity(0.14)
              : AppColors.primaryGreen.withOpacity(0.14),
          child: Icon(
            isAdmin ? Icons.admin_panel_settings : Icons.person_outline,
            color: isAdmin ? AppColors.primaryBlue : AppColors.primaryGreen,
          ),
        ),
        title: Text(
          name.isEmpty ? 'Unknown User' : name,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        subtitle: Text('$email\nRole: $role'),
        isThreeLine: true,
        trailing: IconButton(
          icon: const Icon(Icons.delete_outline, color: Colors.redAccent),
          onPressed: onDelete,
        ),
      ),
    );
  }
}

class _AdminErrorBox extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;

  const _AdminErrorBox({
    required this.message,
    required this.onRetry,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.redAccent.withOpacity(0.12),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Icon(Icons.error_outline, color: Colors.redAccent),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: onRetry,
              child: const Text('Try Again'),
            ),
          ],
        ),
      ),
    );
  }
}

class _AdminEmptyBox extends StatelessWidget {
  const _AdminEmptyBox();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(24),
        child: Center(
          child: Text('No users found.'),
        ),
      ),
    );
  }
}

