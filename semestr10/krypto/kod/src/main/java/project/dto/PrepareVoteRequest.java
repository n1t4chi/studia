package project.dto;

import lombok.*;

import java.math.BigInteger;

@Data
public class PrepareVoteRequest {
    @NonNull
    private final String id;
    @NonNull
    private final BigInteger gToX;
}
