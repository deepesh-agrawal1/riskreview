package codes.deepesh.riskreview.controller;


import codes.deepesh.riskreview.dto.GitHubPullRequest;
import codes.deepesh.riskreview.dto.ReviewRequest;
import codes.deepesh.riskreview.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class RiskReviewController {

    private final GitHubService gitHubService;

    @PostMapping("/analyze")
    public GitHubPullRequest analyze(@RequestBody ReviewRequest request) {

        String[] parts = request.getPrURL()
                .split("/");

        String owner = parts[3];
        String repo = parts[4];
        int pullNumber = Integer.parseInt(parts[6]);

        return gitHubService.getPullRequest(
                owner,
                repo,
                pullNumber
        );
    }
}
