import 'package:flutter/material.dart';
import 'dart:ui'; // Cam efekti (BackdropFilter) için şart
import '../core/app_colors.dart';

class AnimatedGlassLogo extends StatefulWidget {
  const AnimatedGlassLogo({super.key});

  @override
  State<AnimatedGlassLogo> createState() => _AnimatedGlassLogoState();
}

class _AnimatedGlassLogoState extends State<AnimatedGlassLogo> with TickerProviderStateMixin {
  late AnimationController _floatController;
  late AnimationController _rotateController;
  late Animation<double> _floatAnimation;

  @override
  void initState() {
    super.initState();

    _floatController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 3),
    )..repeat(reverse: true);

    _floatAnimation = Tween<double>(begin: -8, end: 8).animate(
      CurvedAnimation(parent: _floatController, curve: Curves.easeInOut),
    );

    _rotateController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 5),
    )..repeat();
  }

  @override
  void dispose() {
    _floatController.dispose();
    _rotateController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _floatAnimation,
      builder: (context, child) {
        return Transform.translate(
          offset: Offset(0, _floatAnimation.value),
          child: child,
        );
      },
      child: Center(
        child: SizedBox(
          width: 140,
          height: 140,
          child: Stack(
            alignment: Alignment.center,
            children: [
              RotationTransition(
                turns: _rotateController,
                child: Container(
                  width: 150,
                  height: 150,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(40),
                    gradient: SweepGradient(
                      colors: [
                        AppColors.primaryBlue.withOpacity(0.8),
                        AppColors.primaryGreen.withOpacity(0.8),
                        AppColors.primaryBlue.withOpacity(0.8),
                      ],
                    ),
                  ),
                ),
              ),

              ClipRRect(
                borderRadius: BorderRadius.circular(32),
                child: BackdropFilter(
                  filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
                  child: Container(
                    width: 130,
                    height: 130,
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(32),
                      color: Colors.white.withOpacity(0.1),
                      border: Border.all(
                        color: Colors.white.withOpacity(0.2),
                        width: 1.5,
                      ),
                    ),
                   
                    child: Image.asset(
                      'assets/images/skillswap_logo.png',
                      fit: BoxFit.contain,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}