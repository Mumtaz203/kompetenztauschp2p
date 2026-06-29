class DiscoverUserModel {
  final String userId;
  final String username;
  final String? profileImageUrl;
  final String? university;
  final int score;
  final double bestSimilarity;
  final List<String> matchedSkills;
  final String? matchReason;

  const DiscoverUserModel({
    required this.userId,
    required this.username,
    this.profileImageUrl,
    this.university,
    required this.score,
    required this.bestSimilarity,
    required this.matchedSkills,
    this.matchReason,
  });

  factory DiscoverUserModel.fromJson(Map<String, dynamic> json) {
    return DiscoverUserModel(
      userId: json['userId'],
      username: json['username'],
      profileImageUrl: json['profileImageUrl'],
      university: json['university'],
      score: json['score'] as int,
      bestSimilarity: (json['bestSimilarity'] as num).toDouble(),
      matchedSkills: List<String>.from(json['matchedSkills'] ?? []),
      matchReason: json['matchReason'],
    );
  }
}