package codes.deepesh.riskreview.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class GitHubUrlValidator {

    public boolean isValid(String prUrl) {

        if (prUrl == null || prUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(prUrl);

            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }

            if (!"github.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] parts = uri.getPath().split("/");

            return parts.length == 5
                    && !parts[1].isBlank()
                    && !parts[2].isBlank()
                    && "pull".equalsIgnoreCase(parts[3])
                    && isValidPullNumber(parts[4]);

        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isValidPullNumber(String value) {

        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}