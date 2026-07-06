import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../models/user/user_model.dart';
import '../providers/service_providers.dart';

class AdminUsersScreen extends ConsumerStatefulWidget {
  const AdminUsersScreen({super.key});

  @override
  ConsumerState<AdminUsersScreen> createState() => _AdminUsersScreenState();
}

class _AdminUsersScreenState extends ConsumerState<AdminUsersScreen> {
  final searchController = TextEditingController();

  List<UserModel> users = [];
  bool isLoading = true;
  String? errorMessage;

  String selectedFilter = 'all';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadUsers();
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  Future<void> loadUsers() async {
    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final loadedUsers = await ref.read(adminServiceProvider).getAllUsers();

      if (!mounted) return;

      setState(() {
        users = loadedUsers;
        isLoading = false;
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        errorMessage = e.toString().replaceAll('Exception: ', '');
        isLoading = false;
      });
    }
  }

  List<UserModel> get filteredUsers {
    final query = searchController.text.trim().toLowerCase();

    return users.where((user) {
      final hasSkills =
          user.offeredSkills.isNotEmpty || user.wantedSkills.isNotEmpty;

      if (selectedFilter == 'withSkills' && !hasSkills) return false;
      if (selectedFilter == 'withoutSkills' && hasSkills) return false;
      if (selectedFilter == 'flagged' && !user.internallyFlagged) {
        return false;
      }

      if (query.isEmpty) return true;

      final searchableText = [
        user.username,
        user.email,
        user.id,
        ...user.offeredSkills,
        ...user.wantedSkills,
      ].join(' ').toLowerCase();

      return searchableText.contains(query);
    }).toList();
  }

  Future<void> _setInternalFlag(UserModel user, bool value) async {
    try {
      final updatedUser = await ref
          .read(adminServiceProvider)
          .updateUserInternalFlag(userId: user.id, internallyFlagged: value);

      if (!mounted) return;

      setState(() {
        users = users
            .map((item) => item.id == updatedUser.id ? updatedUser : item)
            .toList();
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(value ? 'User flagged.' : 'User unflagged.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  Future<void> _showDeleteDialog(BuildContext context, UserModel user) async {
    final shouldDelete = await showDialog<bool>(
      context: context,
      builder: (dialogContext) {
        return AlertDialog(
          title: const Text('Delete User'),
          content: Text(
            'Are you sure you want to delete ${user.username.isEmpty ? 'this user' : user.username}?',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(dialogContext, true),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.redAccent,
                foregroundColor: Colors.white,
              ),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );

    if (shouldDelete != true) return;

    try {
      await ref.read(adminServiceProvider).deleteUser(user.id);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('User deleted successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadUsers();
    } catch (e) {
      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  void _openUserDetails(UserModel user) {
    final offered = user.offeredSkills.isEmpty
        ? ['No offered skills']
        : user.offeredSkills;
    final wanted = user.wantedSkills.isEmpty
        ? ['No wanted skills']
        : user.wantedSkills;

    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (sheetContext) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 28,
                        backgroundColor: AppColors.primaryGreen.withOpacity(
                          0.14,
                        ),
                        child: Text(
                          user.username.isNotEmpty
                              ? user.username[0].toUpperCase()
                              : '?',
                          style: const TextStyle(
                            color: AppColors.primaryGreen,
                            fontSize: 22,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              user.username.isEmpty
                                  ? 'Unknown User'
                                  : user.username,
                              style: const TextStyle(
                                fontSize: 22,
                                fontWeight: FontWeight.w900,
                              ),
                            ),
                            const SizedBox(height: 3),
                            Text(
                              user.email.isEmpty ? 'No email' : user.email,
                              style: const TextStyle(color: Colors.grey),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 22),

                  if (user.internallyFlagged) ...[
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.redAccent.withOpacity(0.12),
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                          color: Colors.redAccent.withOpacity(0.28),
                        ),
                      ),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.warning_amber_rounded,
                            color: Colors.redAccent,
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Text(
                              user.warningMessage.isEmpty
                                  ? 'This user is internally flagged.'
                                  : user.warningMessage,
                              style: const TextStyle(
                                color: Colors.redAccent,
                                fontWeight: FontWeight.w700,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 18),
                  ],

                  _detailTitle('User ID'),
                  SelectableText(user.id),
                  const SizedBox(height: 18),

                  _detailTitle('Offered Skills'),
                  const SizedBox(height: 8),
                  _skillWrap(
                    skills: offered,
                    color: user.offeredSkills.isEmpty
                        ? Colors.grey
                        : AppColors.primaryBlue,
                  ),

                  const SizedBox(height: 18),

                  _detailTitle('Wanted Skills'),
                  const SizedBox(height: 8),
                  _skillWrap(
                    skills: wanted,
                    color: user.wantedSkills.isEmpty
                        ? Colors.grey
                        : AppColors.primaryGreen,
                  ),

                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.pop(sheetContext);
                        _setInternalFlag(user, !user.internallyFlagged);
                      },
                      icon: Icon(
                        user.internallyFlagged
                            ? Icons.flag_circle_outlined
                            : Icons.flag_outlined,
                      ),
                      label: Text(
                        user.internallyFlagged
                            ? 'Remove Internal Flag'
                            : 'Flag User Internally',
                      ),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: user.internallyFlagged
                            ? Colors.grey
                            : Colors.redAccent,
                        side: BorderSide(
                          color: user.internallyFlagged
                              ? Colors.grey
                              : Colors.redAccent,
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 14),
                      ),
                    ),
                  ),

                  const SizedBox(height: 12),

                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        Navigator.pop(sheetContext);
                        _showDeleteDialog(context, user);
                      },
                      icon: const Icon(Icons.delete_outline),
                      label: const Text('Delete User'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: Colors.redAccent,
                        side: const BorderSide(color: Colors.redAccent),
                        padding: const EdgeInsets.symmetric(vertical: 14),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _detailTitle(String text) {
    return Text(
      text,
      style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 15),
    );
  }

  Widget _skillWrap({required List<String> skills, required Color color}) {
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: skills.map((skill) {
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 11, vertical: 7),
          decoration: BoxDecoration(
            color: color.withOpacity(0.12),
            borderRadius: BorderRadius.circular(30),
          ),
          child: Text(
            skill,
            style: TextStyle(
              color: color,
              fontSize: 12,
              fontWeight: FontWeight.w800,
            ),
          ),
        );
      }).toList(),
    );
  }

  String _short(String value) {
    if (value.isEmpty) return '-';
    if (value.length <= 8) return value;
    return '${value.substring(0, 8)}...';
  }

  Widget _statCard({
    required String title,
    required String value,
    required IconData icon,
    required Color color,
  }) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: color.withOpacity(0.12),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: color.withOpacity(0.22)),
        ),
        child: Column(
          children: [
            Icon(icon, color: color),
            const SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(
                color: color,
                fontSize: 20,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              title,
              textAlign: TextAlign.center,
              style: TextStyle(
                color: color.withOpacity(0.9),
                fontSize: 12,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _filterChip({required String label, required String value}) {
    final selected = selectedFilter == value;

    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) {
        setState(() {
          selectedFilter = value;
        });
      },
      selectedColor: AppColors.primaryBlue.withOpacity(0.18),
      labelStyle: TextStyle(
        color: selected ? AppColors.primaryBlue : null,
        fontWeight: selected ? FontWeight.w800 : FontWeight.normal,
      ),
    );
  }

  Widget _userCard(UserModel user) {
    final hasSkills =
        user.offeredSkills.isNotEmpty || user.wantedSkills.isNotEmpty;

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => _openUserDetails(user),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    backgroundColor: AppColors.primaryGreen.withOpacity(0.14),
                    child: Text(
                      user.username.isNotEmpty
                          ? user.username[0].toUpperCase()
                          : '?',
                      style: const TextStyle(
                        color: AppColors.primaryGreen,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          user.username.isEmpty
                              ? 'Unknown User'
                              : user.username,
                          style: const TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 16,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          user.email.isEmpty ? 'No email' : user.email,
                          style: const TextStyle(
                            fontSize: 13,
                            color: Colors.grey,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const Icon(Icons.chevron_right_rounded, color: Colors.grey),
                ],
              ),

              if (user.internallyFlagged) ...[
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 7,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.redAccent.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(30),
                  ),
                  child: const Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        Icons.warning_amber_rounded,
                        color: Colors.redAccent,
                        size: 16,
                      ),
                      SizedBox(width: 5),
                      Text(
                        'Internally flagged',
                        style: TextStyle(
                          color: Colors.redAccent,
                          fontSize: 12,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                    ],
                  ),
                ),
              ],

              const SizedBox(height: 12),

              Text(
                'ID: ${_short(user.id)}',
                style: const TextStyle(color: Colors.grey, fontSize: 12),
              ),

              const SizedBox(height: 12),

              if (hasSkills) ...[
                if (user.offeredSkills.isNotEmpty) ...[
                  const Text(
                    'Teaches',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800),
                  ),
                  const SizedBox(height: 6),
                  _skillWrap(
                    skills: user.offeredSkills.take(4).toList(),
                    color: AppColors.primaryBlue,
                  ),
                  const SizedBox(height: 10),
                ],
                if (user.wantedSkills.isNotEmpty) ...[
                  const Text(
                    'Wants to learn',
                    style: TextStyle(fontSize: 12, fontWeight: FontWeight.w800),
                  ),
                  const SizedBox(height: 6),
                  _skillWrap(
                    skills: user.wantedSkills.take(4).toList(),
                    color: AppColors.primaryGreen,
                  ),
                ],
              ] else
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 11,
                    vertical: 7,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.grey.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(30),
                  ),
                  child: const Text(
                    'No skills added',
                    style: TextStyle(
                      color: Colors.grey,
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final visibleUsers = filteredUsers;
    final usersWithSkills = users
        .where(
          (user) =>
              user.offeredSkills.isNotEmpty || user.wantedSkills.isNotEmpty,
        )
        .length;
    final flaggedUsers = users.where((user) => user.internallyFlagged).length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('User Management'),
        centerTitle: true,
        actions: [
          IconButton(onPressed: loadUsers, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Users',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Search, inspect and manage registered users.',
            style: TextStyle(
              color: isDark
                  ? AppColors.subtitleDarkColor
                  : AppColors.subtitleBrightColor,
            ),
          ),

          const SizedBox(height: 18),

          Row(
            children: [
              _statCard(
                title: 'Total Users',
                value: users.length.toString(),
                icon: Icons.people_outline,
                color: AppColors.primaryBlue,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'With Skills',
                value: usersWithSkills.toString(),
                icon: Icons.school_outlined,
                color: AppColors.primaryGreen,
              ),
              const SizedBox(width: 10),
              _statCard(
                title: 'Flagged',
                value: flaggedUsers.toString(),
                icon: Icons.warning_amber_rounded,
                color: Colors.redAccent,
              ),
            ],
          ),

          const SizedBox(height: 18),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search by username, email, id or skill...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),

          const SizedBox(height: 12),

          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _filterChip(label: 'All', value: 'all'),
              _filterChip(label: 'With Skills', value: 'withSkills'),
              _filterChip(label: 'No Skills', value: 'withoutSkills'),
              _filterChip(label: 'Flagged', value: 'flagged'),
            ],
          ),

          const SizedBox(height: 18),

          if (isLoading)
            const Padding(
              padding: EdgeInsets.all(40),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (errorMessage != null)
            _AdminErrorBox(message: errorMessage!, onRetry: loadUsers)
          else if (visibleUsers.isEmpty)
            const _AdminEmptyBox()
          else ...[
            Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: Text(
                '${visibleUsers.length} user(s) found',
                style: TextStyle(
                  color: isDark
                      ? AppColors.subtitleDarkColor
                      : AppColors.subtitleBrightColor,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            ...visibleUsers.map(_userCard),
          ],

          const SizedBox(height: 20),
        ],
      ),
    );
  }
}

class _AdminErrorBox extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;

  const _AdminErrorBox({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.redAccent.withOpacity(0.12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Icon(Icons.error_outline, color: Colors.redAccent),
            const SizedBox(height: 8),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 12),
            ElevatedButton(onPressed: onRetry, child: const Text('Try Again')),
          ],
        ),
      ),
    );
  }
}

class _AdminEmptyBox extends StatelessWidget {
  const _AdminEmptyBox();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(24),
        child: Center(child: Text('No users found.')),
      ),
    );
  }
}
