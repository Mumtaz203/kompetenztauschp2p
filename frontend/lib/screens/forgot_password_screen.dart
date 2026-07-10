import 'package:flutter/material.dart';
import '../core/app_colors.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_text_field.dart';

class ForgotPasswordScreen extends StatefulWidget {
  const ForgotPasswordScreen({super.key});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  final TextEditingController usernameController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  bool isLoading = false;

  Future<void> _handleSendResetLink() async {
    final username = usernameController.text.trim();
    final email = emailController.text.trim();

    if (username.isEmpty || email.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill in all fields.')),
      );
      return;
    }

    setState(() => isLoading = true);
    await Future.delayed(const Duration(seconds: 1));
    if (!mounted) return;
    setState(() => isLoading = false);

    showDialog(
      context: context,
      builder: (context) {
        final isDark = Theme.of(context).brightness == Brightness.dark;
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Text('📬 Check your inbox', textAlign: TextAlign.center),
          content: Text(
            'If an account exists for $email, we sent a reset link.',
            textAlign: TextAlign.center,
            style: TextStyle(
              color: isDark ? Colors.white70 : Colors.black54,
            ),
          ),
          actions: [
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () {
                  Navigator.pop(context);
                  Navigator.pop(context);
                },
                style: ElevatedButton.styleFrom(
                  backgroundColor: AppColors.primaryBlue,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                child: const Text('Back to Login', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w700)),
              ),
            ),
          ],
        );
      },
    );
  }

  @override
  void dispose() {
    usernameController.dispose();
    emailController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back_ios, color: isDark ? Colors.white : Colors.black87),
          onPressed: () => Navigator.pop(context),
        ),
        iconTheme: IconThemeData(color: isDark ? Colors.white : Colors.black87),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 20),
              Icon(Icons.lock_reset_rounded, size: 64, color: AppColors.primaryBlue),
              const SizedBox(height: 24),
              Text(
                'Forgot Password?',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.w900,
                  color: isDark ? AppColors.textColor : Colors.black87,
                  letterSpacing: -0.5,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Enter your username and email address to receive a reset link.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 15,
                  color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 40),
              CustomTextField(
                controller: usernameController,
                labelText: 'Username',
                hintText: 'Enter your username',
                prefixIcon: Icons.person_outline,
              ),
              const SizedBox(height: 20),
              CustomTextField(
                controller: emailController,
                labelText: 'Email',
                hintText: 'name@example.com',
                prefixIcon: Icons.email_outlined,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: 32),
              CustomGradientButton(
                text: 'Send Reset Link',
                isLoading: isLoading,
                onPressed: _handleSendResetLink,
              ),
            ],
          ),
        ),
      ),
    );
  }
}