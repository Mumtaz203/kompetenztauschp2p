package de.thws.kompetenz.matching.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

public class SearchUserResponse {
    private UUID id;
    private String username;
    private String email;
    private List<String> offeredSkills;
    private List<String> wantedSkills;

    public SearchUserResponse() {
    }

    public SearchUserResponse( UUID id, String username, String email,
                              List<String> offeredSkills, List<String> wantedSkills) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
    }


    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public List<String> getOfferedSkills() { return offeredSkills != null ? offeredSkills : List.of(); }
    public List<String> getWantedSkills() { return wantedSkills != null ? wantedSkills : List.of(); }

    public void setId(UUID id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setOfferedSkills(List<String> offeredSkills) { this.offeredSkills = offeredSkills; }
    public void setWantedSkills(List<String> wantedSkills) { this.wantedSkills = wantedSkills; }
}
