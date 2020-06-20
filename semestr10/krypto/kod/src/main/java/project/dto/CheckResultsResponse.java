package project.dto;

import lombok.Data;
import project.server.VotingResults;

@Data
public class CheckResultsResponse {
    private final VotingResults results;
}
