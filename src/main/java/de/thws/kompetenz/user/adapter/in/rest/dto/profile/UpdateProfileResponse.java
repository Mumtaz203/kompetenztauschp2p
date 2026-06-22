package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import java.util.List;
import java.util.UUID;

public class UpdateProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private List<String> offeredSkills;
    private List<String> wantedSkills;
    private String profileImageUrl;
    private String university;

    public UpdateProfileResponse() {
    }

    public UpdateProfileResponse(UUID id, String username, String email,
                                 List<String> offeredSkills, List<String> wantedSkills,
                                 String profileImageUrl, String university) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
        this.profileImageUrl = profileImageUrl;
        this.university = university;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUniversity() {
        return university;
    }
}
