package lab1.zad2;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author n1t4chi
 */
public class GlibcRandGuesser {
    
    public static void nextGlibcRandValue( int... values )
    {
        int i = values.length;
        if( i < 31 )
        {
            throw new RuntimeException( "not enough numbers" );
        }
        
        var base =(long)values[i-31] + values[i-3];
        int a1 = ( int ) (base % 2147483647);
        int a2 = ( int ) ((base + 1) % 2147483647);
        System.out.println( "Next value will be either " + a1 + " or " + a2 );
    }
    
    public static void main( String[] args ) throws Exception {
        //Paste at least 32 values from glibc rand() function
        nextGlibcRandValue(
            1826437696,
            430377614,
            2106420498,
            1950481524,
            1524995728,
            335144624,
            1595310772,
            796544431,
            1925426181,
            306415582,
            1423805131,
            239992188,
            1348743756,
            1987030717,
            1624148506,
            1232809433,
            6935618,
            2020691296,
            2017206326,
            626785242,
            1990239170,
            1866159953,
            532361948,
            1424120203,
            881917132,
            156268109,
            926483795,
            70930843,
            1240007566,
            1364296508,
            435194906,
            918961614
        );
    }
}
