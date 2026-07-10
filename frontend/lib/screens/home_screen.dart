import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:tutorial_coach_mark/tutorial_coach_mark.dart';
import '../core/app_colors.dart';
import '../services/auth_service.dart';
import '../models/user/user_model.dart';
import '../models/matching/discover_user_model.dart';
import '../providers/service_providers.dart';

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  final TextEditingController searchController = TextEditingController();
  int _selectedIndex = 0;
  bool _aiEnabled = false;

  final GlobalKey _searchKey = GlobalKey();
  final GlobalKey _profileBannerKey = GlobalKey();
  final GlobalKey _listKey = GlobalKey();
  final GlobalKey _navKey = GlobalKey();

  List<UserModel> _classicUsers = [];
  List<DiscoverUserModel> _aiUsers = [];
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _initAiPreference();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAndStartTour();
    });
  }

  Future<void> _checkAndStartTour() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    final hasSeenTour = prefs.getBool('hasSeenTour_$userId') ?? false;
    if (!hasSeenTour && mounted) {
      await Future.delayed(const Duration(milliseconds: 800));
      _showTourDialog();
    }
  }

  Future<void> _showTourDialog() async {
    await showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Text('👋 Welcome to SkillSwap!', textAlign: TextAlign.center),
          content: const Text(
            'Would you like a quick tour of the app?',
            textAlign: TextAlign.center,
          ),
          actions: [
            TextButton(
              onPressed: () async {
                Navigator.pop(context);
                final prefs = await SharedPreferences.getInstance();
                final userId = await AuthService.getStoredUserId() ?? '';
                await prefs.setBool('hasSeenTour_$userId', true);
                if (!mounted) return;
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('You can restart the tour anytime in Settings.'),
                  ),
                );
              },
              child: const Text('Skip'),
            ),
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                _startTour();
              },
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.primaryBlue),
              child: const Text('Start Tour', style: TextStyle(color: Colors.white)),
            ),
          ],
        );
      },
    );
  }

  void _startTour() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    await prefs.setBool('hasSeenTour_$userId', true);

    Widget _tourContent(String emoji, String title, String description) {
      return Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.primaryBlue.withOpacity(0.92),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('$emoji $title', style: const TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.w900)),
            const SizedBox(height: 8),
            Text(description, style: const TextStyle(color: Colors.white, fontSize: 14, height: 1.4)),
          ],
        ),
      );
    }

    final targets = [
      TargetFocus(
        identify: 'search',
        keyTarget: _searchKey,
        contents: [
          TargetContent(
            align: ContentAlign.bottom,
            child: _tourContent('🔍', 'Search', 'Search for skills or users to find your perfect match.'),
          ),
        ],
      ),
      TargetFocus(
        identify: 'profileBanner',
        keyTarget: _profileBannerKey,
        contents: [
          TargetContent(
            align: ContentAlign.bottom,
            child: _tourContent('✨', 'Complete Your Profile', 'Add your skills to get better and more personalized matches.'),
          ),
        ],
      ),
      TargetFocus(
        identify: 'list',
        keyTarget: _listKey,
        contents: [
          TargetContent(
            align: ContentAlign.bottom,
            child: _tourContent('🤝', 'Suggested Matches', 'Here you can see users that match your skills. Tap to view their profile.'),
          ),
        ],
      ),
      TargetFocus(
        identify: 'nav',
        keyTarget: _navKey,
        contents: [
          TargetContent(
            align: ContentAlign.top,
            child: _tourContent('🧭', 'Navigation', 'Use the bottom bar to navigate between Home, Matches and your Profile.'),
          ),
        ],
      ),
    ];

    if (!mounted) return;

    TutorialCoachMark(
      targets: targets,
      colorShadow: Colors.black,
      opacityShadow: 0.8,
      onFinish: () {},
      onSkip: () => true,
    ).show(context: context);
  }

  Future<void> _initAiPreference() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = await AuthService.getStoredUserId() ?? '';
    final isFirstLaunch = prefs.getBool('discover_mode_set_$userId') ?? false;

    if (!isFirstLaunch) {
      if (!mounted) return;
      await _showAiBottomSheet();
    } else {
      _aiEnabled = prefs.getBool('ai_enabled_$userId') ?? false;
      await _loadUsers();
    }
  }

  Future<void> _showAiBottomSheet() async {
    await showModalBottomSheet(
      context: context,
      isDismissible: false,
      enableDrag: false,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
      ),
      builder: (context) {
        final isDark = Theme.of(context).brightness == Brightness.dark;
        return Padding(
          padding: const EdgeInsets.all(28),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: isDark ? Colors.white24 : Colors.black12,
                  borderRadius: BorderRadius.circular(10),
                ),
              ),
              const SizedBox(height: 24),
              const Icon(Icons.auto_awesome_rounded, size: 48, color: AppColors.primaryBlue),
              const SizedBox(height: 16),
              Text(
                'How do you want to discover?',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: isDark ? Colors.white : Colors.black87),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 10),
              Text(
                'AI mode finds the best matches based on your skills. You can change this anytime in your profile.',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 14, color: isDark ? Colors.white54 : Colors.black54, height: 1.5),
              ),
              const SizedBox(height: 28),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => Navigator.pop(context, false),
                      style: OutlinedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      ),
                      child: const Text('Classic', style: TextStyle(fontWeight: FontWeight.w700)),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () => Navigator.pop(context, true),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: AppColors.primaryBlue,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                      ),
                      child: const Text('Enable AI', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w700)),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
            ],
          ),
        );
      },
    ).then((value) async {
      final prefs = await SharedPreferences.getInstance();
      final userId = await AuthService.getStoredUserId() ?? '';
      await prefs.setBool('discover_mode_set_$userId', true);
      await prefs.setBool('ai_enabled_$userId', value ?? false);
      _aiEnabled = value ?? false;
      await _loadUsers();
    });
  }

  Future<void> _loadUsers() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final myId = await AuthService.getStoredUserId();
      if (myId == null) throw Exception('User not found');

      if (_aiEnabled) {
        final users = await ref.read(matchingServiceProvider).discoverUsers(myId);
        if (!mounted) return;
        setState(() {
          _aiUsers = users;
          _isLoading = false;
        });
      } else {
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

        final users = await ref.read(matchingServiceProvider).getRandom10Users();
        users.removeWhere((user) => excludeIds.contains(user.id));

        if (!mounted) return;
        setState(() {
          _classicUsers = users;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (!mounted) return;
      setState(() {
        _error = e.toString().replaceAll('Exception: ', '');
        _isLoading = false;
      });
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
      body: SafeArea(
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
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: _aiEnabled ? AppColors.primaryBlue.withOpacity(0.15) : (isDark ? Colors.white12 : Colors.black12),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      children: [
                        Icon(Icons.auto_awesome_rounded, size: 16,
                            color: _aiEnabled ? AppColors.primaryBlue : (isDark ? Colors.white38 : Colors.black38)),
                        const SizedBox(width: 6),
                        Text(
                          _aiEnabled ? 'AI On' : 'AI Off',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w700,
                            color: _aiEnabled ? AppColors.primaryBlue : (isDark ? Colors.white38 : Colors.black38),
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'Find people, exchange skills, grow together.',
                style: TextStyle(
                  color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor,
                  fontSize: 15,
                  height: 1.5,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 24),
              TextField(
                key: _searchKey,
                controller: searchController,
                style: TextStyle(color: isDark ? Colors.white : Colors.black87),
                onSubmitted: _onSearchSubmitted,
                decoration: InputDecoration(
                  hintText: 'Search skills or users',
                  hintStyle: TextStyle(color: isDark ? Colors.white54 : Colors.black45, fontSize: 15),
                  prefixIcon: Icon(Icons.search_rounded, color: isDark ? Colors.white54 : Colors.black45),
                  filled: true,
                  fillColor: isDark ? const Color(0xFF1E293B) : Colors.white,
                  contentPadding: const EdgeInsets.symmetric(vertical: 18),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(18),
                    borderSide: BorderSide(color: isDark ? Colors.white12 : Colors.black12),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(18),
                    borderSide: const BorderSide(color: AppColors.primaryBlue, width: 1.8),
                  ),
                ),
              ),
              const SizedBox(height: 24),
              Container(
                key: _profileBannerKey,
                padding: const EdgeInsets.all(22),
                decoration: BoxDecoration(
                  color: isDark ? const Color(0xFF1E293B).withOpacity(0.85) : Colors.white.withOpacity(0.9),
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
                  boxShadow: [BoxShadow(color: AppColors.primaryBlue.withOpacity(isDark ? 0.12 : 0.08), blurRadius: 24, offset: const Offset(0, 10))],
                ),
                child: Row(
                  children: [
                    Container(
                      width: 52,
                      height: 52,
                      decoration: BoxDecoration(shape: BoxShape.circle, gradient: AppColors.primaryBlueGradient),
                      child: const Icon(Icons.auto_awesome_rounded, color: Colors.white),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Complete your profile',
                              style: TextStyle(color: isDark ? AppColors.textColor : Colors.black87, fontSize: 17, fontWeight: FontWeight.w800)),
                          const SizedBox(height: 6),
                          Text('Add your skills to get better matches.',
                              style: TextStyle(color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor, fontSize: 13, height: 1.4)),
                        ],
                      ),
                    ),
                    IconButton(
                      onPressed: () => Navigator.pushNamed(context, '/my-profile'),
                      icon: const Icon(Icons.arrow_forward_ios_rounded),
                      color: AppColors.primaryBlue,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 30),
              Row(
                key: _listKey,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    _aiEnabled ? 'AI Recommendations' : 'Suggested Matches',
                    style: TextStyle(color: isDark ? AppColors.textColor : Colors.black87, fontSize: 20, fontWeight: FontWeight.w900, letterSpacing: -0.3),
                  ),
                  Text(
                    _aiEnabled ? 'Personalized' : 'Random',
                    style: TextStyle(color: isDark ? AppColors.subtitleDarkColor : AppColors.subtitleBrightColor, fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Expanded(
                child: _isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : _error != null
                    ? Center(child: Text(_error!, style: const TextStyle(color: Colors.redAccent)))
                    : _aiEnabled
                    ? _buildAiList(isDark)
                    : _buildClassicList(isDark),
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: BottomNavigationBar(
        key: _navKey,
        currentIndex: _selectedIndex,
        onTap: _onItemTapped,
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        unselectedItemColor: isDark ? Colors.white38 : Colors.black38,
        selectedItemColor: AppColors.primaryBlue,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        elevation: 0,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home_filled), label: 'Home'),
          BottomNavigationBarItem(icon: Icon(Icons.people_outline), activeIcon: Icon(Icons.people), label: 'Matches'),
          BottomNavigationBarItem(icon: Icon(Icons.person_outline), activeIcon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }

  Widget _buildClassicList(bool isDark) {
    if (_classicUsers.isEmpty) {
      return Center(child: Text('No suggested matches right now.', style: TextStyle(color: isDark ? Colors.white54 : Colors.black54)));
    }
    return ListView.separated(
      itemCount: _classicUsers.length,
      separatorBuilder: (_, __) => const SizedBox(height: 14),
      itemBuilder: (context, index) {
        final user = _classicUsers[index];
        return _ClassicUserCard(user: user, isDark: isDark, onTap: () {
          Navigator.pushNamed(context, '/user-profile', arguments: {
            'userId': user.id,
            'username': user.username,
            'email': user.email,
            'offeredSkills': user.offeredSkills,
            'wantedSkills': user.wantedSkills,
          });
        });
      },
    );
  }

  Widget _buildAiList(bool isDark) {
    if (_aiUsers.isEmpty) {
      return Center(child: Text('No AI recommendations right now.', style: TextStyle(color: isDark ? Colors.white54 : Colors.black54)));
    }
    return ListView.separated(
      itemCount: _aiUsers.length,
      separatorBuilder: (_, __) => const SizedBox(height: 14),
      itemBuilder: (context, index) {
        final user = _aiUsers[index];
        return _AiUserCard(user: user, isDark: isDark, onTap: () {
          Navigator.pushNamed(context, '/user-profile', arguments: {
            'userId': user.userId,
            'username': user.username,
            'email': '',
            'offeredSkills': user.matchedSkills,
            'wantedSkills': [],
          });
        });
      },
    );
  }
}

class _ClassicUserCard extends StatelessWidget {
  final UserModel user;
  final bool isDark;
  final VoidCallback onTap;

  const _ClassicUserCard({required this.user, required this.isDark, required this.onTap});

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
          color: isDark ? const Color(0xFF1E293B).withOpacity(0.82) : Colors.white.withOpacity(0.95),
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: isDark ? Colors.white12 : Colors.black12),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(isDark ? 0.18 : 0.05), blurRadius: 18, offset: const Offset(0, 8))],
        ),
        child: Row(
          children: [
            CircleAvatar(
              radius: 28,
              backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
              child: Text(user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                  style: const TextStyle(color: AppColors.primaryBlue, fontSize: 20, fontWeight: FontWeight.w900)),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(user.username, style: TextStyle(color: isDark ? AppColors.textColor : Colors.black87, fontSize: 16, fontWeight: FontWeight.w800)),
                  const SizedBox(height: 8),
                  if (offeredSkills.isNotEmpty)
                    Wrap(spacing: 6, runSpacing: 6, children: offeredSkills.map((s) => _SkillChip(label: s, isDark: isDark, isPrimary: true)).toList()),
                  if (wantedSkills.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Wrap(spacing: 6, runSpacing: 6, children: wantedSkills.map((s) => _SkillChip(label: s, isDark: isDark, isPrimary: false)).toList()),
                  ],
                  if (user.averagePoints != null) ...[
                    const SizedBox(height: 8),
                    Row(children: [
                      const Icon(Icons.star_rounded, color: Colors.amber, size: 16),
                      const SizedBox(width: 4),
                      Text('${user.averagePoints!.toStringAsFixed(1)} (${user.ratingCount})',
                          style: TextStyle(fontSize: 12, color: isDark ? Colors.white54 : Colors.black54, fontWeight: FontWeight.w600)),
                    ]),
                  ],
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, color: isDark ? Colors.white38 : Colors.black38),
          ],
        ),
      ),
    );
  }
}

class _AiUserCard extends StatelessWidget {
  final DiscoverUserModel user;
  final bool isDark;
  final VoidCallback onTap;

  const _AiUserCard({required this.user, required this.isDark, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(22),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1E293B).withOpacity(0.82) : Colors.white.withOpacity(0.95),
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: AppColors.primaryBlue.withOpacity(0.3)),
          boxShadow: [BoxShadow(color: AppColors.primaryBlue.withOpacity(0.08), blurRadius: 18, offset: const Offset(0, 8))],
        ),
        child: Row(
          children: [
            CircleAvatar(
              radius: 28,
              backgroundColor: AppColors.primaryBlue.withOpacity(0.15),
              child: Text(user.username.isNotEmpty ? user.username[0].toUpperCase() : '?',
                  style: const TextStyle(color: AppColors.primaryBlue, fontSize: 20, fontWeight: FontWeight.w900)),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(user.username, style: TextStyle(color: isDark ? AppColors.textColor : Colors.black87, fontSize: 16, fontWeight: FontWeight.w800)),
                  if (user.matchReason != null) ...[
                    const SizedBox(height: 4),
                    Text(user.matchReason!, style: TextStyle(fontSize: 12, color: AppColors.primaryBlue.withOpacity(0.8), fontWeight: FontWeight.w600)),
                  ],
                  if (user.matchedSkills.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Wrap(spacing: 6, runSpacing: 6, children: user.matchedSkills.take(3).map((s) => _SkillChip(label: s, isDark: isDark, isPrimary: true)).toList()),
                  ],
                  const SizedBox(height: 6),
                  Row(children: [
                    const Icon(Icons.auto_awesome_rounded, size: 14, color: AppColors.primaryBlue),
                    const SizedBox(width: 4),
                    Text('Match score: ${user.score}', style: const TextStyle(fontSize: 12, color: AppColors.primaryBlue, fontWeight: FontWeight.w700)),
                  ]),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, color: isDark ? Colors.white38 : Colors.black38),
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

  const _SkillChip({required this.label, required this.isDark, required this.isPrimary});

  @override
  Widget build(BuildContext context) {
    final color = isPrimary ? AppColors.primaryBlue : AppColors.primaryGreen;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: color.withOpacity(isDark ? 0.16 : 0.12), borderRadius: BorderRadius.circular(30)),
      child: Text(label, style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w700)),
    );
  }
}