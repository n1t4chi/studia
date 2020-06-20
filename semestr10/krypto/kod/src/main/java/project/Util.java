package project;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public interface Util {
    static BigInteger randomBigInt( int bitSize ) {
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
    
    static String indentedPerLine( Collection<?> objects) {
        return objects.stream()
            .map( Util::indent )
            .collect( Collectors.joining("\n"))
        ;
    }
    
    static String indent( Object object ) {
        var string = Objects.toString( object );
        return "\t" + string.replace( "\n", "\t\n" );
    }
    
    static String perLine( Collection<?> objects) {
        return objects.stream()
            .map( Objects::toString )
            .collect( Collectors.joining("\n"))
        ;
    }
    
    static String spaces( int i ) {
        return  i <= 0 ? "" : String.format( "%"+i+"s", " ");
    }
    
    static String leftPad( Object object, int i ) {
        var value = String.valueOf( object );
        return spaces(i - value.length()) + value;
    }
    
    static BigInteger getGtoC(
        Group group,
        Vote vote,
        BigInteger x,
        int index,
        int n,
        List<BigInteger> listOf_g_to_x
    ) {
        var toMultiply = IntStream.rangeClosed( 0, index - 1 )
            .mapToObj( listOf_g_to_x::get )
            .collect( Collectors.toList() );
        System.out.println( index+" -> toMultiply: " + toMultiply );
        
        var multipler = toMultiply.stream()
            .reduce( group::multiply )
            .orElse( BigInteger.ONE )
        ;
        var toDivide = IntStream.range( index + 1, n )
            .mapToObj( listOf_g_to_x::get )
            .collect( Collectors.toList() )
        ;
        System.out.println( index+" -> toDivide: " + toDivide );
        var divident = toDivide
            .stream()
            .reduce( group::multiply )
            .orElse( BigInteger.ONE )
        ;
        var g_to_yi = group.divide( multipler, divident );
        System.out.println( "g_to_y["+index+"] "+multipler+"/"+divident+"= " + g_to_yi );
        var ret = group.pow( g_to_yi, x );
        System.out.println( "g_to_xy*yi["+index+"] "+g_to_yi+"^"+x+"= " + ret );
        
        /*
        here in case of picking yes ('vetoing') instead of using some random value,
        we simply multiply by g.
        that way we have r_i = g*g^c_i*x_i
        Now when we multiply each r_i we get g^|[votes for yes]|
        We can then check if it's bigger than n/2 and have our result
        */
        
        if( Vote.yes.equals( vote ) )
        {
            ret = group.multiply( ret, group.getG() );
        }
        return ret;
    }
}
