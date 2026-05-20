package de.thws.kompetenz.matching.adapter.in.rest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SkillSearchTermsParserTest {

    @Test
    void parse_splitsCommaSeparatedTerms() {
        assertEquals(List.of("sql", "java"), SkillSearchTermsParser.parse(null, "sql,java"));
    }

    @Test
    void parse_splitsWhitespaceSeparatedTerms() {
        assertEquals(List.of("sql", "java"), SkillSearchTermsParser.parse(null, "sql java"));
    }

    @Test
    void parse_normalizesTrimAndLowercase() {
        assertEquals(List.of("sql", "java", "spring"),
                SkillSearchTermsParser.parse(null, " SQL, Java   spring "));
    }

    @Test
    void parse_removesDuplicateTermsWhilePreservingOrder() {
        assertEquals(List.of("sql", "java"), SkillSearchTermsParser.parse(null, "sql, sql, JAVA"));
    }

    @Test
    void parse_combinesSkillAndSkillsParameters() {
        assertEquals(List.of("java", "sql"), SkillSearchTermsParser.parse("java", "sql"));
    }

    @Test
    void parse_returnsEmptyListForBlankInput() {
        assertEquals(List.of(), SkillSearchTermsParser.parse(null, null));
        assertEquals(List.of(), SkillSearchTermsParser.parse("  ", ","));
    }

    @Test
    void validate_requiresAtLeastOneTerm() {
        assertNotNull(SkillSearchTermsParser.validate(List.of()));
    }

    @Test
    void validate_requiresMinimumTermLength() {
        assertEquals("each search term must be at least 3 characters",
                SkillSearchTermsParser.validate(List.of("js")));
    }

    @Test
    void validate_acceptsValidTerms() {
        assertNull(SkillSearchTermsParser.validate(List.of("sql", "java")));
    }
}
