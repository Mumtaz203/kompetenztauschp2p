class UserModel {
  final String id;
  final String username;
  final String email;
  final List<String> offeredSkills;
  final List<String> wantedSkills;

  UserModel({
    required this.id,
    required this.username,
    required this.email,
    required this.offeredSkills,
    required this.wantedSkills,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    String parsedId = '';
    if (json['id'] != null) {
      parsedId = json['id'].toString();
    }

    String parsedUsername = '';
    if (json['username'] != null) {
      parsedUsername = json['username'].toString();
    }

    String parsedEmail = '';
    if (json['email'] != null) {
      parsedEmail = json['email'].toString();
    }

    List<String> parsedOfferedSkills = [];
    if (json['offeredSkills'] != null) {
      List<dynamic> rawList = json['offeredSkills'];
      for (var item in rawList) {
        parsedOfferedSkills.add(item.toString());
      }
    }

    List<String> parsedWantedSkills = [];
    if (json['wantedSkills'] != null) {
      List<dynamic> rawList = json['wantedSkills'];
      for (var item in rawList) {
        parsedWantedSkills.add(item.toString());
      }
    }

    return UserModel(
      id: parsedId,
      username: parsedUsername,
      email: parsedEmail,
      offeredSkills: parsedOfferedSkills,
      wantedSkills: parsedWantedSkills,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'offeredSkills': offeredSkills,
      'wantedSkills': wantedSkills,
    };
  }
}