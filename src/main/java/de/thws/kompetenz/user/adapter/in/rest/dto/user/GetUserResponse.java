package de.thws.kompetenz.user.adapter.in.rest.dto.user;

import java.util.List;
import java.util.UUID;

public class GetUserResponse { private UUID id;
    private String username;
    private String email;
    private List<String> offeredSkills;
    private List<String> wantedSkills;

    public GetUserResponse() {
    }

    public GetUserResponse(UUID id, String username, String email,
                                 List<String> offeredSkills, List<String> wantedSkills) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.offeredSkills = offeredSkills;
        this.wantedSkills = wantedSkills;
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
}
