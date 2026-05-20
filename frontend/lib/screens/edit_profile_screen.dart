import 'package:flutter/material.dart';
import '../core/app_colors.dart';
import '../models/user_model.dart';
import '../services/auth_service.dart';
import '../services/user_service.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_text_field.dart';

class EditProfileScreen extends StatefulWidget {
  const EditProfileScreen({super.key});

  @override
  State<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends State<EditProfileScreen> {
  UserModel? user;

  final usernameController = TextEditingController();
  final teachSkillsController = TextEditingController();
  final learnSkillsController = TextEditingController();

  bool isLoading = false;
  bool didFillFields = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();

    if (didFillFields) return;

    final routeUser = ModalRoute.of(context)!.settings.arguments as UserModel;

    user = routeUser;

    usernameController.text = routeUser.username;
    teachSkillsController.text = routeUser.offeredSkills.join(', ');
    learnSkillsController.text = routeUser.wantedSkills.join(', ');

    didFillFields = true;
  }

  Future<void> handleSave() async {
    final token = await AuthService.getStoredToken();

    if (token == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No token found. Please login again.')),
      );
      return;
    }

    final username = usernameController.text.trim();

    final offeredSkills = teachSkillsController.text
        .split(',')
        .map((skill) => skill.trim())
        .where((skill) => skill.isNotEmpty)
        .toList();

    final wantedSkills = learnSkillsController.text
        .split(',')
        .map((skill) => skill.trim())
        .where((skill) => skill.isNotEmpty)
        .toList();

    if (username.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Username cannot be empty.')),
      );
      return;
    }

    setState(() => isLoading = true);

    try {
      final updatedUser = await UserService().updateMyProfile(
        userId: user!.id,
        username: username,
        offeredSkills: offeredSkills,
        wantedSkills: wantedSkills,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Profile updated successfully.')),
      );

      Navigator.pop(context, updatedUser);
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Could not update profile: $e')));
    } finally {
      if (mounted) setState(() => isLoading = false);
    }
  }

  @override
  void dispose() {
    usernameController.dispose();
    teachSkillsController.dispose();
    learnSkillsController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);
      },
      child: Scaffold(
        backgroundColor: AppColors.backgroundColor,
        appBar: AppBar(
          leading: IconButton(
            onPressed: () {
              Navigator.pushNamedAndRemoveUntil(
                context,
                '/home',
                (route) => false,
              );
            },
            icon: const Icon(Icons.arrow_back),
          ),
          title: const Text('Edit Profile'),
          centerTitle: true,
          backgroundColor: AppColors.backgroundColor,
          elevation: 0,
        ),
        body: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 12),

                const CircleAvatar(
                  radius: 42,
                  child: Icon(Icons.person, size: 40),
                ),

                const SizedBox(height: 28),

                _buildInputLabel('Username'),
                CustomTextField(
                  controller: usernameController,
                  labelText: '',
                  hintText: 'Enter your username',
                  prefixIcon: Icons.person_outline,
                ),

                const SizedBox(height: 18),

                _buildInputLabel('Skills I can teach'),
                CustomTextField(
                  controller: teachSkillsController,
                  labelText: '',
                  hintText: 'Example: Python, Math, Design',
                  prefixIcon: Icons.school_outlined,
                ),

                const SizedBox(height: 18),

                _buildInputLabel('Skills I want to learn'),
                CustomTextField(
                  controller: learnSkillsController,
                  labelText: '',
                  hintText: 'Example: Spanish, Photography',
                  prefixIcon: Icons.lightbulb_outline,
                ),

                const SizedBox(height: 28),

                CustomGradientButton(
                  text: 'Save Changes',
                  isLoading: isLoading,
                  onPressed: handleSave,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildInputLabel(String text) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w600,
          color: AppColors.textColor,
        ),
      ),
    );
  }
}
