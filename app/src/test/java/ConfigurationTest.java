import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @TempDir
    Path tempDir;

    // Helper: write a .properties file with the given content and return its path string
    private String writePropertiesFile(String content) throws IOException {
        Path file = tempDir.resolve("config.properties");
        Files.writeString(file, content);
        return file.toString();
    }

    // Test 1: Valid properties file with both fields loads correctly
    @Test
    void validPropertiesFile_loadsBothFields() throws IOException {
        String path = writePropertiesFile("GITHUB_TOKEN=mytoken\nORG_NAME=myorg\n");
        Configuration config = Configuration.load(path);
        assertEquals("mytoken", config.githubToken());
        assertEquals("myorg", config.orgName());
    }

    // Test 2: Missing GITHUB_TOKEN throws IllegalStateException identifying the property and file path
    @Test
    void missingGithubToken_throwsIllegalStateException() throws IOException {
        String path = writePropertiesFile("ORG_NAME=myorg\n");
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> Configuration.load(path)
        );
        assertTrue(ex.getMessage().contains("GITHUB_TOKEN"),
                "Message should identify GITHUB_TOKEN but was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains(path),
                "Message should contain the file path but was: " + ex.getMessage());
    }

    // Test 3: Blank GITHUB_TOKEN (whitespace-only) throws IllegalStateException
    @Test
    void blankGithubToken_throwsIllegalStateException() throws IOException {
        String path = writePropertiesFile("GITHUB_TOKEN=   \nORG_NAME=myorg\n");
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> Configuration.load(path)
        );
        assertTrue(ex.getMessage().contains("GITHUB_TOKEN"),
                "Message should identify GITHUB_TOKEN but was: " + ex.getMessage());
    }

    // Test 4: Missing ORG_NAME throws IllegalStateException identifying the property and file path
    @Test
    void missingOrgName_throwsIllegalStateException() throws IOException {
        String path = writePropertiesFile("GITHUB_TOKEN=mytoken\n");
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> Configuration.load(path)
        );
        assertTrue(ex.getMessage().contains("ORG_NAME"),
                "Message should identify ORG_NAME but was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains(path),
                "Message should contain the file path but was: " + ex.getMessage());
    }

    // Test 5: Blank ORG_NAME throws IllegalStateException
    @Test
    void blankOrgName_throwsIllegalStateException() throws IOException {
        String path = writePropertiesFile("GITHUB_TOKEN=mytoken\nORG_NAME=   \n");
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> Configuration.load(path)
        );
        assertTrue(ex.getMessage().contains("ORG_NAME"),
                "Message should identify ORG_NAME but was: " + ex.getMessage());
    }

    // Test 6: Non-existent file path throws IOException
    @Test
    void nonExistentFile_throwsIOException() {
        String path = tempDir.resolve("does-not-exist.properties").toString();
        assertThrows(
                IOException.class,
                () -> Configuration.load(path)
        );
    }
}
