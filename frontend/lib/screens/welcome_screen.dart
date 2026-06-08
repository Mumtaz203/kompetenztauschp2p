import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_colors.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_outlined_button.dart';

class WelcomeScreen extends ConsumerWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Stack(
        children: [
          // Lights for dynamic background effects
          // blue one
          Positioned(
            top: -50,
            left: -100,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: AppColors.primaryBlueGradient.withOpacity(0.15),
                boxShadow: [
                  BoxShadow(
                    color: isDark
                        ? AppColors.primaryBlue.withOpacity(0.7)
                        : AppColors.primaryBlue.withOpacity(0.2),
                    blurRadius: 150,
                    spreadRadius: 50,
                  ),
                ],
              ),
            ),
          ),

          // green one
          Positioned(
            bottom: -100,
            right: -200,
            child: Container(
              width: 400,
              height: 400,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: AppColors.primaryGreenGradient.withOpacity(0.2),
                boxShadow: [
                  BoxShadow(
                    color: isDark
                        ? AppColors.primaryGreen.withOpacity(0.5)
                        : AppColors.primaryBlue.withOpacity(0.2),
                    blurRadius: 150,
                    spreadRadius: 50,
                  ),
                ],
              ),
            ),
          ),

          SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 30.0),
              child: Column(
                children: [
                  const Spacer(flex: 2),

                  Image.asset(
                    'assets/images/skillswap_logo.png',
                    height: 100,
                    fit: BoxFit.contain,
                  ),

                  const SizedBox(height: 48),

                  Text(
                    "SkillSwap",
                    style: TextStyle(
                      color: isDark ? AppColors.textColor : Colors.black87,
                      fontSize: 40,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 1.5,
                    ),
                  ),

                  const SizedBox(height: 24),

                  Text(
                    "Exchange skills, learn from others, and grow together.",
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontSize: 18,
                      fontWeight: FontWeight.w700,
                      height: 1.5,
                    ),
                  ),

                  const Spacer(flex: 3),

                  CustomGradientButton(
                    text: "Login",
                    onPressed: () {
                      debugPrint("Login button pressed");
                      Navigator.pushNamed(context, '/login');
                    },
                  ),

                  const SizedBox(height: 16),

                  CustomOutlinedButton(
                    text: "Sign Up",
                    onPressed: () {
                      debugPrint("Sign Up button pressed");
                      Navigator.pushNamed(context, '/register');
                    },
                  ),

                  const Spacer(flex: 1),
                ],
              ),
            ),
          ),

          // theme toggle button at the top right corner
          SafeArea(
            child: Align(
              alignment: Alignment.topRight,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: IconButton(
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
              ),
            ),
          ),
        ],
      ),
    );
  }
}