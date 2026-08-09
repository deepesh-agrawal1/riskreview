package codes.deepesh.riskreview.controller;

import codes.deepesh.riskreview.dto.GitHubChangedFile;
import codes.deepesh.riskreview.dto.ReviewRequest;
import codes.deepesh.riskreview.dto.ReviewResult;
import codes.deepesh.riskreview.service.GitHubService;
import codes.deepesh.riskreview.service.GitHubUrlValidator;
import codes.deepesh.riskreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class RiskReviewController {

    private final GitHubService gitHubService;
    private final ReviewService reviewService;
    private final GitHubUrlValidator gitHubUrlValidator;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestBody ReviewRequest request
    ) {

        if (!gitHubUrlValidator.isValid(request.getPrUrl())) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Invalid GitHub PR URL"
                    ));
        }

        String[] parts = request.getPrUrl().split("/");

        String owner = parts[3];
        String repository = parts[4];
        int pullNumber = Integer.parseInt(parts[6]);

        List<GitHubChangedFile> files =
                gitHubService.getChangedFiles(
                        owner,
                        repository,
                        pullNumber
                );

        ReviewResult result =
                reviewService.analyze(files);

        return ResponseEntity.ok(result);
    }
}