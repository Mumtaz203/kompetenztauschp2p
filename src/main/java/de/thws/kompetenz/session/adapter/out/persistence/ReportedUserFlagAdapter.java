package de.thws.kompetenz.session.adapter.out.persistence;

import de.thws.kompetenz.session.application.port.out.ReportedUserFlagPort;
import de.thws.kompetenz.user.adapter.out.persistence.repository.UserPanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ReportedUserFlagAdapter implements ReportedUserFlagPort {

    private final UserPanacheRepository userRepository;

    public ReportedUserFlagAdapter(UserPanacheRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void updateReportCount(UUID userId, long reportCount) {
        userRepository.findUserById(userId).ifPresent(user -> {
            user.setPrivateReportCount(reportCount);
        });
    }

    @Override
    public void flagUser(UUID userId) {
        userRepository.findUserById(userId).ifPresent(user -> user.setInternallyFlagged(true));
    }
}
