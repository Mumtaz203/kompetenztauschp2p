package de.thws.kompetenz.matching.application.port.out;

import java.util.List;

public interface EmbeddingClientPort {

    List<Double> createEmbedding(String text);
}
