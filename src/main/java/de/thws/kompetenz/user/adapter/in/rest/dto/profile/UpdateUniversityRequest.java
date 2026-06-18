package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import jakarta.validation.constraints.Size;

public class UpdateUniversityRequest {

    @Size(max = 255)
    private String university;

    public UpdateUniversityRequest() {
    }

    public UpdateUniversityRequest(String university) {
        this.university = university;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }
}
