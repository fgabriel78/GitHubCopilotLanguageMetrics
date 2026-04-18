import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

class CopilotClient {
    private static final String API_URL_TEMPLATE = "https://api.github.com/orgs/%s/copilot/metrics";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final Configuration config;
    private final String apiUrlTemplate;

    CopilotClient(Configuration config) {
        this(config, API_URL_TEMPLATE);
    }

    CopilotClient(Configuration config, String apiUrlTemplate) {
        this.config = config;
        this.apiUrlTemplate = apiUrlTemplate;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    String fetchMetrics() throws IOException, InterruptedException {
        String url = apiUrlTemplate.formatted(config.orgName());

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
