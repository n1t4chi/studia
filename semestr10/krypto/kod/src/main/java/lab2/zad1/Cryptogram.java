package lab2.zad1;

import lombok.Data;

@Data
public class Cryptogram {
    private final byte[] cipherText;
    private final byte[] iv;
    private final int length;
}
