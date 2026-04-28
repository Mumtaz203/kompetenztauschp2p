package de.thws.kompetenz.user.application.port.in;



import de.thws.kompetenz.user.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface UpdateUserProfileUseCase {
    User updateSkills(UUID userId, List<String> offeredSkills, List<String>wantedSkills);
    User updateName(UUID userId,String name);
    User updateUser(UUID userId,User user);

}
