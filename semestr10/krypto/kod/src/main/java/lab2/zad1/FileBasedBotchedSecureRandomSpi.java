package lab2.zad1;

import lombok.SneakyThrows;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileBasedBotchedSecureRandomSpi extends
    BotchedSecureRandomSpi
{
    private static final String fileName = "iv.txt";
    
    public FileBasedBotchedSecureRandomSpi() {
        load();
    }
    
    @Override
    protected void engineSetSeed( byte[] iv ) {
        super.engineSetSeed( iv );
        save();
    }
    
    @SneakyThrows
    private void save() {
        var iv = getIv();
//        System.out.println( "save" + Arrays.toString( iv ) );
        try( var writer = new BufferedWriter( new FileWriter( fileName ) ) ) {
            for ( var b : iv ) {
                writer.write( String.valueOf( b ) );
                writer.write( "\n" );
            }
        }
    }
    
    @SneakyThrows
    private void load() {
        var file = new File( fileName );
        if( file.exists() ) {
            try ( var reader = new BufferedReader( new FileReader( fileName ) ) ) {
                var ints = reader.lines()
                    .filter( s -> !s.isBlank() )
                    .map( Integer::parseInt )
                    .collect( Collectors.toList() )
                ;
                var iv = new byte[ ints.size() ];
                for ( int i = 0; i < ints.size(); i++ ) {
                    iv[ i ] = ints.get( i ).byteValue();
                }
//                System.out.println( "load" + Arrays.toString( iv ) );
                super.engineSetSeed( iv );
            }
        }
    }
}
