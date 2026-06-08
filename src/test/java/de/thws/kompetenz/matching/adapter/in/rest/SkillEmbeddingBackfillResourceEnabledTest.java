package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.application.service.BackfillResult;
import de.thws.kompetenz.matching.application.service.SkillEmbeddingBackfillService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(SkillEmbeddingBackfillResourceEnabledTest.EnabledBackfillProfile.class)
class SkillEmbeddingBackfillResourceEnabledTest {

    @InjectMock
    SkillEmbeddingBackfillService skillEmbeddingBackfillService;

    @Test
    void backfillEmbeddings_returnsBackfillResult_whenEnabled() {
        when(skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers())
                .thenReturn(new BackfillResult(4, 2, 5));

        given()
                .when()
                .post("/internal/embeddings/backfill")
                .then()
                .statusCode(200)
                .body("usersChecked", equalTo(4))
                .body("usersWithOfferedSkills", equalTo(2))
                .body("embeddingsEnsured", equalTo(5));

        verify(skillEmbeddingBackfillService).generateMissingOfferedSkillEmbeddingsForAllUsers();
    }

    public static class EnabledBackfillProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("embedding.backfill.enabled", "true");
        }
    }
}
