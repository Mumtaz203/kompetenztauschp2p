package de.thws.kompetenz.matching.adapter.in.rest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SkillSearchTermsParser {

    private SkillSearchTermsParser() {
    }

    static List<String> parse(String skill, String skills) {
        Set<String> terms = new LinkedHashSet<>();
        appendTerms(terms, skill);
        appendTerms(terms, skills);
        return List.copyOf(terms);
    }

    private static void appendTerms(Set<String> terms, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        for (String part : raw.split("[,\\s]+")) {
            String normalized = part.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                terms.add(normalized);
            }
        }
    }

    static String validate(List<String> terms) {
        if (terms == null || terms.isEmpty()) {
            return "at least one search term is required";
        }
        for (String term : terms) {
            if (term.length() < 3) {
                return "each search term must be at least 3 characters";
            }
        }
        return null;
    }
}
