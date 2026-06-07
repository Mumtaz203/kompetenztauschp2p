package de.thws.kompetenz.user.application.service;

import de.thws.kompetenz.matching.application.service.SkillEmbeddingService;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SkillEmbeddingService skillEmbeddingService;

    private UpdateUserProfileService updateUserProfileService;

    @BeforeEach
    void setUp() {
        updateUserProfileService = new UpdateUserProfileService(userRepositoryPort, skillEmbeddingService);
    }

    @Test
    void updateSkills_triggersEmbeddingGeneration_whenOfferedSkillsPresent() {
        UUID userId = UUID.randomUUID();
        User existing = new User(userId, "user", "u@test.com", "pwd");
        when(userRepositoryPort.findUserById(eq(userId))).thenReturn(Optional.of(existing));

        User saved = new User(userId, "user", "u@test.com", "pwd");
        saved.setOfferedSkills(List.of("Java"));
        saved.setWantedSkills(List.of());
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);

        updateUserProfileService.updateSkills(userId, List.of("Java"), List.of());

        verify(userRepositoryPort).save(any(User.class));
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(eq(saved));
    }

    @Test
    void updateSkills_triggersWantedEmbeddingGeneration_whenWantedSkillsPresent() {
        UUID userId = UUID.randomUUID();
        User existing = new User(userId, "user", "u@test.com", "pwd");
        when(userRepositoryPort.findUserById(eq(userId))).thenReturn(Optional.of(existing));

        User saved = new User(userId, "user", "u@test.com", "pwd");
        saved.setOfferedSkills(List.of());
        saved.setWantedSkills(List.of("spring"));
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);

        updateUserProfileService.updateSkills(userId, List.of(), List.of("Spring"));

        verify(userRepositoryPort).save(any(User.class));
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(any());
        verify(skillEmbeddingService).ensureWantedSkillEmbeddings(eq(saved));
    }

    @Test
    void updateSkills_doesNotTriggerEmbeddingGeneration_whenNoOfferedSkills() {
        UUID userId = UUID.randomUUID();
        User existing = new User(userId, "user", "u@test.com", "pwd");
        when(userRepositoryPort.findUserById(eq(userId))).thenReturn(Optional.of(existing));

        User saved = new User(userId, "user", "u@test.com", "pwd");
        saved.setOfferedSkills(List.of());
        saved.setWantedSkills(List.of());
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);

        updateUserProfileService.updateSkills(userId, List.of(), List.of());

        verify(userRepositoryPort).save(any(User.class));
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(any());
        verify(skillEmbeddingService, never()).ensureWantedSkillEmbeddings(any());
    }

    @Test
    void updateUser_triggersEmbeddingGeneration_whenOfferedSkillsPresent() {
        UUID userId = UUID.randomUUID();
        User existing = new User(userId, "user", "u@test.com", "pwd");
        when(userRepositoryPort.findUserById(eq(userId))).thenReturn(Optional.of(existing));

        User incoming = new User();
        incoming.setOfferedSkills(List.of("Go"));

        User saved = new User(userId, "user", "u@test.com", "pwd");
        saved.setOfferedSkills(List.of("Go"));
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);

        updateUserProfileService.updateUser(userId, incoming);

        verify(userRepositoryPort).save(any(User.class));
        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(eq(saved));
    }

    @Test
    void updateUser_triggersWantedEmbeddingGeneration_whenWantedSkillsPresent() {
        UUID userId = UUID.randomUUID();
        User existing = new User(userId, "user", "u@test.com", "pwd");
        when(userRepositoryPort.findUserById(eq(userId))).thenReturn(Optional.of(existing));

        User incoming = new User();
        incoming.setWantedSkills(List.of("Docker"));

        User saved = new User(userId, "user", "u@test.com", "pwd");
        saved.setWantedSkills(List.of("docker"));
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);

        updateUserProfileService.updateUser(userId, incoming);

        verify(userRepositoryPort).save(any(User.class));
        verify(skillEmbeddingService, never()).ensureOfferedSkillEmbeddings(any());
        verify(skillEmbeddingService).ensureWantedSkillEmbeddings(eq(saved));
    }
}
