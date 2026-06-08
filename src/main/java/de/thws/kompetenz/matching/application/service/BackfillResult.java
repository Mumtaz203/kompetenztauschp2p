package de.thws.kompetenz.matching.application.service;

public record BackfillResult(
        int usersChecked,
        int usersWithOfferedSkills,
        int embeddingsEnsured
) {
}
