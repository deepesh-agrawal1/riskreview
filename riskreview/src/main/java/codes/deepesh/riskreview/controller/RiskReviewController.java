package codes.deepesh.riskreview.controller;

import codes.deepesh.riskreview.dto.GitHubChangedFile;
import codes.deepesh.riskreview.dto.ReviewRequest;
import codes.deepesh.riskreview.dto.ReviewResult;
import codes.deepesh.riskreview.service.GitHubService;
import codes.deepesh.riskreview.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class RiskReviewController {

    private final GitHubService gitHubService;
    private final ReviewService reviewService;

    @PostMapping("/analyze")
    public ReviewResult analyze(
            @RequestBody ReviewRequest request
    ) {

        String[] parts = request.getPrURL().split("/");

        String owner = parts[3];
        String repository = parts[4];
        int pullNumber = Integer.parseInt(parts[6]);

        List<GitHubChangedFile> files =
                gitHubService.getChangedFiles(
                        owner,
                        repository,
                        pullNumber
                );

        return reviewService.analyze(files);
    }
}