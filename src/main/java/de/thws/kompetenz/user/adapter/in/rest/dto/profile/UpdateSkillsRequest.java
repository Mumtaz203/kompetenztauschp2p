package de.thws.kompetenz.user.adapter.in.rest.dto.profile;
import java.util.List;

public class UpdateSkillsRequest {

    private List<String> offeredSkills;
    private List<String> wantedSkills;

    public UpdateSkillsRequest() {
    }

    public List<String> getOfferedSkills() {
        return offeredSkills;
    }

    public List<String> getWantedSkills() {
        return wantedSkills;
    }
}