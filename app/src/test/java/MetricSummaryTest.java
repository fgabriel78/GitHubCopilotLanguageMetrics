import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricSummaryTest {

    // Requirement 4.1: acceptanceRate() with known values
    @Test
    void acceptanceRate_withKnownValues_returnsCorrectPercentage() {
        MetricSummary summary = new MetricSummary("Java", 50, 25);
        assertEquals(50.0, summary.acceptanceRate(), 1e-9);
    }

    // Requirement 4.2: acceptanceRate() with zero suggestions returns 0.0
    @Test
    void acceptanceRate_withZeroSuggestions_returnsZero() {
        MetricSummary summary = new MetricSummary("Java", 0, 0);
        assertEquals(0.0, summary.acceptanceRate(), 1e-9);
    }

    // Requirement 4.3: merge() sums totalSuggestions and totalAcceptances
    @Test
    void merge_sumsTotalSuggestionsAndAcceptances() {
        MetricSummary a = new MetricSummary("Python", 30, 10);
        MetricSummary b = new MetricSummary("Python", 20, 15);
        MetricSummary merged = a.merge(b);
        assertEquals(50, merged.totalSuggestions());
        assertEquals(25, merged.totalAcceptances());
    }

    // Requirement 4.3: merge() preserves the language name from the receiver
    @Test
    void merge_preservesReceiverLanguageName() {
        MetricSummary receiver = new MetricSummary("TypeScript", 10, 5);
        MetricSummary other = new MetricSummary("JavaScript", 10, 5);
        MetricSummary merged = receiver.merge(other);
        assertEquals("TypeScript", merged.language());
    }
}
