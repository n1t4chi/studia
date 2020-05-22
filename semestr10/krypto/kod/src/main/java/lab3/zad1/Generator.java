package lab3.zad1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Generator {
    public static PrivateKey generate( int length ) {
        if ( length < 1 ) {
            throw new IllegalArgumentException( "Key length lesser than 1" );
        }
    
        //proposition from book:
        // n = length
        // values(i) = random from [ (i-1)*2^n + 1, i*2^n ]
        //           = (i-1)*2^n + rand from [ 1, 2^n ]
        // modulus = random from [ 2^(2n+1) + 1, 2^(2n+2) - 1 ]
        //         = 2^(2n+1) + rand from [ 1 , 2^(n+1) - 1 ]
        // multiplier = w / gcd ( w, modulus )
        // w = random from [ 2, modulus - 2 ]
        
        //It seems that superincreasing values are impossible to choose that way
        //for example: 4th value is from [ 3*2^n + 1, 4*2^n ] while
        //previous elements can sum up to (1+2+3)*2^n=6*2^n which is outside the 4th value range.
        
        //instead we will use:
        //n=length
        //values(i) = 2^(i+n)-1 + random from [1,2^(i+n))
        
        //also multiplier = w/gcd(w,m) can still have common factorization with modulus
        //see: w=9, m=12, this yelds multiplier=3 which has GCD(3,12)=3.
        //so we are running w=w/gcd(w,modulus) until gcd(w,modulus)=1 and then multiplier=w
        
        var values = new ArrayList<BigInteger>( length );
        var base = BigInteger.TWO.pow( length );
        var sum = BigInteger.ZERO;
        for ( int i = 0; i < length; i++ ) {
            //use exclusive bound rather than inclusive
            var baseMultiplier = BigInteger.TWO.pow( i ).subtract( BigInteger.ONE );
            var currentBase = base.multiply( baseMultiplier );
            var random = randomBigInt( length );
            var next = currentBase.add( random );
//            System.out.println( "["+i+"] "+baseMultiplier+"*"+base+"+"+random+"="+next+"(sum: "+sum+")" );
            sum = sum.add( next );
            values.add( next );
        }
        
        var modulus = BigInteger.TWO.pow( length * 2 + 1 )
            .add( randomBigInt( 2*length+1 ) );
    
        BigInteger multiplier = getMultiplier( modulus );
        return new PrivateKey(
            values,
            modulus,
            multiplier
        );
    }
    
    private static BigInteger getMultiplier( BigInteger modulus ) {
        var w = getBigIntegerLesserThan( modulus.subtract( BigInteger.valueOf( 4 ) ) )
            .add( BigInteger.TWO );
        BigInteger gcd;
        while( !(gcd =w.gcd( modulus )).equals( BigInteger.ONE ) )
        {
            w = w.divide( gcd );
        }
        return w;
    }
    
    private static BigInteger getBigIntegerLesserThan( BigInteger modulus ) {
        var value = randomBigInt( modulus.bitLength() );
        return value.compareTo( modulus ) >= 0
            ? getBigIntegerLesserThan( modulus )
            : value
        ;
    }
    
    private static BigInteger randomBigInt( int bitSize ) {
        var randomOnesAndZeroes = ThreadLocalRandom.current()
            .ints( bitSize, 0, 2 )
            .mapToObj( String::valueOf )
            .collect( Collectors.joining() );
        if( randomOnesAndZeroes.matches( "0+" ) )
        {
            //let's not return 0
            return randomBigInt( bitSize );
        }
        return new BigInteger( randomOnesAndZeroes, 2 );
    }
}
