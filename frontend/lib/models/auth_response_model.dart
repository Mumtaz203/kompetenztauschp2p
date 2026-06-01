class AuthResponseModel {
  final String token;

  AuthResponseModel({
    required this.token,
  });

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) {
    String parsedToken = '';

    if (json['token'] != null) {
      parsedToken = json['token'].toString();
    }

    return AuthResponseModel(
      token: parsedToken,
    );
  }
}