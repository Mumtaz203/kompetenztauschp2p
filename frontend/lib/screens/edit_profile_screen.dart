import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';
import '../services/auth_service.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_text_field.dart';

class EditProfileScreen extends ConsumerStatefulWidget {
  const EditProfileScreen({super.key});

  @override
  ConsumerState<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends ConsumerState<EditProfileScreen> {
  UserModel? user;

  final usernameController = TextEditingController();
  final universityController = TextEditingController();
  final newTeachSkillController = TextEditingController();
  final newLearnSkillController = TextEditingController();

  List<String> offeredSkills = [];
  List<String> wantedSkills = [];

  bool isLoading = false;
  bool didFillFields = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();

    if (didFillFields) return;

    final routeUser = ModalRoute.of(context)!.settings.arguments as UserModel;
    user = routeUser;

    usernameController.text = routeUser.username;
    universityController.text = routeUser.university;

    offeredSkills = List<String>.from(routeUser.offeredSkills);
    wantedSkills = List<String>.from(routeUser.wantedSkills);

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
    final university = universityController.text.trim();

    if (username.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Username cannot be empty.')),
      );
      return;
    }

    setState(() => isLoading = true);

    try {
      await ref.read(userServiceProvider).updateMyName(
        userId: user!.id,
        name: username,
      );

      await ref.read(userServiceProvider).updateMyUniversity(
        userId: user!.id,
        university: university,
      );

      final updatedUser = await ref.read(userServiceProvider).updateMySkills(
        userId: user!.id,
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

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Could not update profile: $e')),
      );
    } finally {
      if (mounted) setState(() => isLoading = false);
    }
  }

  void addSkill({
    required TextEditingController controller,
    required bool isOffered,
  }) {
    final skill = controller.text.trim();

    if (skill.isEmpty) return;

    final list = isOffered ? offeredSkills : wantedSkills;

    if (list.any((item) => item.toLowerCase() == skill.toLowerCase())) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('This skill already exists.')),
      );
      return;
    }

    setState(() {
      list.add(skill);
      controller.clear();
    });
  }

  void removeSkill({
    required String skill,
    required bool isOffered,
  }) {
    setState(() {
      if (isOffered) {
        offeredSkills.remove(skill);
      } else {
        wantedSkills.remove(skill);
      }
    });
  }

  @override
  void dispose() {
    usernameController.dispose();
    universityController.dispose();
    newTeachSkillController.dispose();
    newLearnSkillController.dispose();
    super.dispose();
  }

  Widget _buildInputLabel(String text) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 8),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 15,
          fontWeight: FontWeight.w700,
          color: AppColors.textColor,
        ),
      ),
    );
  }

  Widget _buildSkillEditor({
    required String title,
    required IconData icon,
    required List<String> skills,
    required TextEditingController controller,
    required bool isOffered,
  }) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: Colors.black12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: isOffered ? AppColors.primaryBlue : AppColors.primaryGreen),
              const SizedBox(width: 10),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.w900,
                  color: AppColors.textColor,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          if (skills.isEmpty)
            const Text('No skills added yet.')
          else
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: skills.map((skill) {
                return Chip(
                  label: Text(skill),
                  deleteIcon: const Icon(Icons.close, size: 18),
                  onDeleted: () => removeSkill(skill: skill, isOffered: isOffered),
                );
              }).toList(),
            ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: controller,
                  decoration: InputDecoration(
                    hintText: 'Add skill',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  onSubmitted: (_) => addSkill(
                    controller: controller,
                    isOffered: isOffered,
                  ),
                ),
              ),
              const SizedBox(width: 10),
              IconButton.filled(
                onPressed: () => addSkill(
                  controller: controller,
                  isOffered: isOffered,
                ),
                icon: const Icon(Icons.add),
              ),
            ],
          ),
        ],
      ),
    );
  }


  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        Navigator.pushNamedAndRemoveUntil(context, '/my-profile', (route) => false);
      },
      child: Scaffold(
        backgroundColor: AppColors.backgroundColor,
        appBar: AppBar(
          leading: IconButton(
            onPressed: () {
              Navigator.pop(context);
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

                _buildInputLabel('University'),
                CustomTextField(
                  controller: universityController,
                  labelText: '',
                  hintText: 'Example: THWS Würzburg',
                  prefixIcon: Icons.school_outlined,
                ),

                const SizedBox(height: 18),


                _buildSkillEditor(
                  title: 'Skills I can teach',
                  icon: Icons.school_outlined,
                  skills: offeredSkills,
                  controller: newTeachSkillController,
                  isOffered: true,
                ),

                const SizedBox(height: 18),

                _buildSkillEditor(
                  title: 'Skills I want to learn',
                  icon: Icons.auto_awesome_outlined,
                  skills: wantedSkills,
                  controller: newLearnSkillController,
                  isOffered: false,
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
}