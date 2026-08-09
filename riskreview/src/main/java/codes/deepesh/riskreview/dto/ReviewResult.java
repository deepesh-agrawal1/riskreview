package codes.deepesh.riskreview.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReviewResult {

    private int riskScore;
    private String riskLevel;
    private List<RiskSignal> signals;
    private List<String> recommendations;

}
