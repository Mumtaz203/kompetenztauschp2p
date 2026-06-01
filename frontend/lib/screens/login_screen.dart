import 'package:flutter/material.dart';
import '../core/app_colors.dart';
import '../services/auth_service.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_text_field.dart';
import '../widgets/animated_glass_logo.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final TextEditingController emailController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();

  bool rememberMe = false;
  bool isLoading = false;

  Future<void> handleLogin() async {
    final email = emailController.text.trim();
    final password = passwordController.text.trim();

    if (email.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill in all fields.')),
      );
      return;
    }

    setState(() => isLoading = true);

    try {
      final authResponse = await AuthService().login(email: email, password: password);

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Login successful!, welcome back.')),
      );

      Navigator.pushNamedAndRemoveUntil(context, '/home', (route) => false);

    } catch (e) {
      if (!mounted) return;

      String errorMessage = e.toString().replaceAll('Exception: ', '');

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(errorMessage),
          backgroundColor: Colors.redAccent,
        ),
      );
    } finally {
      if (mounted) setState(() => isLoading = false);
    }
  }

  @override
  void dispose() {
    emailController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: IntrinsicHeight(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 30.0, vertical: 20.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            IconButton(
                              padding: EdgeInsets.zero,
                              alignment: Alignment.centerLeft,
                              icon: Icon(Icons.arrow_back_ios,
                                  color: isDark ? Colors.white : Colors.black87),
                              onPressed: () => Navigator.pop(context),
                            ),
                            IconButton(
                              icon: Icon(
                                isDark ? Icons.light_mode : Icons.dark_mode,
                                color: isDark ? Colors.yellow : Colors.blueGrey,
                                size: 30,
                              ),
                              onPressed: () {
                                AppColors.themeNotifier.value =
                                isDark ? ThemeMode.light : ThemeMode.dark;
                              },
                            ),
                          ],
                        ),

                        const SizedBox(height: 20),
                        const AnimatedGlassLogo(),
                        const SizedBox(height: 32),

                        Text(
                          'Welcome Back!',
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
                          'Sign in to continue swapping skills.',
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: 16,
                            color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                            height: 1.5,
                          ),
                        ),

                        const SizedBox(height: 40),

                        CustomTextField(
                          controller: emailController,
                          labelText: 'Email',
                          hintText: 'name@example.com',
                          prefixIcon: Icons.email_outlined,
                          keyboardType: TextInputType.emailAddress,
                        ),

                        const SizedBox(height: 24),

                        CustomTextField(
                          controller: passwordController,
                          labelText: 'Password',
                          hintText: 'Enter your password',
                          prefixIcon: Icons.lock_outline,
                          isPassword: true,
                        ),

                        const SizedBox(height: 16),

                        Row(
                          children: [
                            SizedBox(
                              height: 24,
                              width: 24,
                              child: Checkbox(
                                value: rememberMe,
                                activeColor: AppColors.primaryBlue,
                                checkColor: Colors.white,
                                side: BorderSide(
                                    color: isDark ? AppColors.subtitleDarkColor : Colors.grey),
                                shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(6)),
                                onChanged: (value) => setState(() => rememberMe = value ?? false),
                              ),
                            ),
                            const SizedBox(width: 12),
                            Text(
                              'Remember me',
                              style: TextStyle(
                                color: isDark ? AppColors.textColor : Colors.black87,
                                fontSize: 14,
                              ),
                            ),
                            const Spacer(),
                            TextButton(
                              onPressed: () {},
                              style: TextButton.styleFrom(
                                padding: EdgeInsets.zero,
                                minimumSize: Size.zero,
                                tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                              ),
                              child: const Text(
                                'Forgot password?',
                                style: TextStyle(
                                  color: AppColors.primaryBlue,
                                  fontWeight: FontWeight.w600,
                                  fontSize: 14,
                                ),
                              ),
                            ),
                          ],
                        ),

                        const Spacer(),
                        const SizedBox(height: 32),

                        CustomGradientButton(
                          text: 'Login',
                          isLoading: isLoading,
                          onPressed: handleLogin,
                        ),

                        const SizedBox(height: 24),

                        Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              "Don't have an account? ",
                              style: TextStyle(
                                color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                                fontSize: 14,
                              ),
                            ),
                            GestureDetector(
                              onTap: () => Navigator.pushReplacementNamed(context, '/register'),
                              child: const Text(
                                'Sign Up',
                                style: TextStyle(
                                  color: AppColors.primaryGreen,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 14,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 20),
                      ],
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}