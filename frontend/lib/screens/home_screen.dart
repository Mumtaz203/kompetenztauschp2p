import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;
import '../core/app_colors.dart';
import '../services/auth_service.dart';
import '../models/user_model.dart';
import '../providers/service_providers.dart';
import '../widgets/custom_gradient_button.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  final TextEditingController searchController = TextEditingController();
  int _selectedIndex = 0;

  late Future<List<UserModel>> _suggestedUsersFuture;

  @override
  void initState() {
    super.initState();
    _suggestedUsersFuture = _fetchSuggestedUsers();
  }

  Future<List<UserModel>> _fetchSuggestedUsers() async {
    try {
      final token = await AuthService.getStoredToken();
      final myId = await AuthService.getStoredUserId();

      if (myId == null) return [];

      final requestService = ref.read(matchRequestServiceProvider);
      final matches = await requestService.getMatches(myId);
      final outgoing = await requestService.getOutgoingRequests(myId);
      final incoming = await requestService.getIncomingRequests(myId);

      Set<String> excludeIds = {myId};
      for (var match in matches) {
        excludeIds.add(match.senderId == myId ? match.receiverId : match.senderId);
      }
      for (var req in outgoing) excludeIds.add(req.receiverId);
      for (var req in incoming) excludeIds.add(req.senderId);

      final response = await http.get(
        Uri.parse('${AuthService.baseUrl}/users/getAllUsers'),
        headers: {
          'Content-Type': 'application/json',
          if (token != null) 'Authorization': 'Bearer $token',
        },
      );

      if (response.statusCode == 200) {
        final decodedData = jsonDecode(response.body);
        List<dynamic> usersList = [];

        if (decodedData is Map && decodedData.containsKey('users')) {
          usersList = decodedData['users'];
        } else if (decodedData is List) {
          usersList = decodedData;
        }

        final allUsers = usersList.map((json) => UserModel.fromJson(json)).toList();

        allUsers.removeWhere((user) => excludeIds.contains(user.id));

        allUsers.shuffle();
        return allUsers.take(3).toList();
      } else {
        throw Exception(
          'Server cancelled: ${response.statusCode} - ${response.body}',
        );
      }
    } catch (e) {
      throw Exception('Error fetching users: $e');
    }
  }

  void _onSearchSubmitted(String query) {
    if (query.trim().isNotEmpty) {
      Navigator.pushNamed(context, '/search', arguments: query.trim());
    }
  }

  void _onItemTapped(int index) {
    if (_selectedIndex == index) return;

    setState(() => _selectedIndex = index);

    if (index == 1) {
      Navigator.pushReplacementNamed(context, '/matches');
    } else if (index == 2) {
      Navigator.pushReplacementNamed(context, '/my-profile');
    }
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Stack(
        children: [
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 18),

                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'SkillSwap',
                        style: TextStyle(
                          color: isDark ? AppColors.textColor : Colors.black87,
                          fontSize: 28,
                          fontWeight: FontWeight.w900,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 8),

                  Text(
                    'Find people, exchange skills, grow together.',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontSize: 15,
                      height: 1.5,
                      fontWeight: FontWeight.w600,
                    ),
                  ),

                  const SizedBox(height: 24),

                  TextField(
                    controller: searchController,
                    style: TextStyle(
                      color: isDark ? Colors.white : Colors.black87,
                    ),
                    onSubmitted: _onSearchSubmitted,
                    decoration: InputDecoration(
                      hintText: 'Search skills or users',
                      hintStyle: TextStyle(
                        color: isDark ? Colors.white54 : Colors.black45,
                        fontSize: 15,
                      ),
                      prefixIcon: Icon(
                        Icons.search_rounded,
                        color: isDark ? Colors.white54 : Colors.black45,
                      ),
                      filled: true,
                      fillColor:
                      isDark ? const Color(0xFF1E293B) : Colors.white,
                      contentPadding:
                      const EdgeInsets.symmetric(vertical: 18),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(18),
                        borderSide: BorderSide(
                          color: isDark ? Colors.white12 : Colors.black12,
                        ),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(18),
                        borderSide: const BorderSide(
                          color: AppColors.primaryBlue,
                          width: 1.8,
                        ),
                      ),
                    ),
                  ),

                  const SizedBox(height: 24),

                  Container(
                    padding: const EdgeInsets.all(22),
                    decoration: BoxDecoration(
                      color: isDark
                          ? const Color(0xFF1E293B).withOpacity(0.85)
                          : Colors.white.withOpacity(0.9),
                      borderRadius: BorderRadius.circular(24),
                      border: Border.all(
                        color: isDark ? Colors.white12 : Colors.black12,
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.primaryBlue.withOpacity(
                            isDark ? 0.12 : 0.08,
                          ),
                          blurRadius: 24,
                          offset: const Offset(0, 10),
                        ),
                      ],
                    ),
                    child: Row(
                      children: [
                        Container(
                          width: 52,
                          height: 52,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: AppColors.primaryBlueGradient,
                          ),
                          child: const Icon(
                            Icons.auto_awesome_rounded,
                            color: Colors.white,
                          ),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Complete your profile',
                                style: TextStyle(
                                  color: isDark
                                      ? AppColors.textColor
                                      : Colors.black87,
                                  fontSize: 17,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'Add your skills to get better matches.',
                                style: TextStyle(
                                  color: isDark
                                      ? AppColors.subtitleDarkColor
                                      : AppColors.subtitleBrightColor,
                                  fontSize: 13,
                                  height: 1.4,
                                ),
                              ),
                            ],
                          ),
                        ),
                        IconButton(
                          onPressed: () {
                            Navigator.pushNamed(context, '/my-profile');
                          },
                          icon: const Icon(Icons.arrow_forward_ios_rounded),
                          color: AppColors.primaryBlue,
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 30),

                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        'Suggested Matches',
                        style: TextStyle(
                          color: isDark ? AppColors.textColor : Colors.black87,
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          letterSpacing: -0.3,
                        ),
                      ),
                      Text(
                        'Top 3',
                        style: TextStyle(
                          color: isDark
                              ? AppColors.subtitleDarkColor
                              : AppColors.subtitleBrightColor,
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 16),

                  Expanded(
                    child: FutureBuilder<List<UserModel>>(
                      future: _suggestedUsersFuture,
                      builder: (context, snapshot) {
                        if (snapshot.connectionState ==
                            ConnectionState.waiting) {
                          return const Center(
                            child: CircularProgressIndicator(),
                          );
                        }

                        if (snapshot.hasError) {
                          return Center(
                            child: Text(
                              'Error: ${snapshot.error}',
                              style: TextStyle(
                                color: isDark ? Colors.white70 : Colors.black54,
                              ),
                              textAlign: TextAlign.center,
                            ),
                          );
                        }

                        if (!snapshot.hasData || snapshot.data!.isEmpty) {
                          return Center(
                            child: Text(
                              'No new suggested matches right now.',
                              style: TextStyle(
                                color: isDark ? Colors.white54 : Colors.black54,
                              ),
                            ),
                          );
                        }

                        final suggestedUsers = snapshot.data!;

                        return ListView.separated(
                          itemCount: suggestedUsers.length,
                          separatorBuilder: (context, index) =>
                          const SizedBox(height: 14),
                          itemBuilder: (context, index) {
                            final user = suggestedUsers[index];

                            return _SuggestedUserCard(
                              user: user,
                              isDark: isDark,
                              onTap: () {
                                Navigator.pushNamed(
                                  context,
                                  '/user-profile',
                                  arguments: {
                                    'userId': user.id,
                                    'username': user.username,
                                    'email': user.email,
                                    'offeredSkills': user.offeredSkills,
                                    'wantedSkills': user.wantedSkills,
                                  },
                                );
                              },
                            );
                          },
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),

      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        unselectedItemColor: isDark ? Colors.white38 : Colors.black38,
        selectedItemColor: AppColors.primaryBlue,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        elevation: 0,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home_filled),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people_outline),
            activeIcon: Icon(Icons.people),
            label: 'Matches',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person_outline),
            activeIcon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}

class _SuggestedUserCard extends StatelessWidget {
  final UserModel user;
  final bool isDark;
  final VoidCallback onTap;

  const _SuggestedUserCard({
    required this.user,
    required this.isDark,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final offeredSkills = user.offeredSkills.take(2).toList();
    final wantedSkills = user.wantedSkills.take(2).toList();

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(22),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: isDark
              ? const Color(0xFF1E293B).withOpacity(0.82)
              : Colors.white.withOpacity(0.95),
          borderRadius: BorderRadius.circular(22),
          border: Border.all(
            color: isDark ? Colors.white12 : Colors.black12,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(isDark ? 0.18 : 0.05),
              blurRadius: 18,
              offset: const Offset(0, 8),
            ),
          ],
        ),
        child: Row(
          children: [
            CircleAvatar(
              radius: 28,
              backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
              child: Text(
                user.username.isNotEmpty
                    ? user.username[0].toUpperCase()
                    : '?',
                style: const TextStyle(
                  color: AppColors.primaryBlue,
                  fontSize: 20,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),

            const SizedBox(width: 16),

            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    user.username,
                    style: TextStyle(
                      color: isDark ? AppColors.textColor : Colors.black87,
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 8),

                  if (offeredSkills.isNotEmpty)
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      children: offeredSkills
                          .map(
                            (skill) => _SkillChip(
                          label: skill,
                          isDark: isDark,
                          isPrimary: true,
                        ),
                      )
                          .toList(),
                    )
                  else
                    Text(
                      'No skills added yet',
                      style: TextStyle(
                        color: isDark
                            ? AppColors.subtitleDarkColor
                            : AppColors.subtitleBrightColor,
                        fontSize: 13,
                      ),
                    ),

                  if (wantedSkills.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      children: wantedSkills
                          .map(
                            (skill) => _SkillChip(
                          label: skill,
                          isDark: isDark,
                          isPrimary: false,
                        ),
                      )
                          .toList(),
                    ),
                  ],
                ],
              ),
            ),

            const SizedBox(width: 8),

            Icon(
              Icons.chevron_right_rounded,
              color: isDark ? Colors.white38 : Colors.black38,
            ),
          ],
        ),
      ),
    );
  }
}

class _SkillChip extends StatelessWidget {
  final String label;
  final bool isDark;
  final bool isPrimary;

  const _SkillChip({
    required this.label,
    required this.isDark,
    required this.isPrimary,
  });

  @override
  Widget build(BuildContext context) {
    final color = isPrimary ? AppColors.primaryBlue : AppColors.primaryGreen;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(isDark ? 0.16 : 0.12),
        borderRadius: BorderRadius.circular(30),
      ),
      child: Text(
        label,
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}