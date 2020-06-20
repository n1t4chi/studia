package project;

import lombok.Data;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static project.Util.randomBigInt;

@Data
public class Group {
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger r;
    private final BigInteger h;
    private final BigInteger g;
    
    public static Group create( int bitLength ) {
        BigInteger q;
        BigInteger r;
        BigInteger p;
        do {
            q = BigInteger.probablePrime( bitLength, ThreadLocalRandom.current() );
            r = randomBigInt( bitLength*2/3 ).shiftRight( 1 );
            p = q.multiply( r ).add( BigInteger.ONE );
            int i = 2;
            while ( isNotPrime( p ) && i < 50 ) {
                i++;
                r = r.add( BigInteger.valueOf( (i%2 == 0 ? 1 : -1 ) * i/2 ) );
                p = q.multiply( r ).add( BigInteger.ONE );
            } ;
        } while ( isNotPrime( p ) );
        
//        do {
//            q = BigInteger.probablePrime( bitLength, ThreadLocalRandom.current() );
//            r = randomBigInt( bitLength*2/3 );
//            p = q.multiply( r ).add( BigInteger.ONE );
//        } while ( !p.isProbablePrime( 100 ) );
    
        BigInteger h;
        do {
            h = randomBigInt( p.bitLength()-1 );
        } while ( h.modPow( r, p ).equals( BigInteger.ONE ) );
        
        
        return new Group( p, q, r, h );
    }
    
    private static boolean isNotPrime( BigInteger p ) {
        return !p.isProbablePrime( 32 );
    }
    
    public Group( BigInteger p, BigInteger q, BigInteger r, BigInteger h ) {
        this.p = p;
        this.q = q;
        this.r = r;
        this.h = h;
        this.g = h.modPow( r, p );
    }
    
    public Group( BigInteger p, BigInteger q, BigInteger r, BigInteger h, BigInteger g ) {
        this.p = p;
        this.q = q;
        this.r = r;
        this.h = h;
        this.g = g;
        if( !g.equals( h.modPow( r, p ) ) )
        {
            throw new IllegalArgumentException( "g="+g+" should be equal to h.modPow( r, p )=" + h.modPow( r, p ) );
        }
    }
    
    public BigInteger generateRandomExponent() {
        return randomBigInt( q.bitLength() );
    }
    
    public OptionalInt indexOf( BigInteger element ) {
        int i = 1;
        while(true) {
            var base = g.modPow( BigInteger.valueOf( i ), p );
            if( base.equals( BigInteger.ONE ) )
            {
                break;
            } else if( base.equals( element ) )
            {
                return OptionalInt.of( i );
            }
            i++;
        }
        return OptionalInt.empty();
    }
    
    public List<Pair<Integer,BigInteger>> generateElements() {
        List<Pair<Integer,BigInteger>> list = new ArrayList<>();
        int i = 1;
        while(true) {
            var base = g.modPow( BigInteger.valueOf( i ), p );
            if( base.equals( BigInteger.ONE ) )
            {
                break;
            }
            list.add( new Pair<>(i,base) );
            i++;
        }
        return list;
    }
    
    public BigInteger multiply( BigInteger right, BigInteger left ) {
        return right.multiply( left ).mod( p );
    }
    
    public BigInteger powG( BigInteger power ) {
        return g.modPow( power, p );
    }
    
    public BigInteger pow( BigInteger base, BigInteger power ) {
        return base.modPow( power, p );
    }
    
    public BigInteger divide( BigInteger multiplier, BigInteger divident ) {
        return multiply( multiplier, divident.modInverse( p ) );
    }
    
    @Override
    public String toString() {
        return new StringJoiner( ", ", Group.class.getSimpleName() + "[", "]" )
            .add( "p=" + p )
            .add( "q=" + q )
            .add( "r=" + r )
            .add( "h=" + h )
            .add( "g=" + g )
            .toString();
    }
}
