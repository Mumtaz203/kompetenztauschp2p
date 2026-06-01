import 'package:flutter/material.dart';
import 'screens/login_screen.dart';
import 'screens/register_screen.dart';
import 'screens/home_screen.dart';
import 'screens/matches_screen.dart';
import 'screens/user_profile_screen.dart';
import 'screens/chat_screen.dart';
import 'screens/my_profile_screen.dart';
import 'screens/welcome_screen.dart';
import 'core/app_colors.dart';
import 'screens/edit_profile_screen.dart';
import 'screens/search_results_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: AppColors.themeNotifier,
      builder: (_, currentMode, __) {
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'SkillSwap',
          theme: AppColors.lightTheme,
          darkTheme: AppColors.darkTheme,
          themeMode: currentMode,
          initialRoute: '/welcome',

          routes: {
            '/welcome': (context) => const WelcomeScreen(),
            '/login': (context) => const LoginScreen(),
            '/register': (context) => const RegisterScreen(),
            '/home': (context) => const HomeScreen(),
            '/matches': (context) => const MatchesScreen(),
            '/user-profile': (context) => const UserProfileScreen(),
            '/chat': (context) {
              final routeArgs = ModalRoute.of(context)?.settings.arguments;
              final args = routeArgs is Map ? routeArgs : const {};

              return ChatScreen(
                conversationId: args['conversationId']?.toString(),
                currentUserId: args['currentUserId']?.toString(),
                otherUserId: args['otherUserId']?.toString(),
                otherUserName: args['otherUserName']?.toString(),
              );
            },
            '/my-profile': (context) => const MyProfileScreen(),
            '/edit-profile': (context) => const EditProfileScreen(),
          },

          onGenerateRoute: (settings) {
            if (settings.name == '/search') {
              final query = settings.arguments as String;
              return MaterialPageRoute(
                builder: (context) => SearchResultsScreen(skillQuery: query),
              );
            }
            return null;
          },
        );
      },
    );
  }
}