package lab1.zad1;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author n1t4chi
 */
public class LcgGuesser {
    LcgParams guessIncrement( LCG lcg, int multiplier, int modulus ) {
        var s0 = lcg.next();
        var s1 = lcg.next();
        
        //s1 = ( multiplier * s0 + c ) (mod modulus)
        //s1 - c = multiplier * s0 (mod modulus)
        //c = s1 - multiplier * s0 (mod modulus)
        
        var multipliedS0 = mod( multiplier * s0, modulus );
        int increment = mod( s1 - multipliedS0, modulus );
        return new LcgParams( multiplier, increment, modulus );
    }
    
    LcgParams guessMultiplierAndIncrement( LCG lcg, int modulus ) {
        var s0 = lcg.next();
        var s1 = lcg.next();
        var s2 = lcg.next();
        
        //s1 = s0*m + c (mod modulus)
        //s2 = s1*m + c (mod modulus)
        //
        //s2-s1 = s1 * m - s0 * m (mod modulus)
        //s2-s1 = m * (s1 - s0) (mod modulus)
        //m = (s2-s1)/(s1-s0) (mod modulus)
        
        var numerator = mod(s2-s1, modulus);
        var denominator = mod( s1 - s0, modulus );
        long invertedDenominator;
        try {
            invertedDenominator =
                BigInteger.valueOf( denominator ).modInverse( BigInteger.valueOf( modulus ) ).longValue();
        } catch ( Exception ex )
        {
            System.out.println( "Unsolveable inverse denominator: " + denominator + "^-1 mod " + modulus );
            throw ex;
        }
        var multiplier = mod(numerator * invertedDenominator, modulus );
        
        return guessIncrement( lcg, multiplier, modulus );
    }
    
    private LcgParams guessAll( LCG lcg ) {
        //s1 = s0*m + c  (mod n)
        //s2 = s1*m + c  (mod n)
        //s3 = s2*m + c  (mod n)
        
        //s1 - (s0*m + c) = k1 * n
        //s2 - (s1*m + c) = k2 * n
        //s3 - (s2*m + c) = k3 * n
        
        //X = 0 (mod n) = k*n
        
        //differences:
        //t0 = s1-s0
        //t1 = s2-s1 = (s1*m+c) - (s0*m+c) = m*(s1-s0) = m*t0 (mod n)
        //t2 = s3-s2 = (s2*m+c) - (s1*m+c) = m*(s2-s1) = m*t1 (mod n)
        //t3 = s4-s3 = (s3*m+c) - (s2*m+c) = m*(s3-s2) = m*t2 (mod n)
        
        //zeroes
        //t2*t0 - t1*t1 = (m*t1*t0) - (m*t0*m*t0) = (m*m*t0*t0) - (m*m*t0*t0) = 0 (mod n)
    
        var values = LongStream.generate( lcg::next )
            .limit( 7 )
            .boxed()
            .collect( Collectors.toList() )
        ;
//        forEachIterate( values, (i,s) -> System.out.println( "s"+i+"="+s ) );
        
        var differences = IntStream.range( 1, values.size() )
            .mapToObj( i -> values.get( i ) - values.get( i-1 ) )
            .collect( Collectors.toList() );
        
        System.out.println();
//        forEachIterate( differences, (i,t) -> System.out.println( "t"+i+"="+t ) );
        
    
        var zeroes = IntStream.range( 2, differences.size() )
            .mapToObj( i -> getZero( differences, i ) )
            .collect( Collectors.toList() );
        
        System.out.println();
//        forEachIterate( zeroes, (i,z) -> System.out.println( "z"+i+"="+z ) );
        
        var reduce = zeroes.stream().reduce( this::gcd );
//        System.out.println( "reduce:" + reduce );
    
        int modulus = Math.abs( reduce.orElseThrow( () -> new RuntimeException( "no result" ) ).intValue() );
//        System.out.println( "modulus:" + modulus );
        return guessMultiplierAndIncrement( lcg, modulus );
    }
    
    private long getZero( List<Long> values, int i ) {
        var t0 = values.get( i - 2 );
        var t1 = values.get( i - 1 );
        var t2 = values.get( i );
        var st1 = sqr( t1 );
    
//        System.out.println( i + " -> t"+i +"["+t2+"]*" + "t"+(i-2) + "["+t0+"]-t"+(i-1)+"["+t1+"]^2{"+st1+"}" );
    
        return t2 * t0 - st1;
    }
    
    private <T> void forEachIterate( List<T> collection, BiConsumer<Integer,T> consumer )
    {
        for ( int i = 0; i < collection.size(); i++ ) {
            consumer.accept( i, collection.get( i ) );
        }
    }
    
    private long sqr( long i ) {
        return i*i;
    }
    
    @Test
    void unknownIncrement() {
        var seed = 54321;
        
        var unknownIncrement = 12345;
        var knownMultiplier = 123;
        var knownModulus = 1234567;
    
        var lcgParams = new LcgParams( knownMultiplier, unknownIncrement, knownModulus );
        var lcg = new LCG( seed, lcgParams );
        Assertions.assertEquals( lcgParams, guessIncrement( lcg, knownMultiplier, knownModulus ) );
    }
    
    @Test
    void unknownIncrement_onRandomValues() {
        var random = ThreadLocalRandom.current();
        var knownModulus = random.nextInt( 1, 46340 );
        var unknownIncrement = random.nextInt( knownModulus );
        var knownMultiplier = random.nextInt( knownModulus );
        var seed = random.nextInt( knownModulus );
    
        var lcgParams = new LcgParams( knownMultiplier, unknownIncrement, knownModulus );
        var lcg = new LCG( seed, lcgParams );
        
//        System.out.println( "params: " + lcgParams );
        Assertions.assertEquals( lcgParams, guessIncrement( lcg, knownMultiplier, knownModulus ) );
    }
    
    @Test
    void unknownMultiplierAndIncrement() {
        var seed = 54321;
        
        var unknownIncrement = 12345;
        var unknownMultiplier = 123;
        var knownModulus = 1234567;
        
        var lcgParams = new LcgParams( unknownMultiplier, unknownIncrement, knownModulus );
        var lcg = new LCG( seed, lcgParams );
        
        Assertions.assertEquals( lcgParams, guessMultiplierAndIncrement( lcg, knownModulus ) );
    }
    
    @Test
    void unknownMultiplierAndIncrement_onRandomValues() {
        var random = ThreadLocalRandom.current();
        var knownModulus = random.nextInt( 1, 46340 );
        var unknownIncrement = random.nextInt( knownModulus );
        var knownMultiplier = random.nextInt( knownModulus );
        var seed = random.nextInt( knownModulus );
    
        var lcgParams = new LcgParams( knownMultiplier, unknownIncrement, knownModulus );
        var lcg = new LCG( seed, lcgParams );
    
//        System.out.println( "params: " + lcgParams );
        Assertions.assertEquals( lcgParams, guessMultiplierAndIncrement( lcg, knownModulus ) );
    }
    
    private int mod( long value, int modulus ) {
        var c = value % modulus;
        return (int)( c < 0 ? ( modulus + c ) : c );
    }
    
    private long gcd( long a, long b )
    {
        var gcd2 = gcd2( a, b );
//        System.out.println( "gcd("+a+","+b+")="+gcd2 );
        return gcd2;
    }
    
    private long gcd2( long a, long b )
    {
        return b == 0 ? a : gcd2( b, a % b );
    }
    
    @Test
    void allUnknown() {
        var seed = 54321;
        
        var unknownIncrement = 12345;
        var unknownMultiplier = 123;
        var unknownModulus = 1234567;
        
        var lcgParams = new LcgParams( unknownMultiplier, unknownIncrement, unknownModulus );
        var lcg = new LCG( seed, lcgParams );
        
        Assertions.assertEquals( lcgParams, guessAll( lcg ) );
    }
    
    @Test
    void allUnknown_onRandomValues() {
        try {
            var random = ThreadLocalRandom.current();
            var unknownModulus = random.nextInt( 1, 46340 );
            var unknownIncrement = random.nextInt( unknownModulus );
            var unknownMultiplier = random.nextInt( unknownModulus );
            var seed = random.nextInt( unknownModulus );
    
            var lcgParams = new LcgParams( unknownMultiplier, unknownIncrement, unknownModulus );
            var lcg = new LCG( seed, lcgParams );
    
            Assertions.assertEquals( lcgParams, guessAll( lcg ) );
        }
        catch ( ArithmeticException ex )
        {
            Assertions.assertEquals( "BigInteger not invertible.",ex.getMessage() );
            System.out.println( "Unsolveable case" );
        }
    }
}
