package codes.deepesh.riskreview.service;

import codes.deepesh.riskreview.dto.GitHubChangedFile;
import codes.deepesh.riskreview.dto.RiskSignal;
import codes.deepesh.riskreview.dto.ReviewResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    public ReviewResult analyze(List<GitHubChangedFile> files) {

        int score = 0;

        int productionFiles = 0;
        int testFiles = 0;

        List<RiskSignal> signals = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        for (GitHubChangedFile file : files) {

            String filename = file.getFilename().toLowerCase();

            /*
             * Count production and test files
             */
            if (isTestFile(filename)) {

                testFiles++;

            } else {

                productionFiles++;
            }

            /*
             * Analyze added code from patch
             */
            String patch = file.getPatch();

            if (patch != null) {

                String addedCode = getAddedCode(patch);

                // Hardcoded secrets
                if (containsSecretPattern(addedCode)) {

                    int signalScore = 30;

                    score += signalScore;

                    signals.add(new RiskSignal(
                            "HARDCODED_SECRET",
                            "HIGH",
                            file.getFilename(),
                            "Possible hardcoded secret detected",
                            signalScore
                    ));

                    recommendations.add(
                            "Review the change for exposed API keys, passwords, or tokens"
                    );
                }

                // SQL changes
                if (containsSqlPattern(addedCode)) {

                    int signalScore = 20;

                    score += signalScore;

                    signals.add(new RiskSignal(
                            "SQL_CHANGE",
                            "HIGH",
                            file.getFilename(),
                            "SQL or database operation detected",
                            signalScore
                    ));

                    recommendations.add(
                            "Review SQL changes for injection and data integrity risks"
                    );
                }

                // Authentication/security changes
                if (containsSecurityPattern(addedCode)) {

                    int signalScore = 20;

                    score += signalScore;

                    signals.add(new RiskSignal(
                            "SECURITY_CODE",
                            "HIGH",
                            file.getFilename(),
                            "Security-sensitive code detected",
                            signalScore
                    ));

                    recommendations.add(
                            "Review authentication and authorization logic carefully"
                    );
                }
            }

            /*
             * Security-related filename
             */
            if (filename.contains("security")
                    || filename.contains("auth")) {

                int signalScore = 30;

                score += signalScore;

                signals.add(new RiskSignal(
                        "SECURITY_FILE",
                        "HIGH",
                        file.getFilename(),
                        "Security-related file changed",
                        signalScore
                ));
            }

            /*
             * Database-related file
             */
            if (filename.contains("migration")
                    || filename.endsWith(".sql")) {

                int signalScore = 25;

                score += signalScore;

                signals.add(new RiskSignal(
                        "DATABASE_CHANGE",
                        "HIGH",
                        file.getFilename(),
                        "Database-related file changed",
                        signalScore
                ));
            }

            /*
             * Configuration file
             */
            if (filename.endsWith(".yml")
                    || filename.endsWith(".yaml")
                    || filename.endsWith(".properties")) {

                int signalScore = 15;

                score += signalScore;

                signals.add(new RiskSignal(
                        "CONFIGURATION_CHANGE",
                        "MEDIUM",
                        file.getFilename(),
                        "Configuration file changed",
                        signalScore
                ));
            }

            /*
             * Large change
             */
            if (file.getChanges() > 100) {

                int signalScore = 15;

                score += signalScore;

                signals.add(new RiskSignal(
                        "LARGE_CHANGE",
                        "MEDIUM",
                        file.getFilename(),
                        "More than 100 lines changed",
                        signalScore
                ));
            }

            /*
             * Deleted file
             */
            if ("deleted".equalsIgnoreCase(file.getStatus())) {

                int signalScore = 20;

                score += signalScore;

                signals.add(new RiskSignal(
                        "FILE_DELETED",
                        "HIGH",
                        file.getFilename(),
                        "File was deleted",
                        signalScore
                ));
            }
        }

        /*
         * PR-level test coverage signal
         */
        if (productionFiles > 0 && testFiles == 0) {

            int signalScore = 10;

            score += signalScore;

            signals.add(new RiskSignal(
                    "NO_TEST_CHANGES",
                    "MEDIUM",
                    null,
                    "Production code changed without corresponding test changes",
                    signalScore
            ));

            recommendations.add(
                    "Consider adding or updating tests for the changed production code"
            );
        }

        /*
         * Remove duplicate recommendations
         */
        recommendations = recommendations.stream()
                .distinct()
                .toList();

        /*
         * Build result
         */
        ReviewResult result = new ReviewResult();

        result.setRiskScore(normalizeScore(score));
        result.setRiskLevel(getRiskLevel(normalizeScore(score)));
        result.setSignals(signals);
        result.setRecommendations(recommendations);

        return result;
    }

    /*
     * Detect test files
     */
    private boolean isTestFile(String filename) {

        return filename.contains("/test/")
                || filename.contains("\\test\\")
                || filename.endsWith("test.java")
                || filename.endsWith("tests.java")
                || filename.contains("test_")
                || filename.contains("_test.");
    }

    /*
     * Detect possible hardcoded secrets
     */
    private boolean containsSecretPattern(String patch) {

        return patch.contains("api_key")
                || patch.contains("apikey")
                || patch.contains("api-key")
                || patch.contains("password =")
                || patch.contains("password:")
                || patch.contains("secret =")
                || patch.contains("secret:")
                || patch.contains("access_token")
                || patch.contains("access-token");
    }

    /*
     * Detect SQL operations
     */
    private boolean containsSqlPattern(String patch) {

        return patch.contains("select ")
                || patch.contains("insert into")
                || patch.contains("update ")
                || patch.contains("delete from")
                || patch.contains("drop table")
                || patch.contains("alter table");
    }

    /*
     * Detect security-related code
     */
    private boolean containsSecurityPattern(String patch) {

        return patch.contains("@preauthorize")
                || patch.contains("@secured")
                || patch.contains("securityfilterchain")
                || patch.contains("passwordencoder")
                || patch.contains("authentication")
                || patch.contains("authorization");
    }

    /*
     * Extract only added lines from the GitHub patch
     */
    private String getAddedCode(String patch) {

        StringBuilder addedCode = new StringBuilder();

        for (String line : patch.split("\n")) {

            if (line.startsWith("+")
                    && !line.startsWith("+++")) {

                addedCode.append(line).append("\n");
            }
        }

        return addedCode.toString().toLowerCase();
    }

    /*
     * Keep risk score between 0 and 100
     */
    private int normalizeScore(int score) {

        return Math.min(score, 100);
    }

    /*
     * Determine risk level
     */
    private String getRiskLevel(int score) {

        if (score >= 70) {
            return "HIGH";
        }

        if (score >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }
}