package de.thws.kompetenz.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RestAssuredStatusAssert {
    private RestAssuredStatusAssert() {
    }

    public static void assertStatus(int expectedStatus, StatusAssertion assertion) {
        try {
            assertion.execute();
        } catch (Exception e) {
            assertTrue(
                    e.getMessage() != null && e.getMessage().contains("status code: " + expectedStatus),
                    "Expected HTTP status " + expectedStatus + " but got: " + e.getMessage()
            );
        }
    }

    @FunctionalInterface
    public interface StatusAssertion {
        void execute();
    }
}
