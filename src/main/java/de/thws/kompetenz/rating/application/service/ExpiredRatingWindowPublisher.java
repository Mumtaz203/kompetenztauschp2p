package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class ExpiredRatingWindowPublisher {

    private final ISessionRepositoryPort sessionRepositoryPort;
    private final IPublishSessionRatingsUseCase publishSessionRatingsUseCase;

    public ExpiredRatingWindowPublisher(
            ISessionRepositoryPort sessionRepositoryPort,
            IPublishSessionRatingsUseCase publishSessionRatingsUseCase
    ) {
        this.sessionRepositoryPort = sessionRepositoryPort;
        this.publishSessionRatingsUseCase = publishSessionRatingsUseCase;
    }

    @Scheduled(every = "1h")
    @Transactional
    void publishExpiredRatingWindows() {
        sessionRepositoryPort.findRatingOpenSessionsWithExpiredWindow(LocalDateTime.now())
                .forEach(session -> publishSessionRatingsUseCase.publishRatingsForSession(session.getId()));
    }
}
