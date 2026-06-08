package de.thws.kompetenz.matching.application.service.scoring;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CosineSimilarityCalculator {

    public double calculate(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }

        if (left.size() != right.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double leftMagnitude = 0.0;
        double rightMagnitude = 0.0;

        for (int i = 0; i < left.size(); i++) {
            Double leftValue = left.get(i);
            Double rightValue = right.get(i);

            if (leftValue == null || rightValue == null) {
                return 0.0;
            }

            dotProduct += leftValue * rightValue;
            leftMagnitude += leftValue * leftValue;
            rightMagnitude += rightValue * rightValue;
        }

        double leftNorm = Math.sqrt(leftMagnitude);
        double rightNorm = Math.sqrt(rightMagnitude);

        if (leftNorm == 0.0 || rightNorm == 0.0) {
            return 0.0;
        }

        return dotProduct / (leftNorm * rightNorm);
    }
}
