package de.thws.kompetenz.matching.adapter.out.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class GeminiEmbeddingAdapter implements EmbeddingClientPort {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final String apiKey;
    private final String model;

    public GeminiEmbeddingAdapter(
            @ConfigProperty(name = "gemini.api.key") Optional<String> apiKey,
            @ConfigProperty(name = "gemini.embedding.model", defaultValue = "gemini-embedding-001") String model
    ) {
        this.apiKey = apiKey.map(String::trim).orElse("");
        this.model = model;
    }

    @Override
    public List<Double> createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be null or blank");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured. Set gemini.api.key.");
        }

        String normalizedText = text.trim();
        String normalizedModel = normalizeModel(model);
        String endpoint = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent", normalizedModel);
        String requestBody = buildRequestBody(normalizedModel, normalizedText);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(String.format(
                        "Gemini embedding request failed with status %d: %s",
                        response.statusCode(),
                        abbreviate(response.body(), 200)
                ));
            }
            return parseEmbeddingFromResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to call Gemini embedding API", e);
        }
    }

    String buildRequestBody(String modelName, String text) {
        try {
            JsonNode root = OBJECT_MAPPER.createObjectNode()
                    .put("model", modelName)
                    .set("content", OBJECT_MAPPER.createObjectNode()
                            .set("parts", OBJECT_MAPPER.createArrayNode()
                                    .add(OBJECT_MAPPER.createObjectNode().put("text", text))));
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to build Gemini request body", e);
        }
    }

    List<Double> parseEmbeddingFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Gemini response body is empty");
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            JsonNode embeddingNode = root.path("embedding");
            if (embeddingNode.isArray() && embeddingNode.size() > 0) {
                return readDoubleList(embeddingNode);
            }
            JsonNode embeddingValues = embeddingNode.path("values");
            if (embeddingValues.isArray() && embeddingValues.size() > 0) {
                return readDoubleList(embeddingValues);
            }

            JsonNode dataNode = root.path("data");
            if (dataNode.isArray() && dataNode.size() > 0) {
                JsonNode first = dataNode.get(0);
                JsonNode dataEmbedding = first.path("embedding");
                if (dataEmbedding.isArray() && dataEmbedding.size() > 0) {
                    return readDoubleList(dataEmbedding);
                }
                JsonNode values = dataEmbedding.path("values");
                if (values.isArray() && values.size() > 0) {
                    return readDoubleList(values);
                }
            }

            throw new IllegalStateException("No embedding values found in Gemini response");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Gemini response", e);
        }
    }

    private List<Double> readDoubleList(JsonNode arrayNode) {
        List<Double> values = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (!item.isNumber()) {
                throw new IllegalStateException("Gemini embedding contains non-numeric value");
            }
            values.add(item.doubleValue());
        }
        return values;
    }

    private String normalizeModel(String configuredModel) {
        if (configuredModel == null || configuredModel.isBlank()) {
            throw new IllegalStateException("Gemini embedding model is not configured. Set gemini.embedding.model.");
        }
        return configuredModel.startsWith("models/") ? configuredModel.substring("models/".length()) : configuredModel;
    }

    private String abbreviate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
