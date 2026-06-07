package de.thws.kompetenz.matching.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

public class DiscoverUserResponse {

    private UUID userId;
    private String username;
    private int score;
    private double bestSimilarity;
    private List<String> matchedSkills;
    private String matchReason;

    public DiscoverUserResponse() {
    }

    public DiscoverUserResponse(
            UUID userId,
            String username,
            int score,
            double bestSimilarity,
            List<String> matchedSkills,
            String matchReason
    ) {
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.bestSimilarity = bestSimilarity;
        this.matchedSkills = matchedSkills;
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
        return matchedSkills != null ? matchedSkills : List.of();
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }
}
