package lab3.zad1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class KnapsackEncryptionTest {
    private static final int length = 10;
    
    @Test
    public void canGeneratePrivateKey() {
        PrivateKey key = Generator.generate( length );
        System.out.println( key );
        Assertions.assertEquals( 10, key.length() );
    
        BigInteger sum = BigInteger.ZERO;
        for ( var indexed : key.indexedIterable() ) {
            Assertions.assertTrue(
                sum.compareTo( indexed.value() ) < 0,
                "Key is not super increasing sequence. " +
                    "Value: " + indexed.value() + "@"+indexed.index()+" is lesser than sum: " + sum
            );
            sum = sum.add( indexed.value() );
        }
        Assertions.assertTrue(
            sum.compareTo( key.getModulus() ) < 0,
            "Modulus " + key.getModulus() + " is lesser than sum " + sum
        );
        Assertions.assertEquals(
            BigInteger.ONE,
            key.getMultiplier().gcd( key.getModulus() ),
            "Multiplier " + key.getMultiplier() + " is not coprime with modulus " + key.getModulus()
        );
        Assertions.assertEquals(
            key.getMultiplier().modInverse( key.getModulus() ),
            key.getInverseMultiplier(),
            "Inverse multiplier " + key.getInverseMultiplier() + " has different value than actual inverse: " + key.getMultiplier().modInverse( key.getModulus() )
        );
    
        var publicKey = key.getPublicKey();
        
        publicKey.forEach( (index,value) ->
        {
            var expected = key.get( index )
                .multiply( key.getMultiplier() )
                .mod( key.getModulus() )
            ;
            Assertions.assertEquals(
                expected,
                value,
                "Public key " + value + " has invalid value to" +
                    " transformed private key value: " + expected
        );
        } );
    }
    
    @Test
    public void canEncryptAndDecrypt() {
        PrivateKey key = Generator.generate( length );
        var message = "This is neat message. Could you please encrypt it?";
        var cipherText = key.getPublicKey().encrypt( message );
        //System.out.println( "Cipher text:\n" + cipherText );
        Assertions.assertEquals(
            message,
            key.decrypt( cipherText )
        );
    }
}
