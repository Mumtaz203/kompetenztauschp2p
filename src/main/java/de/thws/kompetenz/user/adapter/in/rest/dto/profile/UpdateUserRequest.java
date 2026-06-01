package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateUserRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    private List<String> offeredSkills;
    private List<String> wantedSkills;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String username, List<String> offeredSkills, List<String> wantedSkills) {
        this.username = username;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setOfferedSkills(List<String> offeredSkills) {
        this.offeredSkills = offeredSkills;
    }

    public void setWantedSkills(List<String> wantedSkills) {
        this.wantedSkills = wantedSkills;
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