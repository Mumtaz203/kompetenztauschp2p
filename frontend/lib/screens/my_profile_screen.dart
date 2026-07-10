import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';
import '../services/auth_service.dart';
import '../widgets/app_bottom_nav.dart';

class MyProfileScreen extends ConsumerStatefulWidget {
  const MyProfileScreen({super.key});

  @override
  ConsumerState<MyProfileScreen> createState() => _MyProfileScreenState();
}

class _MyProfileScreenState extends ConsumerState<MyProfileScreen> {
  UserModel? user;
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadProfile();
    });
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
      final loadedUser = await ref.read(userServiceProvider).getMyProfile();

      if (!mounted) return;
      setState(() {
        user = loadedUser;
        isLoading = false;
        errorMessage = null;
      });
    } catch (e) {
      if (!mounted) return;
      setState(() {
        isLoading = false;
        errorMessage = 'Could not load profile: $e';
      });
    }
  }

  Future<void> _openEditProfile() async {
    if (user == null) return;

    final updatedUser = await Navigator.pushNamed(
      context,
      '/edit-profile',
      arguments: user,
    );

    if (updatedUser is UserModel && mounted) {
      setState(() {
        user = updatedUser;
      });
    }

    if (mounted) {
      await loadProfile();
    }
  }

  Future<void> _handleLogout() async {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final confirm = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: Text(
            'Log Out',
            style: TextStyle(
              color: isDark ? Colors.white : Colors.black87,
              fontWeight: FontWeight.bold,
            ),
          ),
          content: Text(
            'Are you sure you want to log out of your account?',
            style: TextStyle(color: isDark ? Colors.white70 : Colors.black54),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.redAccent,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: const Text(
                'Log Out',
                style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
              ),
            ),
          ],
        );
      },
    );

    if (confirm == true) {
      try {
        await ref.read(authServiceProvider).logout();
      } catch (e) {
        debugPrint('Logout error: $e');
      }

      if (!mounted) return;
      Navigator.pushNamedAndRemoveUntil(context, '/welcome', (route) => false);
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
    final isDark = Theme.of(context).brightness == Brightness.dark;
    Widget bodyContent;

    if (isLoading) {
      bodyContent = const Center(
        child: CircularProgressIndicator(color: AppColors.primaryBlue),
      );
    } else if (errorMessage != null) {
      bodyContent = _ProfileStateMessage(
        isDark: isDark,
        icon: Icons.error_outline_rounded,
        title: 'Profile could not be loaded',
        message: errorMessage!,
      );
    } else {
      bodyContent = ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 18),
        children: [
          _ProfileHeader(
            user: user!,
            isDark: isDark,
            onEditPressed: _openEditProfile,
          ),
          const SizedBox(height: 24),
          _SkillSection(
            title: 'Skills I can teach',
            icon: Icons.school_outlined,
            skills: user!.offeredSkills,
            color: AppColors.primaryBlue,
            isDark: isDark,
            onTap: _openEditProfile,
          ),
          const SizedBox(height: 18),
          _SkillSection(
            title: 'Skills I want to learn',
            icon: Icons.auto_awesome_outlined,
            skills: user!.wantedSkills,
            color: AppColors.primaryGreen,
            isDark: isDark,
            onTap: _openEditProfile,
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
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        appBar: AppBar(
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
          elevation: 0,
          centerTitle: true,
          iconTheme: IconThemeData(
            color: isDark ? Colors.white : Colors.black87,
          ),
          title: Text(
            'My Profile',
            style: TextStyle(
              color: isDark ? AppColors.textColor : Colors.black87,
              fontSize: 24,
              fontWeight: FontWeight.w900,
              letterSpacing: -0.4,
            ),
          ),
          leading: IconButton(
            onPressed: _goHome,
            icon: const Icon(Icons.arrow_back_ios_new_rounded),
          ),
          actions: [
            IconButton(
              onPressed: () => Navigator.pushNamed(context, '/settings'),
              icon: Icon(
                Icons.settings_outlined,
                color: isDark ? Colors.white : Colors.black87,
              ),
              tooltip: 'Settings',
            ),
            IconButton(
              onPressed: _handleLogout,
              icon: const Icon(Icons.logout_rounded, color: Colors.redAccent),
              tooltip: 'Log Out',
            ),
            const SizedBox(width: 8),
          ],
        ),
        body: SafeArea(child: bodyContent),
        bottomNavigationBar: AppBottomNav(
          currentIndex: 2,
          onTap: (index) => _onNavTap(context, index),
        ),
      ),
    );
  }
}

class _ProfileHeader extends StatelessWidget {
  final UserModel user;
  final bool isDark;
  final VoidCallback onEditPressed;

  const _ProfileHeader({
    required this.user,
    required this.isDark,
    required this.onEditPressed,
  });

  @override
  Widget build(BuildContext context) {
    final hasUniversity = user.university.trim().isNotEmpty;
    final hasRating = user.ratingCount > 0;

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E293B).withOpacity(0.85)
            : Colors.white.withOpacity(0.95),
        borderRadius: BorderRadius.circular(26),
        border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(isDark ? 0.18 : 0.05),
            blurRadius: 22,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        children: [
          CircleAvatar(
            radius: 44,
            backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
            child: Text(
              user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
              style: const TextStyle(
                color: AppColors.primaryBlue,
                fontSize: 34,
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            user.username,
            textAlign: TextAlign.center,
            style: TextStyle(
              color: isDark ? AppColors.textColor : Colors.black87,
              fontSize: 24,
              fontWeight: FontWeight.w900,
              letterSpacing: -0.4,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            user.email,
            textAlign: TextAlign.center,
            style: TextStyle(
              color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
              fontSize: 14,
              height: 1.4,
            ),
          ),
          if (hasUniversity) ...[
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.account_balance_outlined, size: 16, color: AppColors.primaryGreen),
                const SizedBox(width: 6),
                Flexible(
                  child: Text(
                    user.university,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ],
            ),
          ],
          if (hasRating) ...[
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.star_rounded, size: 18, color: Colors.amber),
                const SizedBox(width: 5),
                Text(
                  '${user.averagePoints.toStringAsFixed(1)} (${user.ratingCount} ratings)',
                  style: TextStyle(
                    color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
          ],
          const SizedBox(height: 22),
          SizedBox(
            width: double.infinity,
            child: TextButton(
              onPressed: onEditPressed,
              style: TextButton.styleFrom(
                backgroundColor: AppColors.primaryBlue.withOpacity(0.12),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                padding: const EdgeInsets.symmetric(vertical: 14),
              ),
              child: const Text(
                'Edit Profile',
                style: TextStyle(color: AppColors.primaryBlue, fontSize: 15, fontWeight: FontWeight.w800),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _SkillSection extends StatelessWidget {
  final String title;
  final IconData icon;
  final List<String> skills;
  final Color color;
  final bool isDark;
  final VoidCallback? onTap;

  const _SkillSection({
    required this.title,
    required this.icon,
    required this.skills,
    required this.color,
    required this.isDark,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(22),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1E293B).withOpacity(0.72) : Colors.white.withOpacity(0.9),
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, color: color, size: 22),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    title,
                    style: TextStyle(
                      color: isDark ? AppColors.textColor : Colors.black87,
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                Icon(Icons.edit_outlined, color: color, size: 18),
              ],
            ),
            const SizedBox(height: 6),
            Text(
              'Tap to edit',
              style: TextStyle(
                color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                fontSize: 12,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 14),
            if (skills.isEmpty)
              Text(
                'No skills added yet.',
                style: TextStyle(
                  color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                  fontSize: 14,
                ),
              )
            else
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: skills.map((skill) => _SkillChip(label: skill, color: color, isDark: isDark)).toList(),
              ),
          ],
        ),
      ),
    );
  }
}

class _SkillChip extends StatelessWidget {
  final String label;
  final Color color;
  final bool isDark;

  const _SkillChip({required this.label, required this.color, required this.isDark});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 11, vertical: 7),
      decoration: BoxDecoration(
        color: color.withOpacity(isDark ? 0.16 : 0.12),
        borderRadius: BorderRadius.circular(30),
      ),
      child: Text(
        label,
        style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w800),
      ),
    );
  }
}

class _ProfileStateMessage extends StatelessWidget {
  final bool isDark;
  final IconData icon;
  final String title;
  final String message;

  const _ProfileStateMessage({
    required this.isDark,
    required this.icon,
    required this.title,
    required this.message,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1E293B).withOpacity(0.85) : Colors.white.withOpacity(0.95),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, color: Colors.redAccent, size: 42),
              const SizedBox(height: 14),
              Text(
                title,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: isDark ? AppColors.textColor : Colors.black87,
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                message,
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}