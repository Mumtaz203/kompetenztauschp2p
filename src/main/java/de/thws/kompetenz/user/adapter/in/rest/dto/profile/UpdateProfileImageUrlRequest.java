package de.thws.kompetenz.user.adapter.in.rest.dto.profile;

import jakarta.validation.constraints.Size;

public class UpdateProfileImageUrlRequest {

    @Size(max = 2048)
    private String profileImageUrl;

    public UpdateProfileImageUrlRequest() {
    }

    public UpdateProfileImageUrlRequest(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
