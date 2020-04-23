package lab2.zad2;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Guesser {
    
    @SneakyThrows
    public static void main( String[] args ) {
        var reader = new BufferedReader( new InputStreamReader( System.in ) );
        System.out.println( "Start oracle mode first." );
        System.out.println( "Ask for any message (for example: test), then paste bytes for IV used" );
        var iv = toBytes( reader.readLine() );
        
        incrementIv( iv );
        System.out.println( "Ask for message (IV+1, this will result with ciphertext for AES(key,0)):\n" + toString( iv ) );
        System.out.println( "Put the cryptogram that was returned." );
        var zeroedMessageCryprogram = toBytes( reader.readLine() );
        System.out.println( "Now start the challenge." );
        incrementIv( iv );
        System.out.println( "message1 (IV+2, if selected, should result in AES(key,0) ):\n" + toString( iv ) );
        System.out.println( "message2 (random text):\nThanks Obama!" );
        System.out.println( "Put the cryptogram that was returned." );
        var returnedCryprogram = toBytes( reader.readLine() );
        if( Arrays.equals( zeroedMessageCryprogram, returnedCryprogram ) ) {
            System.out.println( "Returned cryptogram is same as cryptogram for IV+1." );
            System.out.println( "Select 1" );
        } else {
            System.out.println( "Returned cryptogram is different than cryprogram for IV+1." );
            System.out.println( "Select 2" );
        }
        
    }
    
    private static byte[] toBytes( String ivString ) {
        int[] ints = Arrays.stream(
            ivString.substring( 1, ivString.length()-1 )
                .split( "\s*,\s*" )
        )
            .mapToInt( Integer::parseInt )
            .toArray()
        ;
        byte[] bytes = new byte[ints.length];
        for ( int i = 0; i < bytes.length; i++ ) {
            bytes[i] = ( byte ) ints[i];
        }
        return bytes;
    }
    
    protected static void incrementIv( byte[] iv ) {
        for ( int i = iv.length - 1; i >= 0; i-- ) {
            iv[i]++;
            if( iv[i] > Byte.MIN_VALUE )
            {
                break;
            }
        }
    }
    
    private static String toString( byte[] bytes )
    {
        return IntStream.range( 0, bytes.length ).map( i -> bytes[i] )
            .mapToObj( String::valueOf )
            .collect( Collectors.joining(",", "[", "]" ) );
    }
}
