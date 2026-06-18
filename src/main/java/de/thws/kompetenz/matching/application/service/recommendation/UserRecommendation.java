package de.thws.kompetenz.matching.application.service.recommendation;

import java.util.List;
import java.util.UUID;

public class UserRecommendation {

    private UUID userId;
    private String username;
    private String profileImageUrl;
    private String university;
    private int score;
    private double bestSimilarity;
    private List<String> matchedSkills;
    private String matchReason;

    public UserRecommendation() {
        this.matchedSkills = List.of();
    }

    public UserRecommendation(
            UUID userId,
            String username,
            String profileImageUrl,
            String university,
            int score,
            double bestSimilarity,
            List<String> matchedSkills,
            String matchReason
    ) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.university = university;
        this.score = score;
        this.bestSimilarity = bestSimilarity;
        this.matchedSkills = matchedSkills == null ? List.of() : List.copyOf(matchedSkills);
        this.matchReason = matchReason;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getBestSimilarity() {
        return bestSimilarity;
    }

    public void setBestSimilarity(double bestSimilarity) {
        this.bestSimilarity = bestSimilarity;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills == null ? List.of() : matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills == null ? List.of() : List.copyOf(matchedSkills);
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
