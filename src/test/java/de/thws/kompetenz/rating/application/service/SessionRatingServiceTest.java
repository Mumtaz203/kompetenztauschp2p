package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.exception.SessionRatingNotAuthorizedException;
import de.thws.kompetenz.rating.application.exception.SessionRatingNotFoundException;
import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.rating.domain.SessionRating;
import de.thws.kompetenz.session.application.port.in.ICloseRatingWindowUseCase;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionRatingServiceTest {

    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID RATING_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final UUID RECEIVER_ID = UUID.randomUUID();
    private static final UUID OTHER_USER_ID = UUID.randomUUID();

    @Mock
    SessionRatingRepositoryPort sessionRatingRepositoryPort;

    @Mock
    IGetSessionUseCase getSessionUseCase;

    @Mock
    ICloseRatingWindowUseCase closeRatingWindowUseCase;

    SessionRatingService service;

    @BeforeEach
    void setUp() {
        service = new SessionRatingService(
                sessionRatingRepositoryPort,
                getSessionUseCase,
                closeRatingWindowUseCase
        );
    }

    @Test
    void createRating_shouldCreateRating_whenInputIsValid() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRatingRepositoryPort.existsBySessionIdAndSenderUserId(SESSION_ID, SENDER_ID))
                .thenReturn(false);
        when(sessionRatingRepositoryPort.save(any(SessionRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionRating result = service.createRating(
                SESSION_ID,
                SENDER_ID,
                RECEIVER_ID,
                BigDecimal.valueOf(4.5),
                "Good session"
        );

        assertNotNull(result);
        assertEquals(SESSION_ID, result.getSessionId());
        assertEquals(SENDER_ID, result.getSenderUserId());
        assertEquals(RECEIVER_ID, result.getReceiverUserId());
        assertEquals(RatingStatus.PENDING, result.getStatus());

        verify(sessionRatingRepositoryPort).save(any(SessionRating.class));
    }

    @Test
    void createRating_shouldThrow_whenSessionIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        null,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenSenderIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        null,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenReceiverIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        null,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenSenderRatesHimself() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        SENDER_ID,
                        BigDecimal.valueOf(4.5),
                        "Self rating"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenSessionDoesNotExist() {
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenSenderIsNotParticipant() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(session.hasParticipant(SENDER_ID)).thenReturn(false);
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenReceiverIsNotParticipant() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(session.hasParticipant(SENDER_ID)).thenReturn(true);
        when(session.hasParticipant(RECEIVER_ID)).thenReturn(false);
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenUsersDoNotBelongToThisSessionPair() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(session.isBetween(SENDER_ID, RECEIVER_ID)).thenReturn(false);
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalArgumentException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenSessionIsNotRatingOpen() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(session.getStatus()).thenReturn(SessionStatus.ACTIVE);
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenRatingWindowIsClosed() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().minusDays(1));

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void createRating_shouldThrow_whenUserAlreadyRatedSession() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRatingRepositoryPort.existsBySessionIdAndSenderUserId(SESSION_ID, SENDER_ID))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                service.createRating(
                        SESSION_ID,
                        SENDER_ID,
                        RECEIVER_ID,
                        BigDecimal.valueOf(4.5),
                        "Good session"
                )
        );
    }

    @Test
    void publishRatingsForSession_shouldPublishPendingRatingsAndCloseWindow() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().minusMinutes(1));

        SessionRating ratingOne = pendingRating(SENDER_ID, RECEIVER_ID);
        SessionRating ratingTwo = pendingRating(RECEIVER_ID, SENDER_ID);

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRatingRepositoryPort.findPendingRatingsBySessionId(SESSION_ID))
                .thenReturn(List.of(ratingOne, ratingTwo));
        when(sessionRatingRepositoryPort.save(any(SessionRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<SessionRating> result = service.publishRatingsForSession(SESSION_ID);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(rating -> rating.getStatus() == RatingStatus.PUBLISHED));
        assertTrue(result.stream().allMatch(rating -> rating.getPublishedAt() != null));

        verify(closeRatingWindowUseCase).closeRatingWindow(SESSION_ID);
        verify(sessionRatingRepositoryPort, times(2)).save(any(SessionRating.class));
    }

    @Test
    void publishRatingsForSession_shouldThrow_whenSessionIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.publishRatingsForSession(null)
        );
    }

    @Test
    void publishRatingsForSession_shouldThrow_whenSessionDoesNotExist() {
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                service.publishRatingsForSession(SESSION_ID)
        );
    }

    @Test
    void publishRatingsForSession_shouldThrow_whenSessionIsNotRatingOpen() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().minusMinutes(1));

        when(session.getStatus()).thenReturn(SessionStatus.ACTIVE);
        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () ->
                service.publishRatingsForSession(SESSION_ID)
        );
    }

    @Test
    void publishRatingsForSession_shouldThrow_whenRatingWindowEndIsNull() {
        SkillSession session = ratingOpenSession(null);

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () ->
                service.publishRatingsForSession(SESSION_ID)
        );
    }

    @Test
    void publishRatingsForSession_shouldThrow_whenRatingWindowIsStillOpen() {
        SkillSession session = ratingOpenSession(LocalDateTime.now().plusDays(1));

        when(getSessionUseCase.findById(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(IllegalStateException.class, () ->
                service.publishRatingsForSession(SESSION_ID)
        );
    }

    @Test
    void getRatingSummaryForUser_shouldReturnZeroSummary_whenUserHasNoPublishedRatings() {
        when(sessionRatingRepositoryPort.sumPublishedPointsByReceiverUserId(RECEIVER_ID))
                .thenReturn(BigDecimal.ZERO);
        when(sessionRatingRepositoryPort.countPublishedRatingsByReceiverUserId(RECEIVER_ID))
                .thenReturn(0L);

        RatingSummary result = service.getRatingSummaryForUser(RECEIVER_ID);

        assertEquals(BigDecimal.ZERO, result.averagePoints());
        assertEquals(0, result.ratingCount());
    }

    @Test
    void getRatingSummaryForUser_shouldCalculateAverageRoundedToOneDecimal() {
        when(sessionRatingRepositoryPort.sumPublishedPointsByReceiverUserId(RECEIVER_ID))
                .thenReturn(BigDecimal.valueOf(9.5));
        when(sessionRatingRepositoryPort.countPublishedRatingsByReceiverUserId(RECEIVER_ID))
                .thenReturn(2L);

        RatingSummary result = service.getRatingSummaryForUser(RECEIVER_ID);

        assertEquals(BigDecimal.valueOf(4.8), result.averagePoints());
        assertEquals(2, result.ratingCount());
    }

    @Test
    void getRatingSummaryForUser_shouldThrow_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getRatingSummaryForUser(null)
        );
    }

    @Test
    void getRating_shouldReturnRating_whenRatingExists() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getRating(RATING_ID);

        assertSame(rating, result);
    }

    @Test
    void getRating_shouldThrow_whenRatingIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getRating(null)
        );
    }

    @Test
    void getRating_shouldThrowNotFound_whenRatingDoesNotExist() {
        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.empty());

        assertThrows(SessionRatingNotFoundException.class, () ->
                service.getRating(RATING_ID)
        );
    }

    @Test
    void getVisibleRating_shouldReturnRatingForAdmin_evenIfPending() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getVisibleRating(RATING_ID, OTHER_USER_ID, true);

        assertSame(rating, result);
    }

    @Test
    void getVisibleRating_shouldReturnPendingRatingForSender() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getVisibleRating(RATING_ID, SENDER_ID, false);

        assertSame(rating, result);
    }

    @Test
    void getVisibleRating_shouldThrowForReceiver_whenRatingIsPending() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        assertThrows(SessionRatingNotAuthorizedException.class, () ->
                service.getVisibleRating(RATING_ID, RECEIVER_ID, false)
        );
    }

    @Test
    void getVisibleRating_shouldThrowForReceiver_whenRatingIsExpired() {
        SessionRating rating = expiredRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        assertThrows(SessionRatingNotAuthorizedException.class, () ->
                service.getVisibleRating(RATING_ID, RECEIVER_ID, false)
        );
    }

    @Test
    void getVisibleRating_shouldReturnPublishedRatingForReceiver() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getVisibleRating(RATING_ID, RECEIVER_ID, false);

        assertSame(rating, result);
    }

    @Test
    void getVisibleRating_shouldReturnPublishedRatingForUnrelatedUser() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getVisibleRating(RATING_ID, OTHER_USER_ID, false);

        assertSame(rating, result);
    }

    @Test
    void getVisibleRating_shouldThrowForUnrelatedUser_whenRatingIsPending() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        assertThrows(SessionRatingNotAuthorizedException.class, () ->
                service.getVisibleRating(RATING_ID, OTHER_USER_ID, false)
        );
    }

    @Test
    void getPublishedRating_shouldReturnRating_whenRatingIsPublished() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getPublishedRating(RATING_ID);

        assertSame(rating, result);
    }

    @Test
    void getPublishedRating_shouldThrow_whenRatingIsNotPublished() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        assertThrows(RuntimeException.class, () ->
                service.getPublishedRating(RATING_ID)
        );
    }

    @Test
    void getNonPublishedRating_shouldReturnRating_whenRatingIsPending() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getNonPublishedRating(RATING_ID);

        assertSame(rating, result);
    }

    @Test
    void getNonPublishedRating_shouldReturnRating_whenRatingIsExpired() {
        SessionRating rating = expiredRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        SessionRating result = service.getNonPublishedRating(RATING_ID);

        assertSame(rating, result);
    }

    @Test
    void getNonPublishedRating_shouldThrow_whenRatingIsPublished() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));

        assertThrows(RuntimeException.class, () ->
                service.getNonPublishedRating(RATING_ID)
        );
    }

    @Test
    void getAllRatings_shouldDelegateToRepository() {
        when(sessionRatingRepositoryPort.findAllRatings()).thenReturn(List.of());

        List<SessionRating> result = service.getAllRatings();

        assertNotNull(result);
        verify(sessionRatingRepositoryPort).findAllRatings();
    }

    @Test
    void getAllPublishedRatings_shouldDelegateToRepository() {
        when(sessionRatingRepositoryPort.findAllPublishedRatings()).thenReturn(List.of());

        List<SessionRating> result = service.getAllPublishedRatings();

        assertNotNull(result);
        verify(sessionRatingRepositoryPort).findAllPublishedRatings();
    }

    @Test
    void getAllNonPublishedRatings_shouldDelegateToRepository() {
        when(sessionRatingRepositoryPort.findAllNonPublishedRatings()).thenReturn(List.of());

        List<SessionRating> result = service.getAllNonPublishedRatings();

        assertNotNull(result);
        verify(sessionRatingRepositoryPort).findAllNonPublishedRatings();
    }

    @Test
    void getOwnRatings_shouldDelegateToRepository() {
        SessionRating sentPendingRating = pendingRating(SENDER_ID, RECEIVER_ID);
        SessionRating receivedPublishedRating = publishedRating(RECEIVER_ID, SENDER_ID);

        when(sessionRatingRepositoryPort.findOwnRatingsByUserId(SENDER_ID))
                .thenReturn(List.of(sentPendingRating, receivedPublishedRating));

        List<SessionRating> result = service.getOwnRatings(SENDER_ID);

        assertEquals(2, result.size());
        verify(sessionRatingRepositoryPort).findOwnRatingsByUserId(SENDER_ID);
    }

    @Test
    void getOwnRatings_shouldThrow_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getOwnRatings(null)
        );
    }

    @Test
    void getPublishedRatingsForUser_shouldDelegateToRepository() {
        when(sessionRatingRepositoryPort.findPublishedRatingsByReceiverUserId(RECEIVER_ID))
                .thenReturn(List.of());

        List<SessionRating> result = service.getPublishedRatingsForUser(RECEIVER_ID);

        assertNotNull(result);
        verify(sessionRatingRepositoryPort).findPublishedRatingsByReceiverUserId(RECEIVER_ID);
    }

    @Test
    void getPublishedRatingsForUser_shouldThrow_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getPublishedRatingsForUser(null)
        );
    }

    @Test
    void getAllRatingsForUser_shouldDelegateToRepository() {
        when(sessionRatingRepositoryPort.findAllRatingsByReceiverUserId(RECEIVER_ID))
                .thenReturn(List.of());

        List<SessionRating> result = service.getAllRatingsForUser(RECEIVER_ID);

        assertNotNull(result);
        verify(sessionRatingRepositoryPort).findAllRatingsByReceiverUserId(RECEIVER_ID);
    }

    @Test
    void getAllRatingsForUser_shouldThrow_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.getAllRatingsForUser(null)
        );
    }

    @Test
    void updateRatingStatus_shouldPublishRatingAndSetPublishedAt() {
        SessionRating rating = pendingRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));
        when(sessionRatingRepositoryPort.save(any(SessionRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionRating result = service.updateRatingStatus(RATING_ID, RatingStatus.PUBLISHED);

        assertEquals(RatingStatus.PUBLISHED, result.getStatus());
        assertNotNull(result.getPublishedAt());

        verify(sessionRatingRepositoryPort).save(rating);
    }

    @Test
    void updateRatingStatus_shouldUnpublishRatingAndClearPublishedAt() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));
        when(sessionRatingRepositoryPort.save(any(SessionRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionRating result = service.updateRatingStatus(RATING_ID, RatingStatus.PENDING);

        assertEquals(RatingStatus.PENDING, result.getStatus());
        assertNull(result.getPublishedAt());

        verify(sessionRatingRepositoryPort).save(rating);
    }

    @Test
    void updateRatingStatus_shouldSetExpiredAndClearPublishedAt() {
        SessionRating rating = publishedRating(SENDER_ID, RECEIVER_ID);

        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.of(rating));
        when(sessionRatingRepositoryPort.save(any(SessionRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SessionRating result = service.updateRatingStatus(RATING_ID, RatingStatus.EXPIRED);

        assertEquals(RatingStatus.EXPIRED, result.getStatus());
        assertNull(result.getPublishedAt());

        verify(sessionRatingRepositoryPort).save(rating);
    }

    @Test
    void updateRatingStatus_shouldThrow_whenRatingIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateRatingStatus(null, RatingStatus.PUBLISHED)
        );
    }

    @Test
    void updateRatingStatus_shouldThrow_whenStatusIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                service.updateRatingStatus(RATING_ID, null)
        );
    }

    @Test
    void updateRatingStatus_shouldThrowNotFound_whenRatingDoesNotExist() {
        when(sessionRatingRepositoryPort.findById(RATING_ID)).thenReturn(Optional.empty());

        assertThrows(SessionRatingNotFoundException.class, () ->
                service.updateRatingStatus(RATING_ID, RatingStatus.PUBLISHED)
        );
    }

    private SkillSession ratingOpenSession(LocalDateTime ratingWindowEndsAt) {
        SkillSession session = mock(SkillSession.class);

        lenient().when(session.hasParticipant(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID userId = invocation.getArgument(0);
                    return Set.of(SENDER_ID, RECEIVER_ID).contains(userId);
                });

        lenient().when(session.isBetween(SENDER_ID, RECEIVER_ID)).thenReturn(true);
        lenient().when(session.getStatus()).thenReturn(SessionStatus.RATING_OPEN);
        lenient().when(session.getRatingWindowEndsAt()).thenReturn(ratingWindowEndsAt);

        return session;
    }

    private SessionRating pendingRating(UUID senderUserId, UUID receiverUserId) {
        return SessionRating.create(
                SESSION_ID,
                senderUserId,
                receiverUserId,
                BigDecimal.valueOf(4.5),
                "Test rating"
        );
    }

    private SessionRating publishedRating(UUID senderUserId, UUID receiverUserId) {
        SessionRating rating = pendingRating(senderUserId, receiverUserId);
        rating.publish(LocalDateTime.now());
        return rating;
    }

    private SessionRating expiredRating(UUID senderUserId, UUID receiverUserId) {
        SessionRating rating = pendingRating(senderUserId, receiverUserId);
        rating.changeStatus(RatingStatus.EXPIRED, LocalDateTime.now());
        return rating;
    }
}