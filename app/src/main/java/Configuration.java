import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

record Configuration(String githubToken, String orgName) {
    static Configuration load(String path) throws IOException {
        var props = new Properties();
        try (InputStream input = Files.newInputStream(Path.of(path))) {
            props.load(input);
        }

        String token = props.getProperty("GITHUB_TOKEN");
        String org = props.getProperty("ORG_NAME");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing required configuration: GITHUB_TOKEN in " + path);
        }
        if (org == null || org.isBlank()) {
            throw new IllegalStateException("Missing required configuration: ORG_NAME in " + path);
        }
        return new Configuration(token, org);
    }
}
