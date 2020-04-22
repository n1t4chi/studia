package lab2.zad1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.*;

public class EncryptTest {
    public static final KeyStore keyStore = EncryptionEngine.getKeyStore(
        new File(EncryptTest.class.getResource("/keystore.jceks").getFile()),
        "zxcvbnm".toCharArray()
    );
    private static final Key defaultKey = EncryptionEngine.getKey(
        keyStore,
        "krypto",
        "zxcvbnm".toCharArray()
    );
    
    private final EncryptionEngine engine = EncryptionEngine.cbc( defaultKey, new BotchedSecureRandom( new BotchedSecureRandomSpi() ) );
    
    /*
keytool.exe -genseckey -alias krypto -keyalg AES -keysize 128 -storetype jceks -keystore keystore.jceks -storepass zxcvbnm -keypass zxcvbnm
   */
    
    @Test
    void checkSimpleEncyptionAndDecryption() {
        var message = "Hello, World!";
        System.out.println( "Entry message: " + message );
        var cryprogram = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryprogram.getCipherText() ) );
        System.out.println( "IV: " + EncryptionEngine.toNiceString( cryprogram.getIv() ) );
        var decrypted = engine.decryptToString( cryprogram );
        System.out.println( "Decrypted message: " + decrypted );
        Assertions.assertEquals(message, decrypted );
    }
    
    
    @Test
    void checkConsecutiveIv() {
        var message = "Hello, World!";
        
        System.out.println( "Entry message: " + message );
        var cryprogram1 = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryprogram1.getCipherText() ) );
        System.out.println( "IV: " + EncryptionEngine.toNiceString( cryprogram1.getIv() ) );
        var decrypted1 = engine.decryptToString( cryprogram1 );
        System.out.println( "Decrypted message: " + decrypted1 );
        Assertions.assertEquals(message, decrypted1 );
    
        var cryprogram2 = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryprogram2.getCipherText() ) );
        System.out.println( "IV: " + EncryptionEngine.toNiceString( cryprogram2.getIv() ) );
        var decrypted2 = engine.decryptToString( cryprogram2 );
        System.out.println( "Decrypted message: " + decrypted2 );
        Assertions.assertEquals(message, decrypted2 );
    
        var cryprogram3 = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryprogram3.getCipherText() ) );
        System.out.println( "IV: " + EncryptionEngine.toNiceString( cryprogram3.getIv() ) );
        var decrypted3 = engine.decryptToString( cryprogram3 );
        System.out.println( "Decrypted message: " + decrypted3 );
        Assertions.assertEquals(message, decrypted3 );
    }
    
    
    @Test
    void checkCbc() {
        var engine = EncryptionEngine.cbc( defaultKey, new BotchedSecureRandom( new BotchedSecureRandomSpi() ) );
    
        var message = "Hello, World1";
        var cryptogram = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryptogram.getCipherText() ) );
        Assertions.assertFalse( new String( cryptogram.getCipherText() ).contains( message ) );
        var decrypted = engine.decryptToString( cryptogram );
        Assertions.assertEquals( message, decrypted );
    }
    
    
    @Test
    void checkOfb() {
        var engine = EncryptionEngine.ofb( defaultKey, new BotchedSecureRandom( new BotchedSecureRandomSpi() ) );
    
        var message = "Hello, World1";
        var cryptogram = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryptogram.getCipherText() ) );
        Assertions.assertFalse( new String( cryptogram.getCipherText() ).contains( message ) );
        var decrypted = engine.decryptToString( cryptogram );
        Assertions.assertEquals( message, decrypted );
    }
    
    
    @Test
    void checkCtr() {
        var engine = EncryptionEngine.ctr( defaultKey, new BotchedSecureRandom( new BotchedSecureRandomSpi() ) );
    
        var message = "Hello, World1";
        var cryptogram = engine.encrypt( message );
        System.out.println( "Cryptogram: " + EncryptionEngine.toNiceString( cryptogram.getCipherText() ) );
        Assertions.assertFalse( new String( cryptogram.getCipherText() ).contains( message ) );
        var decrypted = engine.decryptToString( cryptogram );
        Assertions.assertEquals( message, decrypted );
    }
    
    @Test
    void algorithms() {
        System.out.println( String.join( "\n" , Security.getAlgorithms( "Cipher" ) ) );
    }
    
}
