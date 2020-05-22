package lab3.zad1;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PublicKey implements IndexedIterable {
    private final List<BigInteger> numbers;
    
    public PublicKey( List<BigInteger> numbers ) {
        this.numbers = numbers;
    }
    
    @Override
    public List<BigInteger> values() {
        return numbers;
    }
    
    
    
    @Override
    public String toString() {
        return new ToStringBuilder( "PublicKey" )
            .add( "numbers", numbers )
            .build();
    }
    
    public List<BigInteger> encrypt(String message) {
        List<Chunk> chunks = split( messageToBits( message ) );
        return chunks.stream()
            .map( this::chunkToSum )
            .collect( Collectors.toList() );
    }
    
    private BigInteger chunkToSum( Chunk chunk ) {
        var values = chunk.values;
        var sum = BigInteger.ZERO;
        //System.out.println( chunk );
        for( int i=0; i<values.length; i++ ) {
            if( values[i] ) {
               // System.out.println( "Using " + i + "(" +numbers.get( i )+ ")" );
                sum = sum.add( numbers.get( i ) );
            }
        }
        //System.out.println( "Sum: " + sum );
        return sum;
    }
    
    private List<Chunk> split( boolean[] bits ) {
        List<Chunk> chunks = new ArrayList<>();
        var length = length();
        for ( int i = 0; i < bits.length; i+= length ) {
            chunks.add( new Chunk( Arrays.copyOfRange(
                bits,
                i,
                Math.min( bits.length, i+length )
            ) ) );
        }
        
        return chunks;
    }
    
    private boolean[] messageToBits( String message ) {
        var bytes = message.getBytes();
        var bits = new boolean[bytes.length*8];
        for ( int i = 0; i < bytes.length; i++ ) {
            var aByte = bytes[ i ];
            //System.out.println( "byte["+i+"]="+Integer.toBinaryString( aByte ) );
            for ( int j = 7; j >= 0; j-- ) {
                bits[ 8*i + j ] = aByte % 2 != 0;
                aByte >>= 1;
                //System.out.println( "bits[" +(8*i + j)+ "]=" + (bits[ 8*i + j ] ? "1" : "0")  );
            }
        }
        //System.out.println("Encrypted message:\n"+bitsToString(  bits ) );
        //System.out.println( "###########messageToBits start############" );
        //System.out.println( message );
//        System.out.println(
//            IntStream.range( 0, bytes.length )
//                .map( i -> bytes[i] )
//                .mapToObj( Integer::toBinaryString )
//                .map( this::padByte )
//                .collect( Collectors.joining("") )
//        );
        //System.out.println( IntStream.range( 0, bits.length ).mapToObj( i -> bits[i] ? "1" : "0" ).collect( Collectors.joining()) );
        //System.out.println( "###########messageToBits end############" );
        return bits;
    }
    
    private String padByte( String s ) {
        return s.length() >= 8
            ? s
            : padByte( "0" + s )
        ;
    }
    
    private record Chunk (boolean[] values){
        @Override
        public String toString() {
            return "Chunk[" + bitsToString( this.values ) + "]";
        }
    }
    
    public static String bitsToString( boolean[] array ) {
        return IntStream.range( 0, array.length )
            .mapToObj( i -> bitToString( array[i] ) )
            .collect( Collectors.joining());
    }
    
    public static String bitsToString( Boolean[] array ) {
        return Arrays.stream( array )
            .map( aBoolean -> bitToString( aBoolean ) )
            .collect( Collectors.joining());
    }
    
    public static String bitsToString( List<Boolean> list ) {
        return list.stream()
            .map( aBoolean -> bitToString( aBoolean ) )
            .collect( Collectors.joining());
    }
    
    public static String bitToString( Boolean aBoolean ) {
        return aBoolean ? "1" : "0";
    }
}
