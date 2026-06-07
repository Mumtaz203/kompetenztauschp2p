package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillEmbeddingBackfillServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SkillEmbeddingService skillEmbeddingService;

    private SkillEmbeddingBackfillService skillEmbeddingBackfillService;

    @BeforeEach
    void setUp() {
        skillEmbeddingBackfillService = new SkillEmbeddingBackfillService(userRepositoryPort, skillEmbeddingService);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_returnsZeroResult_whenRepositoryReturnsEmptyList() {
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of());

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(0, 0, 0), result);
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_returnsZeroResult_whenRepositoryReturnsNull() {
        when(userRepositoryPort.findAllUsers()).thenReturn(null);

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(0, 0, 0), result);
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_skipsNullUsers() {
        User user = userWithOfferedSkills("user", "Java");
        when(userRepositoryPort.findAllUsers()).thenReturn(listWithNullAnd(user));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(user)).thenReturn(List.of(embedding(user.getId(), "Java")));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(1, 1, 1), result);
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(user);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_skipsUsersWithoutId() {
        User userWithoutId = new User();
        userWithoutId.setOfferedSkills(List.of("Java"));
        User validUser = userWithOfferedSkills("valid", "SQL");
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(userWithoutId, validUser));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(validUser)).thenReturn(List.of(embedding(validUser.getId(), "SQL")));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(1, 1, 1), result);
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(userWithoutId);
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(validUser);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_skipsUsersWithoutOfferedSkills() {
        User withoutOfferedSkills = new User(UUID.randomUUID(), "empty", "empty@test.com", "password");
        User withEmptyOfferedSkills = new User(UUID.randomUUID(), "empty-list", "empty-list@test.com", "password");
        withEmptyOfferedSkills.setOfferedSkills(List.of());
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(withoutOfferedSkills, withEmptyOfferedSkills));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(2, 0, 0), result);
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_callsEmbeddingServiceForUsersWithOfferedSkills() {
        User user = userWithOfferedSkills("user", "Java");
        SkillEmbedding embedding = embedding(user.getId(), "Java");
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(user));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(user)).thenReturn(List.of(embedding));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(1, 1, 1), result);
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(user);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_callsEmbeddingServiceForUsersWithWantedSkills() {
        User user = userWithWantedSkills("learner", "Kubernetes");
        SkillEmbedding embedding = wantedEmbedding(user.getId(), "Kubernetes");
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(user));
        when(skillEmbeddingService.ensureWantedSkillEmbeddings(user)).thenReturn(List.of(embedding));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(1, 1, 1), result);
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(user);
        verify(skillEmbeddingService).ensureWantedSkillEmbeddings(user);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_accumulatesOfferedAndWantedEmbeddingCounts() {
        User user = userWithOfferedSkills("full-profile", "Java");
        user.setWantedSkills(List.of("Kubernetes", "Docker"));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(user));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(user)).thenReturn(List.of(
                embedding(user.getId(), "Java")
        ));
        when(skillEmbeddingService.ensureWantedSkillEmbeddings(user)).thenReturn(List.of(
                wantedEmbedding(user.getId(), "Kubernetes"),
                wantedEmbedding(user.getId(), "Docker")
        ));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(1, 1, 3), result);
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(user);
        verify(skillEmbeddingService).ensureWantedSkillEmbeddings(user);
    }

    @Test
    void generateMissingOfferedSkillEmbeddingsForAllUsers_accumulatesResultCounts() {
        User javaUser = userWithOfferedSkills("java-user", "Java", "Spring");
        User sqlUser = userWithOfferedSkills("sql-user", "SQL");
        User emptyUser = new User(UUID.randomUUID(), "empty", "empty@test.com", "password");
        emptyUser.setOfferedSkills(List.of());
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(javaUser, emptyUser, sqlUser));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(javaUser)).thenReturn(List.of(
                embedding(javaUser.getId(), "Java"),
                embedding(javaUser.getId(), "Spring")
        ));
        when(skillEmbeddingService.ensureOfferedSkillEmbeddings(sqlUser)).thenReturn(List.of(
                embedding(sqlUser.getId(), "SQL")
        ));

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();

        assertEquals(new BackfillResult(3, 2, 3), result);
    }

    @Test
    void skillEmbeddingBackfillService_doesNotDependOnEmbeddingClientPortDirectly() {
        for (Field field : SkillEmbeddingBackfillService.class.getDeclaredFields()) {
            assertFalse(
                    EmbeddingClientPort.class.equals(field.getType()),
                    "Backfill service must use SkillEmbeddingService instead of calling Gemini directly"
            );
        }
    }

    private static User userWithOfferedSkills(String username, String... offeredSkills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "password");
        user.setOfferedSkills(List.of(offeredSkills));
        return user;
    }

    private static User userWithWantedSkills(String username, String... wantedSkills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "password");
        user.setWantedSkills(List.of(wantedSkills));
        return user;
    }

    private static SkillEmbedding embedding(UUID userId, String skillText) {
        return new SkillEmbedding(UUID.randomUUID(), userId, skillText, SkillType.OFFERED, List.of(0.1, 0.2));
    }

    private static SkillEmbedding wantedEmbedding(UUID userId, String skillText) {
        return new SkillEmbedding(UUID.randomUUID(), userId, skillText, SkillType.WANTED, List.of(0.1, 0.2));
    }

    private static List<User> listWithNullAnd(User user) {
        java.util.ArrayList<User> users = new java.util.ArrayList<>();
        users.add(null);
        users.add(user);
        return users;
    }
}
