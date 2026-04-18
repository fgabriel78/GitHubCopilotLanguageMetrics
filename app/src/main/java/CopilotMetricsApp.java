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
}
