package lab2.zad1;

import lombok.Data;

@Data
public class Challenge {
    private final Cryptogram cryptogram;
    private final byte[] usedMessage;
    private final int messageIndex;
}
