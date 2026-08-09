package codes.deepesh.riskreview.dto;

import lombok.Data;

@Data
public class GitHubChangedFile {

    private String filename;
    private String status;
    private int additions;
    private int deletions;
    private int changes;
    private String patch;
}
