package lab2.zad1;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.security.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EncryptionEngine {
    public static final String aes_cbc = "AES_128/CBC/NOPADDING";
    public static final String aes_ofb = "AES_128/OFB/NOPADDING";
    public static final String aes_ctr = "AES_128/GCM/NOPADDING";
    
    @SneakyThrows
    public static KeyStore getKeyStore( File keyStore, char[] storePassword ) {
        return KeyStore.getInstance( keyStore, storePassword );
    }
    
    @SneakyThrows
    public static Key getKey( KeyStore keyStore, String keyAlias, char[] keyPassword ) {
        return keyStore.getKey( keyAlias, keyPassword );
    }
    
    public static String toNiceString( byte[] bytes ) {
        return "String[" + new String( bytes ) + "], " +
            "Array[" + bytesToString( bytes ) + "]";
    }
    
    public static String bytesToString( byte[] bytes ) {
        return IntStream
            .range( 0, bytes.length )
            .mapToObj( i -> bytes[ i ] )
            .map( String::valueOf )
            .collect( Collectors.joining( "," ) );
    }
    
    public static EncryptionEngine get(
        EncryptionMode encryptionMode,
        Key key,
        SecureRandom secureRandom
    )
    {
        return switch ( encryptionMode ) {
            case aes_cbc -> cbc( key, secureRandom );
            case aes_ofb -> ofb( key, secureRandom );
            case aes_ctr -> ctr( key, secureRandom );
        };
    }
    
    public static EncryptionEngine cbc( Key key, SecureRandom secureRandom )
    {
        return new EncryptionEngine( key, aes_cbc, secureRandom, false );
    }
    
    public static EncryptionEngine ofb( Key key, SecureRandom secureRandom )
    {
        return new EncryptionEngine( key, aes_ofb, secureRandom, false );
    }
    
    public static EncryptionEngine ctr( Key key, SecureRandom secureRandom )
    {
        return new EncryptionEngine( key, aes_ctr, secureRandom, true );
    }
    
    private final Key key;
    private final String algorithm;
    private final SecureRandom secureRandom;
    private final boolean usesGcm;
    
    private EncryptionEngine( Key key, String algorithm, SecureRandom secureRandom, boolean usesGcm ) {
        this.key = key;
        this.algorithm = algorithm;
        this.secureRandom = secureRandom;
        this.usesGcm = usesGcm;
    }
    
    public Challenge challenge( String message1, String message2 ) {
        var messageIndex = ThreadLocalRandom.current().nextInt( 2 );
        var message = List.of( message1, message2 ).get( messageIndex );
        return new Challenge( encrypt( message ), message, messageIndex+1 );
    }
    
    public List< Cryptogram > encrypt( List< String > messages ) {
        return messages.stream()
            .map( String::getBytes )
            .map( this::encrypt )
            .collect( Collectors.toList() )
        ;
    }
    
    public Cryptogram encrypt( String message ) {
        return encrypt( message.getBytes() );
    }
    
    @SneakyThrows
    public Cryptogram encrypt( byte[] input ) {
        var encryption = getCipher();
        encryption.init( Cipher.ENCRYPT_MODE, key, getSecureRandom() );
        var bytes = encryption.doFinal( padIfNeeded( input, encryption.getBlockSize() ) );
        var iv = encryption.getIV();
        return new Cryptogram( bytes, iv, input.length );
    }
    
    public String decryptToString( Cryptogram input ) {
        return new String( decrypt( input ) );
    }
    
    @SneakyThrows
    public byte[] decrypt( Cryptogram input ) {
        var decryption = getCipher();
        if( usesGcm )
            decryption.init( Cipher.DECRYPT_MODE, key, new GCMParameterSpec( 128, input.getIv() ) );
        else
        decryption.init( Cipher.DECRYPT_MODE, key, new IvParameterSpec( input.getIv() ) );
        var decrypted = decryption.doFinal( input.getCipherText() );
        return unpad( input, decrypted );
    }
    
    private byte[] unpad( Cryptogram input, byte[] decrypted ) {
        return Arrays.copyOf( decrypted, input.getLength() );
    }
    
    private byte[] padIfNeeded( byte[] input, int blockSize ) {
        int residue = input.length % blockSize;
        if ( residue == 0 ) {
            return input;
        }
        return Arrays.copyOf( input, input.length + ( 16 - residue ) );
    }
    
    @SneakyThrows
    private Cipher getCipher() {
        return Cipher.getInstance( algorithm );
    }
    
    private SecureRandom getSecureRandom() {
        return secureRandom;
    }
}
