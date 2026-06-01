import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../core/app_colors.dart';
import '../services/auth_service.dart';
import '../models/user_model.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final TextEditingController searchController = TextEditingController();
  int _selectedIndex = 0;

  // This future will hold the list of suggested users to display on the home screen
  late Future<List<UserModel>> _suggestedUsersFuture;

  @override
  void initState() {
    super.initState();
    // when the screen loads, fetch suggested users from the backend
    _suggestedUsersFuture = _fetchSuggestedUsers();
  }

  Future<List<UserModel>> _fetchSuggestedUsers() async {
    try {
      final token = await AuthService.getStoredToken();

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

        List<UserModel> allUsers = usersList.map((json) => UserModel.fromJson(json)).toList();

        final myId = await AuthService.getStoredUserId();
        if (myId != null) {
          allUsers.removeWhere((user) => user.id == myId);
        }
        allUsers.shuffle();
        return allUsers.take(3).toList();
      } else {
        throw Exception('Server cancelled: ${response.statusCode} - ${response.body}');
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
    switch (index) {
      case 0:
        break;
      case 1:
        Navigator.pushReplacementNamed(context, '/matches');
        break;
      case 2:
        Navigator.pushReplacementNamed(context, '/my-profile');
        break;
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
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 20),

              Center(
                child: Text(
                  'SkillSwap',
                  style: TextStyle(
                    color: isDark ? AppColors.textColor : Colors.black87,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    letterSpacing: -0.5,
                  ),
                ),
              ),
              const SizedBox(height: 12),

              // Logo
              Center(
                child: SizedBox(
                  height: 64,
                  child: Image.asset(
                    'assets/images/skillswap_logo.png',
                    fit: BoxFit.contain,
                  ),
                ),
              ),
              const SizedBox(height: 32),

              TextField(
                controller: searchController,
                style: TextStyle(color: isDark ? Colors.white : Colors.black87),
                onSubmitted: _onSearchSubmitted,
                decoration: InputDecoration(
                  hintText: 'Search skills or users',
                  hintStyle: TextStyle(
                      color: isDark ? Colors.white54 : Colors.black54,
                      fontSize: 15
                  ),
                  prefixIcon: Icon(
                      Icons.search,
                      color: isDark ? Colors.white54 : Colors.black54
                  ),
                  filled: true,
                  fillColor: isDark ? const Color(0xFF1E293B) : Colors.grey.shade100,
                  contentPadding: const EdgeInsets.symmetric(vertical: 16),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide(
                        color: isDark ? Colors.white38 : Colors.grey.shade300,
                        width: 1
                    ),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(
                        color: AppColors.primaryBlue,
                        width: 2
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 24),

              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: isDark ? const Color(0xFF1E293B) : const Color(0xFFE2E8F0),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                      color: isDark ? Colors.white12 : Colors.transparent
                  ),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Complete your profile',
                      style: TextStyle(
                        color: isDark ? AppColors.textColor : Colors.black87,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Add your skills to get better matches.',
                      style: TextStyle(
                        color: isDark ? AppColors.subtitleDarkColor : Colors.black54,
                        fontSize: 14,
                        height: 1.5,
                      ),
                    ),
                    const SizedBox(height: 20),
                    ElevatedButton(
                      onPressed: () {
                        Navigator.pushNamed(context, '/my-profile');
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryBlue,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(24),
                        ),
                        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                        elevation: 0,
                      ),
                      child: const Text(
                        'Complete Profile',
                        style: TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.w600,
                          fontSize: 14,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 32),

              Text(
                'Suggested Matches',
                style: TextStyle(
                  color: isDark ? AppColors.textColor : Colors.black87,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 16),

              Expanded(
                child: FutureBuilder<List<UserModel>>(
                  future: _suggestedUsersFuture,
                  builder: (context, snapshot) {
                    if (snapshot.connectionState == ConnectionState.waiting) {
                      return const Center(child: CircularProgressIndicator());
                    } else if (snapshot.hasError) {
                      return Center(
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Text(
                            'Error: ${snapshot.error}',
                            style: TextStyle(color: isDark ? Colors.white70 : Colors.black54),
                            textAlign: TextAlign.center,
                          ),
                        ),
                      );
                    } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                      return Center(
                        child: Text(
                          'No users found.',
                          style: TextStyle(color: isDark ? Colors.white54 : Colors.black54),
                        ),
                      );
                    }

                    final suggestedUsers = snapshot.data!;

                    return ListView.separated(
                      itemCount: suggestedUsers.length,
                      separatorBuilder: (context, index) => const SizedBox(height: 12),
                      itemBuilder: (context, index) {
                        final user = suggestedUsers[index];
                        return Container(
                          decoration: BoxDecoration(
                            color: isDark ? const Color(0xFF1E293B).withOpacity(0.5) : Colors.grey.shade50,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: isDark ? Colors.white12 : Colors.grey.shade200),
                          ),
                          child: ListTile(
                            leading: CircleAvatar(
                              backgroundColor: AppColors.primaryBlue.withOpacity(0.2),
                              child: Text(
                                user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                                style: const TextStyle(color: AppColors.primaryBlue, fontWeight: FontWeight.bold),
                              ),
                            ),
                            title: Text(
                              user.username,
                              style: TextStyle(
                                color: isDark ? Colors.white : Colors.black87,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            subtitle: Text(
                              user.offeredSkills.isNotEmpty
                                  ? 'Teaches: ${user.offeredSkills.join(", ")}'
                                  : 'No skills added yet',
                              style: TextStyle(color: isDark ? Colors.white54 : Colors.black54, fontSize: 13),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            trailing: const Icon(Icons.chevron_right, color: Colors.grey),
                            onTap: () {
                              Navigator.pushNamed(context, '/user-profile', arguments: {
                                'userId': user.id,
                                'username': user.username,
                                'email': user.email,
                                'offeredSkills': user.offeredSkills,
                                'wantedSkills': user.wantedSkills,
                              });
                            },
                          ),
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

      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        unselectedItemColor: isDark ? Colors.white38 : Colors.black38,
        selectedItemColor: AppColors.primaryBlue,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        elevation: 8,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.home_filled),
            label: 'Home',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people_outline),
            label: 'Matches',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person_outline),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}