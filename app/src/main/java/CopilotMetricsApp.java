import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application to retrieve and analyze GitHub Copilot metrics for an
 * organization.
 * Optimized for Java 21 with Records, improved error handling, and modular
 * design.
 */
public class CopilotMetricsApp {

    private static final Logger LOGGER = Logger.getLogger(CopilotMetricsApp.class.getName());
    private static final String CONFIG_FILE_PATH = "app/src/main/resources/config.properties";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args) {
        try {
            var config = Configuration.load(CONFIG_FILE_PATH);
            var client = new CopilotClient(config);

            String jsonResponse = client.fetchMetrics();
            var metrics = MetricsProcessor.process(jsonResponse);

            MetricsPrinter.print(metrics);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Application failed unexpectedly: {0}", e.getMessage());
            // In a real CLI, we might want to System.exit(1), but for this app clean return
            // is fine.
        }
    }

    // -------------------------------------------------------------------------
    // Configuration Record
    // -------------------------------------------------------------------------
    private record Configuration(String githubToken, String orgName) {
        static Configuration load(String path) throws IOException {
            var props = new Properties();
            try (InputStream input = Files.newInputStream(Path.of(path))) {
                props.load(input);
            }

            String token = props.getProperty("GITHUB_TOKEN");
            String org = props.getProperty("ORG_NAME");

            if (token == null || token.isBlank() || org == null || org.isBlank()) {
                throw new IllegalStateException("Missing required configuration: GITHUB_TOKEN or ORG_NAME in " + path);
            }
            return new Configuration(token, org);
        }
    }

    // -------------------------------------------------------------------------
    // API Client
    // -------------------------------------------------------------------------
    private static class CopilotClient {
        private static final String API_URL_TEMPLATE = "https://api.github.com/orgs/%s/copilot/metrics";
        private static final Duration TIMEOUT = Duration.ofSeconds(30);

        private final HttpClient httpClient;
        private final Configuration config;

        CopilotClient(Configuration config) {
            this.config = config;
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(TIMEOUT)
                    .build();
        }

        String fetchMetrics() throws IOException, InterruptedException {
            String url = API_URL_TEMPLATE.formatted(config.orgName());

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Authorization", "Bearer " + config.githubToken())
                    .GET()
                    .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to fetch metrics using URL: " + url +
                        ". Status: " + response.statusCode() + ", Body: " + response.body());
            }

            return response.body();
        }
    }

    // -------------------------------------------------------------------------
    // Domain Models & Processing
    // -------------------------------------------------------------------------
    private record MetricSummary(String language, long totalSuggestions, long totalAcceptances) {
        double acceptanceRate() {
            return totalSuggestions == 0 ? 0.0 : (double) totalAcceptances / totalSuggestions * 100.0;
        }

        MetricSummary merge(MetricSummary other) {
            return new MetricSummary(
                    language,
                    this.totalSuggestions + other.totalSuggestions,
                    this.totalAcceptances + other.totalAcceptances);
        }
    }

    private static class MetricsProcessor {

        static Map<String, MetricSummary> process(String jsonResponse) throws IOException {
            JsonNode rootNode = OBJECT_MAPPER.readTree(jsonResponse);

            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("Invalid API response: Expected JSON Array.");
            }

            // Using pure stream-like reduction could be complex due to nested JSON
            // structure.
            // A hybrid approach with nested for-loops inside a processor method is often
            // more readable
            // for deep JSON traversal, but let's try to be clean.

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

        // Helper to safely navigate basic paths and return Optional
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

    // -------------------------------------------------------------------------
    // Presentation Layer
    // -------------------------------------------------------------------------
    private static class MetricsPrinter {
        static void print(Map<String, MetricSummary> metrics) {
            System.out.println("--- Consolidated Copilot Acceptance Statistics by Language ---");

            metrics.values().stream()
                    .filter(m -> m.totalSuggestions > 0)
                    .sorted(Comparator.comparingDouble(MetricSummary::acceptanceRate).reversed())
                    .forEach(MetricsPrinter::printMetric);
        }

        private static void printMetric(MetricSummary m) {
            System.out.printf("""
                    🔹 **%s**
                      - Acceptance Rate: **%.2f%%**
                      - Total Suggestions: %d, Total Acceptances: %d
                    ---
                    """,
                    m.language(),
                    m.acceptanceRate(),
                    m.totalSuggestions(),
                    m.totalAcceptances());
        }
    }
}
