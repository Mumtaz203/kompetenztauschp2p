package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.SessionRating;
import de.thws.kompetenz.session.application.port.in.ICloseRatingWindowUseCase;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionRatingServiceTest {

    @Mock
    SessionRatingRepositoryPort sessionRatingRepositoryPort;

    @Mock
    IGetSessionUseCase getSessionUseCase;

    @Mock
    ICloseRatingWindowUseCase closeRatingWindowUseCase;

    private SessionRatingService service;

    @BeforeEach
    void setUp() {
        service = new SessionRatingService(
                sessionRatingRepositoryPort,
                getSessionUseCase,
                closeRatingWindowUseCase
        );
    }

    @Test
    void getPublishedRatingsForUser_shouldReturnPublishedRatingsReceivedByUser() {
        UUID receiverUserId = UUID.randomUUID();
        List<SessionRating> expectedRatings = List.of(
                new SessionRating(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        receiverUserId,
                        RatingStatus.PUBLISHED,
                        BigDecimal.valueOf(4.5),
                        "Helpful exchange",
                        LocalDateTime.now().minusDays(2),
                        LocalDateTime.now().minusDays(1)
                )
        );

        when(sessionRatingRepositoryPort.findPublishedRatingsByReceiverUserId(receiverUserId))
                .thenReturn(expectedRatings);

        List<SessionRating> ratings = service.getPublishedRatingsForUser(receiverUserId);

        assertEquals(expectedRatings, ratings);
        verify(sessionRatingRepositoryPort).findPublishedRatingsByReceiverUserId(receiverUserId);
    }

    @Test
    void getPublishedRatingsForUser_shouldThrow_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.getPublishedRatingsForUser(null));
    }

    @Test
    void getAllRatingsForUser_shouldReturnAllRatingsReceivedByUser() {
        UUID receiverUserId = UUID.randomUUID();
        List<SessionRating> expectedRatings = List.of(
                new SessionRating(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        receiverUserId,
                        RatingStatus.PENDING,
                        BigDecimal.valueOf(4.5),
                        "Awaiting publish",
                        LocalDateTime.now().minusDays(1),
                        null
                )
        );

        when(sessionRatingRepositoryPort.findAllRatingsByReceiverUserId(receiverUserId))
                .thenReturn(expectedRatings);

        List<SessionRating> ratings = service.getAllRatingsForUser(receiverUserId);

        assertEquals(expectedRatings, ratings);
        verify(sessionRatingRepositoryPort).findAllRatingsByReceiverUserId(receiverUserId);
    }
}
