package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.application.service.SkillEmbeddingBackfillService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static de.thws.kompetenz.common.RestAssuredStatusAssert.assertStatus;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@QuarkusTest
class SkillEmbeddingBackfillResourceDisabledTest {

    @InjectMock
    SkillEmbeddingBackfillService skillEmbeddingBackfillService;

    @Test
    void backfillEmbeddings_returns403AndDoesNotRunService_whenDisabledByDefault() {
        assertStatus(403, () -> given()
                .when()
                .post("/internal/embeddings/backfill")
                .then()
                .statusCode(403));

        verify(skillEmbeddingBackfillService, never()).generateMissingOfferedSkillEmbeddingsForAllUsers();
    }
}
