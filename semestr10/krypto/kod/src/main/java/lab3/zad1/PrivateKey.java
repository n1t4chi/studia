package lab3.zad1;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static lab3.zad1.PublicKey.bitToString;
import static lab3.zad1.PublicKey.bitsToString;

public class PrivateKey implements IndexedIterable {
    private static List<BigInteger> makePublicValues(
        List<BigInteger> numbers,
        BigInteger modulus,
        BigInteger multiplier
    ) {
        return numbers.stream()
            .map( multiplier::multiply )
            .map( value -> value.mod( modulus ) )
            .collect( Collectors.toList() );
    }
    
    private final List<BigInteger> numbers;
    private final BigInteger modulus;
    private final BigInteger multiplier;
    private final BigInteger inverseMultiplier;
    private final PublicKey publicKey;
    
    public PrivateKey( List<BigInteger> numbers, BigInteger modulus, BigInteger multiplier ) {
        this.numbers = numbers;
        this.modulus = modulus;
        this.multiplier = multiplier;
        
        publicKey = new PublicKey( makePublicValues( numbers, modulus, multiplier ) );
        inverseMultiplier = multiplier.modInverse( modulus );
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public BigInteger getModulus() {
        return modulus;
    }
    
    public BigInteger getMultiplier() {
        return multiplier;
    }
    
    public BigInteger getInverseMultiplier() {
        return inverseMultiplier;
    }
    
    @Override
    public List<BigInteger> values() {
        return numbers;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder( "PublicKey" )
            .add( "numbers", numbers )
            .add( "publicKey", publicKey )
            .add( "modulus", modulus )
            .add( "multiplier", multiplier )
            .add( "inverseMultiplier", inverseMultiplier )
            .build();
    }
    
    public List<BigInteger> encrypt(String message) {
        return publicKey.encrypt( message );
        
    }
    
    public String decrypt(List<BigInteger> message) {
        var bits = new ArrayList<Boolean>();
        for ( BigInteger value : message ) {
            BigInteger coverted = value.multiply( inverseMultiplier ).mod( modulus );
            Boolean[] partialBits = findBits( coverted );
            //System.out.println( value + " (" + coverted + ") -> " + bitsToString( partialBits ) );
            for ( Boolean aBoolean : partialBits ) {
                bits.add( aBoolean );
            }
        }
        //System.out.println("Decrypted message:\n"+bitsToString(  bits ) );
        var bytes = bitsToBytes( bits );
        return new String( bytes );
    }
    
    private byte[] bitsToBytes( List<Boolean> bits ) {
        byte[] bytes = new byte[ (int)Math.ceil( bits.size() / 8.0 ) ];
        for ( int byteIndex = 0; byteIndex < bytes.length; byteIndex++ ) {
            for ( int j = 0; j < 8; j++ ) {
                var bitIndex = (byteIndex+1) * 8 - j - 1;
                //System.out.println( "bit["+bitIndex+"]=" +bitToString( bits.get( bitIndex ) ) + " -> value = " + (1<<j) );
                if( bits.get( bitIndex ) )
                {
                    bytes[byteIndex] |= 1<<j;
                }
            }
            //System.out.println( "bytes["+byteIndex+"]=" +Integer.toBinaryString( bytes[byteIndex] ) );
        }
        return bytes;
    }
    
    private Boolean[] findBits( BigInteger cipherText ) {
        Boolean[] bits = new Boolean[ numbers.size() ];
        for ( int i = numbers.size() - 1; i >= 0; i-- ) {
            var value = numbers.get( i );
            bits[i]=cipherText.compareTo( value ) >= 0;
            if( bits[i] )
            {
                cipherText = cipherText.subtract( value );
            }
        }
        return bits;
    }
}
