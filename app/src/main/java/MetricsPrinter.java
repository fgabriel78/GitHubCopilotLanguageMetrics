import java.util.Comparator;
import java.util.Map;

class MetricsPrinter {
    static void print(Map<String, MetricSummary> metrics) {
        System.out.println("--- Consolidated Copilot Acceptance Statistics by Language ---");

        metrics.values().stream()
                .filter(m -> m.totalSuggestions() > 0)
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
