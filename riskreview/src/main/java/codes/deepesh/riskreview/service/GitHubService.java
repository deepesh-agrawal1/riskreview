package codes.deepesh.riskreview.service;

import codes.deepesh.riskreview.dto.GitHubChangedFile;
import codes.deepesh.riskreview.dto.GitHubPullRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GitHubService {

    private final WebClient webClient;

    public GitHubService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com")
                .build();
    }

    public GitHubPullRequest getPullRequest(
            String owner,
            String repo,
            int pullNumber
    ) {

        return webClient.get()
                .uri(
                        "/repos/{owner}/{repo}/pulls/{pullNumber}",
                        owner,
                        repo,
                        pullNumber
                )
                .retrieve()
                .bodyToMono(GitHubPullRequest.class)
                .block();
    }

    public List<GitHubChangedFile> getChangedFiles(
            String owner,
            String repository,
            int pullNumber
    ) {

        return webClient.get()
                .uri(
                        "/repos/{owner}/{repo}/pulls/{pullNumber}/files",
                        owner,
                        repository,
                        pullNumber
                )
                .retrieve()
                .bodyToFlux(GitHubChangedFile.class)
                .collectList()
                .block();
    }
}