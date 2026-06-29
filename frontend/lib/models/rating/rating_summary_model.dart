class RatingSummaryModel {
  final double averagePoints;
  final int ratingCount;

  const RatingSummaryModel({
    required this.averagePoints,
    required this.ratingCount,
  });

  factory RatingSummaryModel.fromJson(Map<String, dynamic> json) {
    return RatingSummaryModel(
      averagePoints: (json['averagePoints'] as num).toDouble(),
      ratingCount: json['ratingCount'] as int,
    );
  }
}