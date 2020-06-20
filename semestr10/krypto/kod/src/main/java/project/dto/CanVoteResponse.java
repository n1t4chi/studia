package project.dto;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class CanVoteResponse {
    private final boolean votingAllowed;
    private final List<BigInteger> gToXs;
}
