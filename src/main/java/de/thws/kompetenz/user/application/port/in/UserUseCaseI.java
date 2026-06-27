package de.thws.kompetenz.user.application.port.in;

import de.thws.kompetenz.user.domain.model.User;

import java.util.UUID;

public interface UserUseCaseI {
    User deleteUserById(UUID userId);
    User updateUser(UUID userId, User user);

    User updateInternalFlag(UUID userId, boolean internallyFlagged);

    //here normally we should also define methods like findUserById and some other methods too but they are functioning at UserRepositoryPort and we can use them from there,
    //i could take them here but i think it is not necessary because we can use them from the repository port , and also it will take some time thats why i am not gona do it now maybe later
}
