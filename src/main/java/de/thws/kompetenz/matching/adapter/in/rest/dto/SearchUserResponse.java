package de.thws.kompetenz.matching.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SearchUserResponse {
    private UUID id;
    private String username;
    private String email;
    private List<String> offeredSkills;
    private List<String> wantedSkills;
    private String profileImageUrl;
    private String university;
    private BigDecimal averagePoints;
    private long ratingCount;

    public SearchUserResponse() {
    }

    public SearchUserResponse( UUID id, String username, String email,
                              List<String> offeredSkills, List<String> wantedSkills,
                              String profileImageUrl, String university,
                              BigDecimal averagePoints, long ratingCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
        this.profileImageUrl = profileImageUrl;
        this.university = university;
        this.averagePoints = averagePoints;
        this.ratingCount = ratingCount;
    }


    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getOfferedSkills() { return offeredSkills != null ? offeredSkills : List.of(); }
    public List<String> getWantedSkills() { return wantedSkills != null ? wantedSkills : List.of(); }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getUniversity() { return university; }
    public BigDecimal getAveragePoints() {
        return averagePoints;
    }
    public long getRatingCount() {
        return ratingCount;
    }

    public void setAveragePoints(BigDecimal averagePoints) {
        this.averagePoints = averagePoints;
    }
    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }
    public void setId(UUID id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setOfferedSkills(List<String> offeredSkills) { this.offeredSkills = offeredSkills; }
    public void setWantedSkills(List<String> wantedSkills) { this.wantedSkills = wantedSkills; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setUniversity(String university) { this.university = university; }
}
