package codes.deepesh.riskreview.dto;

import lombok.Data;

@Data
public class GitHubPullRequest {
    private int number;
    private String title;
    private String state;
    private int commits;
    private int additions;
    private int deletions;
    private int changed_files;
}
