package de.thws.kompetenz.user.adapter.in.rest.dto.user;

import java.util.List;
import java.util.UUID;

public class UserResponse {
    private String password;
    private String email;
    private UUID id;
    private String username;
    private List<String> offeredSkills;
    private List<String> wantedSkills;
    private String profileImageUrl;
    private String university;

    public UserResponse() {
    }

    public UserResponse(UUID id, String username, String email,
                           List<String> offeredSkills, List<String> wantedSkills,
                           String profileImageUrl, String university, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
        this.profileImageUrl = profileImageUrl;
        this.university = university;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getOfferedSkills() {
        return offeredSkills;
    }

    public List<String> getWantedSkills() {
        return wantedSkills;
    }
    public String getProfileImageUrl() {return profileImageUrl;}
    public String getUniversity() {return university;}
    public String getPassword() {return password;}
}
