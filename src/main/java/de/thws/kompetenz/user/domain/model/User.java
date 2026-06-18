package de.thws.kompetenz.user.domain.model;

import java.util.List;
import java.util.UUID;

public class User {

    private UUID id;
    private String username;
    private String email;
    private String password;
    private List<String> offeredSkills;
    private List<String> wantedSkills;
    private String profileImageUrl;
    private String university;

    public User() {
    }

    public User(UUID id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public List<String> getOfferedSkills() {
        return offeredSkills;
    }
    public void setOfferedSkills(List<String> offeredSkills) {
        this.offeredSkills = offeredSkills;
    }
    public List<String> getWantedSkills() {
        return wantedSkills;
    }
    public void setWantedSkills(List<String> wantedSkills) {
        this.wantedSkills = wantedSkills;
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
}
