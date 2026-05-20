import 'package:flutter/material.dart';

class AppColors {
  // main colors
  static const Color primaryBlue = Color(0xFF2C6FEF);
  static const Color primaryGreen = Color(0xFF14DF88);
  static const Color backgroundColor = Color(0xFF0F172A);

  static const Color textColor = Colors.white;
  static const Color subtitleDarkColor = Color(0xFF94A3B8);
  static const Color subtitleBrightColor = Colors.black;

  static final ValueNotifier<ThemeMode> themeNotifier = ValueNotifier(ThemeMode.dark);

  //gradient button color
  static LinearGradient get primaryGradient {
    return const LinearGradient(
      begin: Alignment.centerLeft,
      end: Alignment.centerRight,
      colors: [primaryBlue, primaryGreen],
    );
  }

  // gradient for background effects
  static LinearGradient get primaryGreenGradient {
    return const LinearGradient(
      begin: Alignment.centerLeft,
      end: Alignment.center,
      colors: [primaryBlue, primaryGreen],
    );
  }

  static LinearGradient get primaryBlueGradient {
    return const LinearGradient(
      begin: Alignment.center,
      end: Alignment.centerRight,
      colors: [primaryBlue, primaryGreen],
    );
  }


  static final ThemeData darkTheme = ThemeData(
    brightness: Brightness.dark,
    scaffoldBackgroundColor: backgroundColor,
  );

  static final ThemeData lightTheme = ThemeData(
    brightness: Brightness.light,
    scaffoldBackgroundColor: Colors.white,
  );
}