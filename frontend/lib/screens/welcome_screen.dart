import 'package:flutter/material.dart';
import '../core/app_colors.dart';
import '../widgets/custom_gradient_button.dart';
import '../widgets/custom_outlined_button.dart';

class WelcomeScreen extends StatelessWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundColor,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [

              Container(
                height: 100,
                width: 100,
                padding: const EdgeInsets.all(2),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: Colors.white,
                  border: Border.all(
                    color: AppColors.primaryBlue.withOpacity(0.2),
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primaryBlue.withOpacity(0.15),
                      blurRadius: 20,
                      offset: const Offset(0, 6),
                    ),
                  ],
                ),
                child: ClipOval(
                  child: Image.asset(
                    'assets/images/skillswap_logo.png',
                    fit: BoxFit.contain,
                  ),
                ),
              ),

              const SizedBox(height: 30),

              const Text(
                'SkillSwap',
                style: TextStyle(
                  fontSize: 30,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textColor,
                  letterSpacing: -0.5,
                ),
              ),

              const SizedBox(height: 10),

              const Text(
                'Exchange skills, learn from others,\nand grow together.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 15,
                  color: AppColors.subtitleColor,
                  height: 1.5,
                ),
              ),

              const SizedBox(height: 50),

              CustomGradientButton(
                text: 'Login',
                onPressed: () {
                  Navigator.pushNamed(context, '/login');
                },
              ),

              const SizedBox(height: 14),

              CustomOutlinedButton(
                text: 'Sign Up',
                onPressed: () {
                  Navigator.pushNamed(context, '/register');
                },
              ),
            ],
          )        ),
      ),
    );
  }
}