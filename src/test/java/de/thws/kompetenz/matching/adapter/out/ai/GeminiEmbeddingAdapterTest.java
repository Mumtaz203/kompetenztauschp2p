package de.thws.kompetenz.matching.adapter.out.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeminiEmbeddingAdapterTest {

    @Test
    void parseEmbeddingFromResponse_shouldReadEmbeddingArrayRoot() {
        GeminiEmbeddingAdapter adapter = new GeminiEmbeddingAdapter(Optional.of("dummy-key"), "gemini-embedding-001");
        String response = "{\"embedding\":[0.12,0.34,0.56]}";

        List<Double> embedding = adapter.parseEmbeddingFromResponse(response);

        assertEquals(List.of(0.12, 0.34, 0.56), embedding);
    }

    @Test
    void parseEmbeddingFromResponse_shouldReadGeminiEmbeddingValues() {
        GeminiEmbeddingAdapter adapter = new GeminiEmbeddingAdapter(Optional.of("dummy-key"), "gemini-embedding-001");
        String response = "{\"embedding\":{\"values\":[0.12,0.34,0.56]}}";

        List<Double> embedding = adapter.parseEmbeddingFromResponse(response);

        assertEquals(List.of(0.12, 0.34, 0.56), embedding);
    }

    @Test
    void parseEmbeddingFromResponse_shouldReadEmbeddingFromDataArray() {
        GeminiEmbeddingAdapter adapter = new GeminiEmbeddingAdapter(Optional.of("dummy-key"), "gemini-embedding-001");
        String response = "{\"data\":[{\"embedding\":[0.1,0.2], \"metadata\":{}}]}";

        List<Double> embedding = adapter.parseEmbeddingFromResponse(response);

        assertEquals(List.of(0.1, 0.2), embedding);
    }

    @Test
    void parseEmbeddingFromResponse_shouldThrowWhenNoEmbeddingValues() {
        GeminiEmbeddingAdapter adapter = new GeminiEmbeddingAdapter(Optional.of("dummy-key"), "gemini-embedding-001");
        String response = "{\"data\":[]}";

        assertThrows(IllegalStateException.class, () -> adapter.parseEmbeddingFromResponse(response));
    }
}
