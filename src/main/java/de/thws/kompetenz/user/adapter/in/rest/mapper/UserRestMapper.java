package de.thws.kompetenz.user.adapter.in.rest.mapper;

import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateProfileResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetAllUsersResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetUserResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.UserResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class UserRestMapper {

    public UpdateProfileResponse toUpdateProfileResponse(User user) {
        if (user == null) return null;
        return new UpdateProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills(),
                user.getProfileImageUrl(),
                user.getUniversity()
        );
    }

    public GetUserResponse toGetUserResponse(User user, RatingSummary ratingSummary) {
        if (user == null) {
            return null;
        }

        RatingSummary safeRatingSummary = ratingSummary != null
                ? ratingSummary
                : new RatingSummary(BigDecimal.ZERO, 0);

        return new GetUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills(),
                user.getProfileImageUrl(),
                user.getUniversity(),
                safeRatingSummary.averagePoints(),
                safeRatingSummary.ratingCount(),
                user.isInternallyFlagged(),
                user.isInternallyFlagged()
                        ? "This user is currently flagged for problematic behavior. Please consider this before sending a match request."
                        : null
        );
    }

    public GetAllUsersResponse toGetAllUsersResponse(
            List<User> users,
            Map<UUID, RatingSummary> ratingSummaries
    ) {
        List<GetUserResponse> mappedUsers = new ArrayList<>();

        if (users != null) {
            for (User user : users) {
                RatingSummary ratingSummary = ratingSummaries != null
                        ? ratingSummaries.get(user.getId())
                        : null;

                GetUserResponse mapped = toGetUserResponse(user, ratingSummary);

                if (mapped != null) {
                    mappedUsers.add(mapped);
                }
            }
        }

        return new GetAllUsersResponse(mappedUsers);
    }
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills(),
                user.getProfileImageUrl(),
                user.getUniversity(),
                user.getPassword()
        );
    }
}
