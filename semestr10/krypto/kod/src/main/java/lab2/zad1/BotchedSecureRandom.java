package lab2.zad1;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;

class BotchedSecureRandom extends SecureRandom
{
    public BotchedSecureRandom( SecureRandomSpi secureRandomSpi ) {
        super( secureRandomSpi, null );
    }
}
