import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class MetricsProcessor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static Map<String, MetricSummary> process(String jsonResponse) throws IOException {
        JsonNode rootNode = OBJECT_MAPPER.readTree(jsonResponse);

        if (!rootNode.isArray()) {
            throw new IllegalArgumentException("Invalid API response: Expected JSON Array.");
        }

        Map<String, MetricSummary> consolidated = new HashMap<>();

        for (JsonNode dailyMetric : rootNode) {
            processDailyMetric(dailyMetric, consolidated);
        }

        return consolidated;
    }

    private static void processDailyMetric(JsonNode dailyMetric, Map<String, MetricSummary> consolidated) {
        path(dailyMetric, "copilot_ide_code_completions", "editors")
                .ifPresent(editorsNode -> {
                    for (JsonNode editor : editorsNode) {
                        processEditor(editor, consolidated);
                    }
                });
    }

    private static void processEditor(JsonNode editorNode, Map<String, MetricSummary> consolidated) {
        path(editorNode, "models")
                .ifPresent(modelsNode -> {
                    for (JsonNode model : modelsNode) {
                        processModel(model, consolidated);
                    }
                });
    }

    private static void processModel(JsonNode modelNode, Map<String, MetricSummary> consolidated) {
        path(modelNode, "languages")
                .ifPresent(languagesNode -> {
                    for (JsonNode lang : languagesNode) {
                        String name = lang.path("name").asText("unknown");
                        long suggestions = lang.path("total_code_suggestions").asLong(0);
                        long acceptances = lang.path("total_code_acceptances").asLong(0);

                        var newMetric = new MetricSummary(name, suggestions, acceptances);
                        consolidated.merge(name, newMetric, MetricSummary::merge);
                    }
                });
    }

    private static Optional<JsonNode> path(JsonNode node, String... keys) {
        JsonNode current = node;
        for (String key : keys) {
            current = current.get(key);
            if (current == null)
                return Optional.empty();
        }
        return current.isArray() ? Optional.of(current) : Optional.empty();
    }
}
