package project;

import org.junit.jupiter.api.*;
import project.client.*;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.*;

import static project.Util.*;

public class SystemTest {
    
    public static final int BIT_LENGTH = 8;
    
    @Test
    void checkAlgoConst() {
        Group group = new Group(
            BigInteger.valueOf( 113 ),
            BigInteger.valueOf( 19 ),
            BigInteger.valueOf( 6 ),
            BigInteger.valueOf( 17 )
        );
        
        System.out.println( "Group: " + group );
        System.out.println( "Elemets: \n" + indentedPerLine( group.generateElements() ) );
        
        List<Voter> voters = List.of(
            new Voter( 0, BigInteger.valueOf( 5 ), Vote.yes ),
            new Voter( 1, BigInteger.valueOf( 2 ), Vote.yes ),
            new Voter( 2, BigInteger.valueOf( 4 ), Vote.yes )
        );
        
        System.out.println( "Voters: \n" + indentedPerLine( voters ) );
        
        var listOf_g_to_x = voters.stream()
            .map( voter -> voter.getGtoX( group ) )
            .collect( Collectors.toList() )
        ;
        System.out.println( "listOf_g_to_x: \n" + indentedPerLine( listOf_g_to_x ) );
        
        var listOf_g_to_cy = voters.stream()
            .map( voter -> voter.getGtoC( group, listOf_g_to_x, voters.size() ) )
            .collect( Collectors.toList() );
        
        System.out.println( "listOf_g_to_cy: \n" + indentedPerLine( listOf_g_to_cy ) );
        
        var g_to_cyi = listOf_g_to_cy
            .stream()
            .reduce( group::multiply )
            .orElseThrow()
        ;
        
        
        System.out.println( "g_to_cyi: \n" + g_to_cyi );
        
        var index = group.indexOf( g_to_cyi );
        
        System.out.println( "index: \n" + index );
        
        Assertions.assertTrue( index.isPresent() );
        Assertions.assertEquals( index.getAsInt(),  voters.stream().filter( Vote::votedYes ).count() );
    }
    
    @Test
    void checkAlgo() {
        Group group = Group.create( BIT_LENGTH );
    
        System.out.println( "Group: " + group );
        
        List<Voter> voters = IntStream.range( 0, 5 )
            .mapToObj( this::create )
            .collect( Collectors.toList() );
    
        System.out.println( "Voters: \n" + indentedPerLine( voters ) );
        
        var listOf_g_to_x = voters.stream()
            .map( voter -> voter.getGtoX( group ) )
            .collect( Collectors.toList() )
        ;
        System.out.println( "listOf_g_to_x: \n" + indentedPerLine( listOf_g_to_x ) );
    
        var listOf_g_to_cy = voters.stream()
            .map( voter -> voter.getGtoC( group, listOf_g_to_x, voters.size() ) )
            .collect( Collectors.toList() );
    
        System.out.println( "listOf_g_to_cy: \n" + indentedPerLine( listOf_g_to_cy ) );
        
        var g_to_cyi = listOf_g_to_cy
            .stream()
            .reduce( group::multiply )
            .orElseThrow()
        ;
    
        System.out.println( "g_to_cyi: \n" + g_to_cyi );
    
        var index = group.indexOf( g_to_cyi );
    
        System.out.println( "index: \n" + index );
    
        Assertions.assertTrue( index.isPresent() );
        Assertions.assertEquals( index.getAsInt(),  voters.stream().filter( Vote::votedYes ).count() );
    }
    
    
    Voter create( int index ) {
        return new Voter( index, randomBigInt( BIT_LENGTH ), Vote.random() );
    }
    
}
