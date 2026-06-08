package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillEmbeddingServiceTest {

    @Mock
    private EmbeddingClientPort embeddingClientPort;

    @Mock
    private SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort;

    private SkillEmbeddingService skillEmbeddingService;

    @BeforeEach
    void setUp() {
        skillEmbeddingService = new SkillEmbeddingService(embeddingClientPort, skillEmbeddingRepositoryPort);
    }

    @Test
    void ensureOfferedSkillEmbeddings_returnsEmptyList_whenUserIsNull() {
        assertTrue(skillEmbeddingService.ensureOfferedSkillEmbeddings(null).isEmpty());
    }

    @Test
    void ensureOfferedSkillEmbeddings_returnsEmptyList_whenUserIdIsNull() {
        User user = new User();
        user.setOfferedSkills(List.of("java"));

        assertTrue(skillEmbeddingService.ensureOfferedSkillEmbeddings(user).isEmpty());
    }

    @Test
    void ensureOfferedSkillEmbeddings_returnsEmptyList_whenOfferedSkillsNullOrEmpty() {
        User user = new User(UUID.randomUUID(), "user", "user@test.com", "password");
        assertTrue(skillEmbeddingService.ensureOfferedSkillEmbeddings(user).isEmpty());

        user.setOfferedSkills(List.of());
        assertTrue(skillEmbeddingService.ensureOfferedSkillEmbeddings(user).isEmpty());
    }

    @Test
    void ensureOfferedSkillEmbeddings_createsEmbeddingsForNewOfferedSkills() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setOfferedSkills(List.of("Java", "Spring Boot"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());
        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Spring Boot"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Java"))).thenReturn(List.of(0.1, 0.2));
        when(embeddingClientPort.createEmbedding(eq("Spring Boot"))).thenReturn(List.of(0.3, 0.4));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);

        assertEquals(2, embeddings.size());
        assertEquals("Java", embeddings.get(0).getSkillText());
        assertEquals(SkillType.OFFERED, embeddings.get(0).getSkillType());
        assertEquals(List.of(0.1, 0.2), embeddings.get(0).getEmbedding());
        assertEquals("Spring Boot", embeddings.get(1).getSkillText());
    }

    @Test
    void ensureOfferedSkillEmbeddings_skipsNewEmbeddings_whenEmbeddingApiDisabled() {
        SkillEmbeddingService disabledService = new SkillEmbeddingService(
                embeddingClientPort,
                skillEmbeddingRepositoryPort,
                false,
                Optional.empty()
        );
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setOfferedSkills(List.of("Java"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());

        List<SkillEmbedding> embeddings = disabledService.ensureOfferedSkillEmbeddings(user);

        assertTrue(embeddings.isEmpty());
        verify(embeddingClientPort, never()).createEmbedding(any());
        verify(skillEmbeddingRepositoryPort, never()).save(any(SkillEmbedding.class));
    }

    @Test
    void ensureOfferedSkillEmbeddings_skipsNewEmbeddings_whenApiKeyMissing() {
        SkillEmbeddingService missingKeyService = new SkillEmbeddingService(
                embeddingClientPort,
                skillEmbeddingRepositoryPort,
                true,
                Optional.empty()
        );
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setOfferedSkills(List.of("Java"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());

        List<SkillEmbedding> embeddings = missingKeyService.ensureOfferedSkillEmbeddings(user);

        assertTrue(embeddings.isEmpty());
        verify(embeddingClientPort, never()).createEmbedding(any());
        verify(skillEmbeddingRepositoryPort, never()).save(any(SkillEmbedding.class));
    }

    @Test
    void ensureOfferedSkillEmbeddings_reusesExistingEmbeddings_withoutCallingEmbeddingClient() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setOfferedSkills(List.of("Java"));

        SkillEmbedding existing = new SkillEmbedding(UUID.randomUUID(), userId, "Java", SkillType.OFFERED, List.of(1.0, 2.0));
        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.of(existing));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        assertEquals(existing, embeddings.get(0));
        verify(embeddingClientPort, never()).createEmbedding(any());
    }

    @Test
    void ensureOfferedSkillEmbeddings_deduplicatesOfferedSkills_beforeCreatingEmbeddings() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setOfferedSkills(List.of("Java", " java ", "JAVA"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Java"))).thenReturn(List.of(0.5));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        verify(embeddingClientPort).createEmbedding(eq("Java"));
    }

    @Test
    void ensureOfferedSkillEmbeddings_ignoresNullAndBlankSkills() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        List<String> skills = new java.util.ArrayList<>();
        skills.add(null);
        skills.add("  ");
        skills.add("Java");
        user.setOfferedSkills(skills);

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Java"), eq(SkillType.OFFERED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Java"))).thenReturn(List.of(0.7));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        assertEquals("Java", embeddings.get(0).getSkillText());
    }

    @Test
    void ensureWantedSkillEmbeddings_returnsEmptyList_whenUserIsNull() {
        assertTrue(skillEmbeddingService.ensureWantedSkillEmbeddings(null).isEmpty());
    }

    @Test
    void ensureWantedSkillEmbeddings_returnsEmptyList_whenUserIdIsNull() {
        User user = new User();
        user.setWantedSkills(List.of("spring"));

        assertTrue(skillEmbeddingService.ensureWantedSkillEmbeddings(user).isEmpty());
    }

    @Test
    void ensureWantedSkillEmbeddings_returnsEmptyList_whenWantedSkillsNullOrEmpty() {
        User user = new User(UUID.randomUUID(), "user", "user@test.com", "password");
        assertTrue(skillEmbeddingService.ensureWantedSkillEmbeddings(user).isEmpty());

        user.setWantedSkills(List.of());
        assertTrue(skillEmbeddingService.ensureWantedSkillEmbeddings(user).isEmpty());
    }

    @Test
    void ensureWantedSkillEmbeddings_createsEmbeddingsForNewWantedSkills() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setWantedSkills(List.of("Kubernetes", "Docker"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Kubernetes"), eq(SkillType.WANTED)))
                .thenReturn(Optional.empty());
        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Docker"), eq(SkillType.WANTED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Kubernetes"))).thenReturn(List.of(0.1, 0.2));
        when(embeddingClientPort.createEmbedding(eq("Docker"))).thenReturn(List.of(0.3, 0.4));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureWantedSkillEmbeddings(user);

        assertEquals(2, embeddings.size());
        assertEquals("Kubernetes", embeddings.get(0).getSkillText());
        assertEquals(SkillType.WANTED, embeddings.get(0).getSkillType());
        assertEquals(List.of(0.1, 0.2), embeddings.get(0).getEmbedding());
        assertEquals("Docker", embeddings.get(1).getSkillText());
    }

    @Test
    void ensureWantedSkillEmbeddings_reusesExistingEmbeddings_withoutCallingEmbeddingClient() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setWantedSkills(List.of("Kubernetes"));

        SkillEmbedding existing = new SkillEmbedding(UUID.randomUUID(), userId, "Kubernetes", SkillType.WANTED, List.of(1.0, 2.0));
        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Kubernetes"), eq(SkillType.WANTED)))
                .thenReturn(Optional.of(existing));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureWantedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        assertEquals(existing, embeddings.get(0));
        verify(embeddingClientPort, never()).createEmbedding(any());
        verify(skillEmbeddingRepositoryPort, never()).save(any(SkillEmbedding.class));
    }

    @Test
    void ensureWantedSkillEmbeddings_deduplicatesWantedSkills_beforeCreatingEmbeddings() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        user.setWantedSkills(List.of("Kubernetes", " kubernetes ", "KUBERNETES"));

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Kubernetes"), eq(SkillType.WANTED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Kubernetes"))).thenReturn(List.of(0.5));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureWantedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        verify(embeddingClientPort, times(1)).createEmbedding(eq("Kubernetes"));
    }

    @Test
    void ensureWantedSkillEmbeddings_ignoresNullAndBlankSkills() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user", "user@test.com", "password");
        List<String> skills = new java.util.ArrayList<>();
        skills.add(null);
        skills.add("  ");
        skills.add("Kubernetes");
        user.setWantedSkills(skills);

        when(skillEmbeddingRepositoryPort.findByUserIdAndSkillTextAndSkillType(eq(userId), eq("Kubernetes"), eq(SkillType.WANTED)))
                .thenReturn(Optional.empty());
        when(embeddingClientPort.createEmbedding(eq("Kubernetes"))).thenReturn(List.of(0.7));
        when(skillEmbeddingRepositoryPort.save(any(SkillEmbedding.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<SkillEmbedding> embeddings = skillEmbeddingService.ensureWantedSkillEmbeddings(user);

        assertEquals(1, embeddings.size());
        assertEquals("Kubernetes", embeddings.get(0).getSkillText());
    }
}
