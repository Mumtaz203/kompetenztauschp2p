package de.thws.kompetenz.matching.application.port.in;



import de.thws.kompetenz.user.domain.model.User;

import java.util.List;


public interface SearchUserUseCase {
    List<User> searchBySkill(String skill);
}
