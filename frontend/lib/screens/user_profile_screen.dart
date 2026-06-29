import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';
import '../services/auth_service.dart';

class UserProfileScreen extends ConsumerStatefulWidget {
  const UserProfileScreen({super.key});

  @override
  ConsumerState<UserProfileScreen> createState() => _UserProfileScreenState();
}

class _UserProfileScreenState extends ConsumerState<UserProfileScreen> {
  UserModel? user;
  bool isLoading = true;
  bool isCheckingConnection = true;
  String? errorMessage;
  bool didStartLoading = false;

  bool isMatched = false;
  bool hasSentRequest = false;
  bool hasIncomingRequest = false;

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
      final loadedUser = await ref.read(userServiceProvider).getUserProfileById(
        userId: otherUserId,
      );

      if (!mounted) return;
      setState(() {
        user = loadedUser;
        isLoading = false;
      });
      _checkConnectionStatus(otherUserId);
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
      _checkConnectionStatus(otherUserId);
    }
  }

  Future<void> _checkConnectionStatus(String otherId) async {
    try {
      final currentUserId = await AuthService.getStoredUserId();
      if (currentUserId == null) return;

      final requestService = ref.read(matchRequestServiceProvider);
      final matches = await requestService.getMatches(currentUserId);
      final outgoing = await requestService.getOutgoingRequests(currentUserId);
      final incoming = await requestService.getIncomingRequests(currentUserId);

      if (!mounted) return;
      setState(() {
        isMatched = matches.any((m) => m.senderId == otherId || m.receiverId == otherId);
        hasSentRequest = outgoing.any((req) => req.receiverId == otherId);
        hasIncomingRequest = incoming.any((req) => req.senderId == otherId);
        isCheckingConnection = false;
      });
    } catch (e) {
      if (mounted) setState(() => isCheckingConnection = false);
    }
  }

  Future<void> _sendMatchRequest() async {
    if (user == null) return;
    try {
      final currentUserId = await AuthService.getStoredUserId();
      if (currentUserId == null) throw Exception("User not logged in");

      setState(() {
        hasSentRequest = true;
      });

      await ref.read(matchRequestServiceProvider).sendRequest(currentUserId, user!.id);

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Match request sent successfully!'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );
    } catch (e) {
      setState(() {
        hasSentRequest = false;
      });
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  void _openChat() {
    AuthService.getStoredUserId().then((currentUserId) {
      if (!mounted) return;
      if (currentUserId == null ||
          currentUserId.isEmpty ||
          user!.id.isEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Chat cannot be opened.')),
        );
        return;
      }

      Navigator.pushNamed(
        context,
        '/chat',
        arguments: {
          'currentUserId': currentUserId,
          'otherUserId': user!.id,
          'otherUserName': user!.username,
        },
      );
    });
  }

  void _goHome() {
    Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    Widget body;

    if (isLoading) {
      body = const Center(
        child: CircularProgressIndicator(color: AppColors.primaryBlue),
      );
    } else if (errorMessage != null && user == null) {
      body = _ProfileStateMessage(
        isDark: isDark,
        icon: Icons.error_outline_rounded,
        title: 'Profile could not be loaded',
        message: errorMessage!,
        buttonText: 'Try again',
        onPressed: loadUserProfile,
      );
    } else {
      body = ListView(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 18),
        children: [
          _UserHeader(
            user: user!,
            isDark: isDark,
            isCheckingConnection: isCheckingConnection,
            isMatched: isMatched,
            hasSentRequest: hasSentRequest,
            hasIncomingRequest: hasIncomingRequest,
            onMessagePressed: _openChat,
            onSendRequestPressed: _sendMatchRequest,
          ),
          const SizedBox(height: 24),
          _SkillSection(
            title: 'Skills I can teach',
            icon: Icons.school_outlined,
            skills: user!.offeredSkills,
            color: AppColors.primaryBlue,
            isDark: isDark,
          ),
          const SizedBox(height: 18),
          _SkillSection(
            title: 'Skills I want to learn',
            icon: Icons.auto_awesome_outlined,
            skills: user!.wantedSkills,
            color: AppColors.primaryGreen,
            isDark: isDark,
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
            'User Profile',
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
        ),
        body: SafeArea(child: body),
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

class _UserHeader extends StatelessWidget {
  final UserModel user;
  final bool isDark;
  final bool isCheckingConnection;
  final bool isMatched;
  final bool hasSentRequest;
  final bool hasIncomingRequest;
  final VoidCallback onMessagePressed;
  final VoidCallback onSendRequestPressed;

  const _UserHeader({
    required this.user,
    required this.isDark,
    required this.isCheckingConnection,
    required this.isMatched,
    required this.hasSentRequest,
    required this.hasIncomingRequest,
    required this.onMessagePressed,
    required this.onSendRequestPressed,
  });

  @override
  Widget build(BuildContext context) {
    final emailText = user.email.isEmpty ? 'No email available' : user.email;

    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E293B).withOpacity(0.85)
            : Colors.white.withOpacity(0.95),
        borderRadius: BorderRadius.circular(26),
        border: Border.all(
          color: isDark ? Colors.white12 : Colors.black12,
        ),
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
            emailText,
            textAlign: TextAlign.center,
            style: TextStyle(
              color: isDark
                  ? AppColors.subtitleDarkColor
                  : AppColors.subtitleBrightColor,
              fontSize: 14,
              height: 1.4,
            ),
          ),
          const SizedBox(height: 22),
          _buildActionButton(),
        ],
      ),
    );
  }

  Widget _buildActionButton() {
    if (isCheckingConnection) {
      return const Center(
        child: SizedBox(
          width: 24,
          height: 24,
          child: CircularProgressIndicator(strokeWidth: 2),
        ),
      );
    }

    String buttonText;
    VoidCallback? action;
    Color bgColor = AppColors.primaryBlue.withOpacity(0.12);
    Color textColor = AppColors.primaryBlue;

    if (isMatched) {
      buttonText = 'Send Message';
      action = onMessagePressed;
    } else if (hasSentRequest) {
      buttonText = 'Request Sent';
      action = null;
      bgColor = Colors.grey.withOpacity(0.2);
      textColor = Colors.grey;
    } else if (hasIncomingRequest) {
      buttonText = 'Check Requests Tab';
      action = null;
      bgColor = Colors.grey.withOpacity(0.2);
      textColor = Colors.grey;
    } else {
      buttonText = 'Send Match Request';
      action = onSendRequestPressed;
      bgColor = AppColors.primaryGreen.withOpacity(0.15);
      textColor = AppColors.primaryGreen;
    }

    return SizedBox(
      width: double.infinity,
      child: TextButton(
        onPressed: action,
        style: TextButton.styleFrom(
          backgroundColor: bgColor,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          padding: const EdgeInsets.symmetric(vertical: 14),
        ),
        child: Text(
          buttonText,
          style: TextStyle(
            color: textColor,
            fontSize: 15,
            fontWeight: FontWeight.w800,
          ),
        ),
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

  const _SkillSection({
    required this.title,
    required this.icon,
    required this.skills,
    required this.color,
    required this.isDark,
  });

  @override
  Widget build(BuildContext context) {
    final displaySkills = skills.isEmpty ? ['No skills added'] : skills;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: isDark
            ? const Color(0xFF1E293B).withOpacity(0.72)
            : Colors.white.withOpacity(0.9),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(
          color: isDark ? Colors.white12 : Colors.black12,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: color, size: 22),
              const SizedBox(width: 10),
              Text(
                title,
                style: TextStyle(
                  color: isDark ? AppColors.textColor : Colors.black87,
                  fontSize: 18,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: displaySkills
                .map(
                  (skill) => _SkillChip(
                label: skill,
                color: skills.isEmpty ? AppColors.subtitleBrightColor : color,
                isDark: isDark,
              ),
            )
                .toList(),
          ),
        ],
      ),
    );
  }
}

class _SkillChip extends StatelessWidget {
  final String label;
  final Color color;
  final bool isDark;

  const _SkillChip({
    required this.label,
    required this.color,
    required this.isDark,
  });

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
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w800,
        ),
      ),
    );
  }
}

class _ProfileStateMessage extends StatelessWidget {
  final bool isDark;
  final IconData icon;
  final String title;
  final String message;
  final String? buttonText;
  final VoidCallback? onPressed;

  const _ProfileStateMessage({
    required this.isDark,
    required this.icon,
    required this.title,
    required this.message,
    this.buttonText,
    this.onPressed,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: isDark
                ? const Color(0xFF1E293B).withOpacity(0.85)
                : Colors.white.withOpacity(0.95),
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: isDark ? Colors.white12 : Colors.black12,
            ),
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
                  color: isDark
                      ? AppColors.subtitleDarkColor
                      : AppColors.subtitleBrightColor,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
              if (buttonText != null && onPressed != null) ...[
                const SizedBox(height: 18),
                TextButton(
                  onPressed: onPressed,
                  child: Text(
                    buttonText!,
                    style: const TextStyle(
                      color: AppColors.primaryBlue,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}