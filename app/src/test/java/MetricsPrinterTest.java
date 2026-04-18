import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5
class MetricsPrinterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void redirectStreams() {
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // Test 1: Header line is printed before any language entry
    // Validates: Requirements 5.1
    @Test
    void print_alwaysPrintsHeaderFirst() {
        Map<String, MetricSummary> metrics = Map.of(
                "java", new MetricSummary("java", 100, 30)
        );

        MetricsPrinter.print(metrics);

        String output = outContent.toString();
        assertTrue(output.startsWith("--- Consolidated Copilot Acceptance Statistics by Language ---"),
                "Output should start with the header line");
    }

    // Test 2: Language with totalSuggestions == 0 is omitted from output
    // Validates: Requirements 5.4
    @Test
    void print_omitsLanguageWithZeroSuggestions() {
        Map<String, MetricSummary> metrics = Map.of(
                "java", new MetricSummary("java", 0, 0)
        );

        MetricsPrinter.print(metrics);

        String output = outContent.toString();
        assertFalse(output.contains("java"),
                "Language with zero suggestions should not appear in output");
    }

    // Test 3: Language with totalSuggestions > 0 appears with name, acceptance rate (2 decimal places),
    //         suggestions count, and acceptances count
    // Validates: Requirements 5.3
    @Test
    void print_includesNameAcceptanceRateSuggestionsAndAcceptances() {
        Map<String, MetricSummary> metrics = Map.of(
                "python", new MetricSummary("python", 200, 50)
        );

        MetricsPrinter.print(metrics);

        String output = outContent.toString();
        assertTrue(output.contains("python"), "Output should contain the language name");
        // acceptance rate = 50/200 * 100 = 25.00% — decimal separator varies by locale (e.g. "25.00" or "25,00")
        assertTrue(output.matches("(?s).*25[.,]00.*"),
                "Output should contain acceptance rate formatted to 2 decimal places");
        assertTrue(output.contains("200"), "Output should contain total suggestions count");
        assertTrue(output.contains("50"), "Output should contain total acceptances count");
    }

    // Test 4: With two languages, the one with higher acceptance rate appears first
    // Validates: Requirements 5.2
    @Test
    void print_sortsLanguagesByAcceptanceRateDescending() {
        // java: 30/100 = 30.00%, python: 80/100 = 80.00%
        Map<String, MetricSummary> metrics = Map.of(
                "java", new MetricSummary("java", 100, 30),
                "python", new MetricSummary("python", 100, 80)
        );

        MetricsPrinter.print(metrics);

        String output = outContent.toString();
        int javaIndex = output.indexOf("java");
        int pythonIndex = output.indexOf("python");
        assertTrue(pythonIndex < javaIndex,
                "python (80% acceptance rate) should appear before java (30% acceptance rate)");
    }

    // Test 5: All output goes to System.out, not System.err
    // Validates: Requirements 5.5
    @Test
    void print_writesOnlyToSystemOut() {
        Map<String, MetricSummary> metrics = Map.of(
                "typescript", new MetricSummary("typescript", 50, 25)
        );

        MetricsPrinter.print(metrics);

        assertFalse(outContent.toString().isEmpty(), "System.out should have content");
        assertTrue(errContent.toString().isEmpty(), "System.err should be empty");
    }
}
