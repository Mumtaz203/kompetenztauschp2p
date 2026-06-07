class AuthResponseModel {
  final String token;
  final String role;

  AuthResponseModel({
    required this.token,
    required this.role,
  });

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) {
    String parsedToken = '';
    String parsedRole = 'USER';

    if (json['token'] != null) {
      parsedToken = json['token'].toString();
    }


    if (json['role'] != null) {
      parsedRole = json['role'].toString();
    }

    return AuthResponseModel(
      token: parsedToken,
      role: parsedRole,
    );
  }
}