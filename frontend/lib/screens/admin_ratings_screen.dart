import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/app_colors.dart';
import '../providers/service_providers.dart';

class AdminRatingsScreen extends ConsumerStatefulWidget {
  const AdminRatingsScreen({super.key});

  @override
  ConsumerState<AdminRatingsScreen> createState() => _AdminRatingsScreenState();
}

class _AdminRatingsScreenState extends ConsumerState<AdminRatingsScreen> {
  final searchController = TextEditingController();
  final userIdController = TextEditingController();
  final sessionIdController = TextEditingController();

  List<Map<String, dynamic>> ratings = [];

  bool isLoading = true;
  String? errorMessage;
  String selectedFilter = 'all';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      loadRatings('all');
    });
  }

  @override
  void dispose() {
    searchController.dispose();
    userIdController.dispose();
    sessionIdController.dispose();
    super.dispose();
  }

  Future<void> loadRatings(String filter) async {
    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedFilter = filter;
    });

    try {
      List<Map<String, dynamic>> loadedRatings;

      if (filter == 'published') {
        loadedRatings = await ref.read(adminServiceProvider).getPublishedRatings();
      } else if (filter == 'nonPublished') {
        loadedRatings = await ref.read(adminServiceProvider).getNonPublishedRatings();
      } else {
        loadedRatings = await ref.read(adminServiceProvider).getAllRatings();
      }

      if (!mounted) return;

      setState(() {
        ratings = loadedRatings;
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

  Future<void> loadRatingsForUser() async {
    final userId = userIdController.text.trim();
    if (userId.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
      selectedFilter = 'user';
    });

    try {
      final loadedRatings =
      await ref.read(adminServiceProvider).getAllRatingsForUser(userId);

      if (!mounted) return;

      setState(() {
        ratings = loadedRatings;
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

  Future<void> publishRatingsForSession() async {
    final sessionId = sessionIdController.text.trim();
    if (sessionId.isEmpty) return;

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      await ref.read(adminServiceProvider).publishRatingsForSession(sessionId);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Session ratings published successfully.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadRatings(selectedFilter == 'user' ? 'all' : selectedFilter);
    } catch (e) {
      if (!mounted) return;

      setState(() {
        errorMessage = e.toString().replaceAll('Exception: ', '');
        isLoading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString().replaceAll('Exception: ', '')),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  Future<void> updateStatus({
    required Map<String, dynamic> rating,
    required String status,
  }) async {
    final ratingId = _read(rating, ['id', 'ratingId']);

    if (ratingId.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Rating ID is missing.'),
          backgroundColor: Colors.redAccent,
        ),
      );
      return;
    }

    try {
      await ref.read(adminServiceProvider).updateRatingStatus(
        ratingId: ratingId,
        status: status,
      );

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Rating status updated to $status.'),
          backgroundColor: AppColors.primaryGreen,
        ),
      );

      await loadRatings(selectedFilter == 'user' ? 'all' : selectedFilter);
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

  List<Map<String, dynamic>> get filteredRatings {
    final query = searchController.text.trim().toLowerCase();

    if (query.isEmpty) return ratings;

    return ratings.where((rating) {
      final values = rating.values.join(' ').toLowerCase();
      return values.contains(query);
    }).toList();
  }

  String _read(Map<String, dynamic> item, List<String> keys) {
    for (final key in keys) {
      final value = item[key];
      if (value != null && value.toString().trim().isNotEmpty) {
        return value.toString();
      }
    }
    return '';
  }

  String _short(String value) {
    if (value.isEmpty) return '-';
    if (value.length <= 8) return value;
    return '${value.substring(0, 8)}...';
  }

  Widget _filterChip({
    required String label,
    required String value,
  }) {
    final selected = selectedFilter == value;

    return ChoiceChip(
      label: Text(label),
      selected: selected,
      onSelected: (_) => loadRatings(value),
      selectedColor: AppColors.primaryBlue.withOpacity(0.2),
      labelStyle: TextStyle(
        color: selected ? AppColors.primaryBlue : null,
        fontWeight: selected ? FontWeight.bold : FontWeight.normal,
      ),
    );
  }

  Widget _ratingCard(Map<String, dynamic> rating) {
    final id = _read(rating, ['id', 'ratingId']);
    final sessionId = _read(rating, ['sessionId', 'skillSessionId']);
    final senderId = _read(rating, ['senderUserId', 'senderId']);
    final receiverId = _read(rating, ['receiverUserId', 'receiverId']);
    final status = _read(rating, ['status']).toUpperCase();
    final points = _read(rating, ['points', 'ratingPoints']);
    final comment = _read(rating, ['comment']);
    final createdAt = _read(rating, ['createdAt']);
    final publishedAt = _read(rating, ['publishedAt']);

    final isPublished = status == 'PUBLISHED';

    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      elevation: 1.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(18),
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: isPublished
                      ? AppColors.primaryGreen.withOpacity(0.14)
                      : Colors.orange.withOpacity(0.16),
                  child: Icon(
                    isPublished ? Icons.verified_rounded : Icons.pending_actions,
                    color: isPublished ? AppColors.primaryGreen : Colors.orange,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Rating ${_short(id)}',
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: isPublished
                        ? AppColors.primaryGreen.withOpacity(0.12)
                        : Colors.orange.withOpacity(0.14),
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text(
                    status.isEmpty ? 'UNKNOWN' : status,
                    style: TextStyle(
                      color: isPublished ? AppColors.primaryGreen : Colors.orange,
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 14),

            if (points.isNotEmpty)
              Row(
                children: [
                  const Icon(Icons.star_rounded, color: Colors.amber, size: 20),
                  const SizedBox(width: 6),
                  Text(
                    '$points points',
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ],
              ),

            if (comment.isNotEmpty) ...[
              const SizedBox(height: 10),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: AppColors.primaryBlue.withOpacity(0.08),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Text(comment),
              ),
            ],

            const SizedBox(height: 14),

            Text('Session ID: ${_short(sessionId)}'),
            Text('Sender ID: ${_short(senderId)}'),
            Text('Receiver ID: ${_short(receiverId)}'),

            if (createdAt.isNotEmpty) Text('Created: $createdAt'),
            if (publishedAt.isNotEmpty) Text('Published: $publishedAt'),

            const SizedBox(height: 14),

            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: isPublished
                        ? null
                        : () => updateStatus(
                      rating: rating,
                      status: 'PUBLISHED',
                    ),
                    child: const Text('Set Published'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: OutlinedButton(
                    onPressed: !isPublished
                        ? null
                        : () => updateStatus(
                      rating: rating,
                      status: 'PENDING',
                    ),
                    child: const Text('Set Pending'),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _adminInput({
    required TextEditingController controller,
    required String label,
    required String buttonText,
    required VoidCallback onPressed,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 14),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            TextField(
              controller: controller,
              decoration: InputDecoration(
                labelText: label,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                ),
              ),
            ),
            const SizedBox(height: 10),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: isLoading ? null : onPressed,
                child: Text(buttonText),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final visibleRatings = filteredRatings;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Rating Management'),
        centerTitle: true,
        actions: [
          IconButton(
            onPressed: () => loadRatings(selectedFilter == 'user' ? 'all' : selectedFilter),
            icon: const Icon(Icons.refresh),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Ratings',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w900,
              color: isDark ? AppColors.textColor : Colors.black87,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            'Review, filter and update rating statuses.',
            style: TextStyle(
              color: isDark
                  ? AppColors.subtitleDarkColor
                  : AppColors.subtitleBrightColor,
            ),
          ),

          const SizedBox(height: 18),

          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _filterChip(label: 'All', value: 'all'),
              _filterChip(label: 'Published', value: 'published'),
              _filterChip(label: 'Non-published', value: 'nonPublished'),
            ],
          ),

          const SizedBox(height: 16),

          TextField(
            controller: searchController,
            onChanged: (_) => setState(() {}),
            decoration: InputDecoration(
              hintText: 'Search ratings by id, status, comment...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),

          const SizedBox(height: 18),

          _adminInput(
            controller: userIdController,
            label: 'User ID',
            buttonText: 'Load Ratings For User',
            onPressed: loadRatingsForUser,
          ),

          _adminInput(
            controller: sessionIdController,
            label: 'Session ID',
            buttonText: 'Publish Ratings For Session',
            onPressed: publishRatingsForSession,
          ),

          const SizedBox(height: 8),

          if (isLoading)
            const Padding(
              padding: EdgeInsets.all(40),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (errorMessage != null)
            Card(
              color: Colors.redAccent.withOpacity(0.12),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  errorMessage!,
                  textAlign: TextAlign.center,
                ),
              ),
            )
          else if (visibleRatings.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: Center(
                    child: Text('No ratings found.'),
                  ),
                ),
              )
            else ...[
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: Text(
                    '${visibleRatings.length} rating(s) found',
                    style: TextStyle(
                      color: isDark
                          ? AppColors.subtitleDarkColor
                          : AppColors.subtitleBrightColor,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                ...visibleRatings.map(_ratingCard),
              ],
        ],
      ),
    );
  }
}