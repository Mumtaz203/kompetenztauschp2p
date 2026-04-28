package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import java.util.List;

public class UpdateUserRequest {

    private String username;
    private List<String> offeredSkills;
    private List<String> wantedSkills;

    public UpdateUserRequest() {
    }

    public String getUsername() {
        return username;
    }

    public List<String> getOfferedSkills() {
        return offeredSkills;
    }

    public List<String> getWantedSkills() {
        return wantedSkills;
    }
}