import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../core/app_colors.dart';
import '../services/auth_service.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _aiEnabled = false;
  bool _isDarkMode = false;

  @override
  void initState() {
    super.initState();
    _loadPreferences();
  }

  Future<void> _loadPreferences() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    setState(() {
      _aiEnabled = prefs.getBool('ai_enabled_$userId') ?? false;
      _isDarkMode = AppColors.themeNotifier.value == ThemeMode.dark;
    });
  }
  Future<void> _toggleAi(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    await prefs.setBool('ai_enabled_$userId', value);
    setState(() => _aiEnabled = value);
  }

  Future<void> _restartTour() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    await prefs.setBool('hasSeenTour_$userId', false);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Tour will start on your next visit to Home.'),
        backgroundColor: AppColors.primaryGreen,
      ),
    );
  }

  Future<void> _toggleDarkMode(bool value) async {
    AppColors.themeNotifier.value = value ? ThemeMode.dark : ThemeMode.light;
    setState(() => _isDarkMode = value);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        elevation: 0,
        centerTitle: true,
        title: Text(
          'Settings',
          style: TextStyle(
            color: isDark ? AppColors.textColor : Colors.black87,
            fontSize: 24,
            fontWeight: FontWeight.w900,
            letterSpacing: -0.4,
          ),
        ),
        iconTheme: IconThemeData(
          color: isDark ? Colors.white : Colors.black87,
        ),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(24),
          children: [
            _SectionTitle(title: 'Appearance', isDark: isDark),
            const SizedBox(height: 12),
            _SettingsTile(
              isDark: isDark,
              icon: Icons.dark_mode_outlined,
              title: 'Dark Mode',
              subtitle: 'Switch between light and dark theme',
              trailing: Switch(
                value: _isDarkMode,
                onChanged: _toggleDarkMode,
                activeColor: AppColors.primaryBlue,
              ),
            ),
            const SizedBox(height: 24),
            _SectionTitle(title: 'Discovery', isDark: isDark),
            const SizedBox(height: 12),
            _SettingsTile(
              isDark: isDark,
              icon: Icons.auto_awesome_rounded,
              title: 'AI Discovery',
              subtitle: 'Get personalized matches based on your skills',
              trailing: Switch(
                value: _aiEnabled,
                onChanged: _toggleAi,
                activeColor: AppColors.primaryBlue,
              ),
            ),
            const SizedBox(height: 24),
            _SectionTitle(title: 'Onboarding', isDark: isDark),
            const SizedBox(height: 12),
            _SettingsTile(
              isDark: isDark,
              icon: Icons.tour_outlined,
              title: 'Restart App Tour',
              subtitle: 'See the guided tour again on your next Home visit',
              trailing: IconButton(
                icon: const Icon(Icons.refresh_rounded, color: AppColors.primaryBlue),
                onPressed: _restartTour,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String title;
  final bool isDark;

  const _SectionTitle({required this.title, required this.isDark});

  @override
  Widget build(BuildContext context) {
    return Text(
      title,
      style: TextStyle(
        color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
        fontSize: 13,
        fontWeight: FontWeight.w800,
        letterSpacing: 0.5,
      ),
    );
  }
}

class _SettingsTile extends StatelessWidget {
  final bool isDark;
  final IconData icon;
  final String title;
  final String subtitle;
  final Widget trailing;

  const _SettingsTile({
    required this.isDark,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.trailing,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E293B).withOpacity(0.85)
            : Colors.white.withOpacity(0.95),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isDark ? Colors.white12 : Colors.black12,
        ),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: AppColors.primaryBlue.withOpacity(0.12),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: AppColors.primaryBlue, size: 22),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    color: isDark ? AppColors.textColor : Colors.black87,
                    fontSize: 15,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  subtitle,
                  style: TextStyle(
                    color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          trailing,
        ],
      ),
    );
  }
}