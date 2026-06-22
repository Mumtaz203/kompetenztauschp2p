package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.application.service.BackfillResult;
import de.thws.kompetenz.matching.application.service.SkillEmbeddingBackfillService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@Path("/internal/embeddings/backfill")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SkillEmbeddingBackfillResource {

    private final SkillEmbeddingBackfillService skillEmbeddingBackfillService;
    private final boolean backfillEnabled;

    public SkillEmbeddingBackfillResource(
            SkillEmbeddingBackfillService skillEmbeddingBackfillService,
            @ConfigProperty(name = "embedding.backfill.enabled", defaultValue = "false") boolean backfillEnabled
    ) {
        this.skillEmbeddingBackfillService = skillEmbeddingBackfillService;
        this.backfillEnabled = backfillEnabled;
    }

    @POST//not sure if we need to implement the guard thing here
    public Response backfillEmbeddings() {
        if (!backfillEnabled) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", "Embedding backfill is disabled"))
                    .build();
        }

        BackfillResult result = skillEmbeddingBackfillService.generateMissingOfferedSkillEmbeddingsForAllUsers();
        return Response.ok(result).build();
    }
}
