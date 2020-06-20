package project.server;

import lombok.Data;
import project.VoteResult;

import java.math.BigInteger;
import java.util.List;

@Data
public class VotingResults {
    private final List<BigInteger> gToXs;
    private final List<BigInteger> votes;
    private final VoteResult majority;
    private final int yesCount;
}
