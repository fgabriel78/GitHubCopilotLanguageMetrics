import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MetricsProcessorTest {

    // Test 1: Valid JSON array with one daily entry, one editor, one model, one language parses correctly
    // Validates: Requirements 3.1, 3.3, 3.4, 3.8
    @Test
    void validJsonArray_singleEntry_parsesLanguageCorrectly() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "java",
                                  "total_code_suggestions": 100,
                                  "total_code_acceptances": 30
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("java"), "Result should contain 'java' language");
        MetricSummary summary = result.get("java");
        assertEquals("java", summary.language());
        assertEquals(100, summary.totalSuggestions());
        assertEquals(30, summary.totalAcceptances());
    }

    // Test 2: Non-array root JSON (object {}) throws IllegalArgumentException
    // Validates: Requirements 3.2
    @Test
    void nonArrayRoot_object_throwsIllegalArgumentException() {
        String json = "{}";
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricsProcessor.process(json)
        );
    }

    // Test 3: Non-array root JSON (string "hello") throws IllegalArgumentException
    // Validates: Requirements 3.2
    @Test
    void nonArrayRoot_string_throwsIllegalArgumentException() {
        String json = "\"hello\"";
        assertThrows(
                IllegalArgumentException.class,
                () -> MetricsProcessor.process(json)
        );
    }

    // Test 4: Language node missing "name" field defaults to "unknown"
    // Validates: Requirements 3.6
    @Test
    void languageMissingName_defaultsToUnknown() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "total_code_suggestions": 50,
                                  "total_code_acceptances": 10
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("unknown"), "Result should contain 'unknown' language");
        assertEquals(50, result.get("unknown").totalSuggestions());
        assertEquals(10, result.get("unknown").totalAcceptances());
    }

    // Test 5: Language node missing "total_code_suggestions" defaults to 0
    // Validates: Requirements 3.7
    @Test
    void languageMissingTotalCodeSuggestions_defaultsToZero() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "python",
                                  "total_code_acceptances": 5
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("python"), "Result should contain 'python' language");
        assertEquals(0, result.get("python").totalSuggestions());
        assertEquals(5, result.get("python").totalAcceptances());
    }

    // Test 6: Language node missing "total_code_acceptances" defaults to 0
    // Validates: Requirements 3.7
    @Test
    void languageMissingTotalCodeAcceptances_defaultsToZero() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "typescript",
                                  "total_code_suggestions": 80
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("typescript"), "Result should contain 'typescript' language");
        assertEquals(80, result.get("typescript").totalSuggestions());
        assertEquals(0, result.get("typescript").totalAcceptances());
    }

    // Test 7: Same language appearing in two daily entries has its counts summed
    // Validates: Requirements 3.5
    @Test
    void sameLanguageInTwoDailyEntries_sumsCountsAcrossEntries() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "java",
                                  "total_code_suggestions": 100,
                                  "total_code_acceptances": 30
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  },
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "java",
                                  "total_code_suggestions": 50,
                                  "total_code_acceptances": 20
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("java"), "Result should contain 'java' language");
        assertEquals(150, result.get("java").totalSuggestions());
        assertEquals(50, result.get("java").totalAcceptances());
    }

    // Test 8: Same language appearing in two editors within one daily entry has its counts summed
    // Validates: Requirements 3.5
    @Test
    void sameLanguageInTwoEditors_sumsCountsAcrossEditors() throws IOException {
        String json = """
                [
                  {
                    "copilot_ide_code_completions": {
                      "editors": [
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "java",
                                  "total_code_suggestions": 60,
                                  "total_code_acceptances": 15
                                }
                              ]
                            }
                          ]
                        },
                        {
                          "models": [
                            {
                              "languages": [
                                {
                                  "name": "java",
                                  "total_code_suggestions": 40,
                                  "total_code_acceptances": 10
                                }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                  }
                ]
                """;

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertTrue(result.containsKey("java"), "Result should contain 'java' language");
        assertEquals(100, result.get("java").totalSuggestions());
        assertEquals(25, result.get("java").totalAcceptances());
    }

    // Test 9: Empty JSON array [] returns an empty map
    // Validates: Requirements 3.1, 3.8
    @Test
    void emptyJsonArray_returnsEmptyMap() throws IOException {
        String json = "[]";

        Map<String, MetricSummary> result = MetricsProcessor.process(json);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty map for empty JSON array");
    }
}
