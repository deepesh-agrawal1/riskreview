package codes.deepesh.riskreview.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskSignal {
    private String type;
    private String severity;
    private String file;
    private String reason;
    private int score;
}
