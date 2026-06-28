package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class SessionCompletionReminderScheduler {

    private static final int COMPLETION_PROMPT_AFTER_DAYS = 3;
    private static final int PENDING_CONFIRMATION_TIMEOUT_HOURS = 48;

    private final ISessionRepositoryPort sessionRepositoryPort;

    public SessionCompletionReminderScheduler(ISessionRepositoryPort sessionRepositoryPort) {
        this.sessionRepositoryPort = sessionRepositoryPort;
    }

    @Scheduled(every = "1h")
    @Transactional
    void updateCompletionConfirmationStates() {
        LocalDateTime now = LocalDateTime.now();

        sessionRepositoryPort.findActiveSessionsAcceptedBefore(now.minusDays(COMPLETION_PROMPT_AFTER_DAYS))
                .forEach(session -> {
                    session.markCompletionConfirmationPending();
                    sessionRepositoryPort.save(session);
                });

        sessionRepositoryPort.findCompletionPendingSessionsWithStaleResponses(
                        now.minusHours(PENDING_CONFIRMATION_TIMEOUT_HOURS)
                )
                .forEach(session -> {
                    session.markDisputed();
                    sessionRepositoryPort.save(session);
                });
    }
}
