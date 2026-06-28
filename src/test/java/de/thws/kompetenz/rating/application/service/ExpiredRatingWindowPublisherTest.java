package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.rating.domain.SessionRating;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpiredRatingWindowPublisherTest {

    @Test
    void publishExpiredRatingWindows_shouldPublishOnlySessionsReturnedByExpiredRatingOpenQuery() {
        ISessionRepositoryPort sessionRepositoryPort = mock(ISessionRepositoryPort.class);
        IPublishSessionRatingsUseCase publishSessionRatingsUseCase = mock(IPublishSessionRatingsUseCase.class);
        ExpiredRatingWindowPublisher publisher = new ExpiredRatingWindowPublisher(
                sessionRepositoryPort,
                publishSessionRatingsUseCase
        );

        SkillSession expiredRatingOpenSession = new SkillSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                SessionStatus.RATING_OPEN,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(8),
                LocalDateTime.now().minusDays(8),
                LocalDateTime.now().minusDays(1)
        );

        when(sessionRepositoryPort.findRatingOpenSessionsWithExpiredWindow(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredRatingOpenSession));
        when(publishSessionRatingsUseCase.publishRatingsForSession(expiredRatingOpenSession.getId()))
                .thenReturn(List.<SessionRating>of());

        publisher.publishExpiredRatingWindows();

        verify(sessionRepositoryPort).findRatingOpenSessionsWithExpiredWindow(any(LocalDateTime.class));
        verify(publishSessionRatingsUseCase).publishRatingsForSession(expiredRatingOpenSession.getId());
        verifyNoMoreInteractions(publishSessionRatingsUseCase);
    }
}
