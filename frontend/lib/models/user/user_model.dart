class UserModel {
  final String id;
  final String username;
  final String email;
  final List<String> offeredSkills;
  final List<String> wantedSkills;
  final String profileImageUrl;
  final String university;
  final double averagePoints;
  final int ratingCount;

  UserModel({
    required this.id,
    required this.username,
    required this.email,
    required this.offeredSkills,
    required this.wantedSkills,
    this.profileImageUrl = '',
    this.university = '',
    this.averagePoints = 0.0,
    this.ratingCount = 0,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    List<String> parseStringList(dynamic value) {
      List<String> result = [];
      if (value is List) {
        for (final item in value) {
          result.add(item.toString());
        }
      }
      return result;
    }

    double parseDouble(dynamic value) {
      if (value == null) return 0.0;
      return double.tryParse(value.toString()) ?? 0.0;
    }

    int parseInt(dynamic value) {
      if (value == null) return 0;
      return int.tryParse(value.toString()) ?? 0;
    }

    return UserModel(
      id: json['id']?.toString() ?? '',
      username: json['username']?.toString() ?? '',
      email: json['email']?.toString() ?? '',
      offeredSkills: parseStringList(json['offeredSkills']),
      wantedSkills: parseStringList(json['wantedSkills']),
      profileImageUrl: json['profileImageUrl']?.toString() ?? '',
      university: json['university']?.toString() ?? '',
      averagePoints: parseDouble(json['averagePoints']),
      ratingCount: parseInt(json['ratingCount']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'offeredSkills': offeredSkills,
      'wantedSkills': wantedSkills,
      'profileImageUrl': profileImageUrl,
      'university': university,
      'averagePoints': averagePoints,
      'ratingCount': ratingCount,
    };
  }
}