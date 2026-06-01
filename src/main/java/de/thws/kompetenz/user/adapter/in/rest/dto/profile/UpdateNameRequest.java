package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateNameRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    public UpdateNameRequest() {
    }

    public UpdateNameRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}