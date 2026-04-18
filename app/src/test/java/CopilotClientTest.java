import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CopilotClient.
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5
 */
class CopilotClientTest {

    private HttpServer server;
    private String baseUrl;

    // Configurable response state
    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<String> responseBody = new AtomicReference<>("");
    private final AtomicReference<String> capturedAuthHeader = new AtomicReference<>();
    private final AtomicReference<String> capturedAcceptHeader = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            capturedAuthHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            capturedAcceptHeader.set(exchange.getRequestHeaders().getFirst("Accept"));

            byte[] body = responseBody.get().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus.get(), body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port + "/";
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private CopilotClient clientWithOrg(String org) {
        // Use a URL template that routes to the stub server; %s is the org name placeholder
        String urlTemplate = baseUrl + "%s";
        Configuration config = new Configuration("test-token", org);
        return new CopilotClient(config, urlTemplate);
    }

    // Validates: Requirement 2.4 - HTTP 200 returns response body unchanged
    @Test
    void http200_returnsBodyUnchanged() throws IOException, InterruptedException {
        responseStatus.set(200);
        responseBody.set("{\"data\":\"metrics\"}");

        CopilotClient client = clientWithOrg("my-org");
        String result = client.fetchMetrics();

        assertEquals("{\"data\":\"metrics\"}", result);
    }

    // Validates: Requirement 2.5 - HTTP 401 throws IOException with URL, status, and body
    @Test
    void http401_throwsIOExceptionWithDetails() {
        responseStatus.set(401);
        responseBody.set("Unauthorized");

        CopilotClient client = clientWithOrg("my-org");
        IOException ex = assertThrows(IOException.class, client::fetchMetrics);

        String msg = ex.getMessage();
        assertTrue(msg.contains("my-org"), "Message should contain the org/URL: " + msg);
        assertTrue(msg.contains("401"), "Message should contain status code 401: " + msg);
        assertTrue(msg.contains("Unauthorized"), "Message should contain response body: " + msg);
    }

    // Validates: Requirement 2.5 - HTTP 404 throws IOException with URL, status, and body
    @Test
    void http404_throwsIOExceptionWithDetails() {
        responseStatus.set(404);
        responseBody.set("Not Found");

        CopilotClient client = clientWithOrg("my-org");
        IOException ex = assertThrows(IOException.class, client::fetchMetrics);

        String msg = ex.getMessage();
        assertTrue(msg.contains("my-org"), "Message should contain the org/URL: " + msg);
        assertTrue(msg.contains("404"), "Message should contain status code 404: " + msg);
        assertTrue(msg.contains("Not Found"), "Message should contain response body: " + msg);
    }

    // Validates: Requirement 2.5 - HTTP 500 throws IOException with URL, status, and body
    @Test
    void http500_throwsIOExceptionWithDetails() {
        responseStatus.set(500);
        responseBody.set("Internal Server Error");

        CopilotClient client = clientWithOrg("my-org");
        IOException ex = assertThrows(IOException.class, client::fetchMetrics);

        String msg = ex.getMessage();
        assertTrue(msg.contains("my-org"), "Message should contain the org/URL: " + msg);
        assertTrue(msg.contains("500"), "Message should contain status code 500: " + msg);
        assertTrue(msg.contains("Internal Server Error"), "Message should contain response body: " + msg);
    }

    // Validates: Requirement 2.2 - Authorization header is "Bearer <token>"
    @Test
    void request_includesAuthorizationBearerHeader() throws IOException, InterruptedException {
        responseStatus.set(200);
        responseBody.set("[]");

        CopilotClient client = clientWithOrg("my-org");
        client.fetchMetrics();

        assertEquals("Bearer test-token", capturedAuthHeader.get());
    }

    // Validates: Requirement 2.3 - Accept header is "application/vnd.github.v3+json"
    @Test
    void request_includesAcceptHeader() throws IOException, InterruptedException {
        responseStatus.set(200);
        responseBody.set("[]");

        CopilotClient client = clientWithOrg("my-org");
        client.fetchMetrics();

        assertEquals("application/vnd.github.v3+json", capturedAcceptHeader.get());
    }
}
