package lab2.zad1;

import lombok.SneakyThrows;

import java.io.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Runner {
    private static final int keyStorePathIndex = 0;
    private static final int keyStorePasswordPathIndex = 1;
    private static final int keyIdentifierIndex = 2;
    private static final int keyIdentifierPasswordIndex = 3;
    private static final int modeOfEncryptionIndex = 4;
    private static final int programModeIndex = 5;
    private static final int size = 6;
    
    public static void main( String[] args ) throws Exception {
        if( args.length == 0 || args.length == 1 && args[0].startsWith( "h" ) )
        {
            printHelp();
            return;
        }
        if( args.length < size )
        {
            printHelp();
            throw new IllegalArgumentException( "" );
        }
        var keyStoreFile = getFile( args[ keyStorePathIndex ] );
        var keyStorePasswordFile = getFile( args[ keyStorePasswordPathIndex ] );
        var keyPasswordFile = getFile( args[ keyIdentifierPasswordIndex ] );
        
        var encryptionMode = EncryptionMode.get( args[ modeOfEncryptionIndex ] );
        var keyIdentifier = args[ keyIdentifierIndex ];
        var programMode = getProgramMode( args[ programModeIndex ] );
        if( encryptionMode.isEmpty() )
        {
            printHelp();
            throw new IllegalArgumentException( "Invalid encryption mode! " + args[ modeOfEncryptionIndex ] );
        }
        var keyStore = EncryptionEngine.getKeyStore( keyStoreFile, getPassword( keyStorePasswordFile, "key store") );
        var key = EncryptionEngine.getKey( keyStore, keyIdentifier, getPassword( keyPasswordFile, "key store") );
        var engine = EncryptionEngine.get(
            encryptionMode.get(),
            key,
            new BotchedSecureRandom( new FileBasedBotchedSecureRandomSpi() )
        );
        
        switch ( programMode ) {
            case oracle -> startOracle( engine );
            case challenge -> startChallenge( engine );
        }
    }
    
    @SneakyThrows
    private static void startChallenge( EncryptionEngine engine ) {
        var reader = new BufferedReader( new InputStreamReader( System.in ) );
        System.out.println( "Challenge mode. Enter two messages and guess which one was encrypted." );
        System.out.println( "First message:" );
        String first = reader.readLine();
        System.out.println( "Send message:" );
        String second = reader.readLine();
        Challenge challenge = engine.challenge( first, second );
        System.out.println( "Cryptogram:\n" +
            EncryptionEngine.bytesToString( challenge.getCryptogram().getCipherText() )
        );
        System.out.println( "Guess which message is this? (1/2)" );
        String line;
        while( !is1or2(line = reader.readLine()) ) {
            System.out.println( "Wrong input, type either 1 or 2" );
        }
        int i = Integer.parseInt( line );
        if( i == challenge.getMessageIndex() ) {
            System.out.println( "You've won!" );
        } else {
            System.out.println( "You've lost!" );
        }
    }
    
    private static boolean is1or2( String s ) {
        return "1".equals( s ) || "2".equals( s );
    }
    
    @SneakyThrows
    private static void startOracle( EncryptionEngine engine ) {
        var reader = new BufferedReader( new InputStreamReader( System.in ) );
        System.out.println( "Oracle mode. Type messages and receive cryptograms for each, empty message quits." );
        while ( true ) {
            System.out.println( "Type message:" );
            var line = reader.readLine();
            if( line == null || line.isEmpty() ) break;
            System.out.println(
                EncryptionEngine.bytesToString( engine.encrypt( line ).getCipherText() )
            );
        }
        System.out.println( "Quitting." );
    }
    
    private static File getFile( String fileName ) {
        File file = new File( fileName );
        if( !file.exists() )
        {
            printHelp();
            throw new IllegalArgumentException( "File " + fileName+ " does not exists" );
        }
        if( !file.canRead() )
        {
            printHelp();
            throw new IllegalArgumentException( "No read permission for " + fileName+ " file" );
        }
        return file;
    }
    
    @SneakyThrows
    private static char[] getPassword( File passwordFile, String name ) {
        try( Reader reader = new FileReader( passwordFile ) )
        {
            char[] buffer = new char[512];
            int charsRead = reader.read( buffer );
            return Arrays.copyOf( buffer, charsRead );
        }
    }
    
    private static void printHelp() {
        System.out.println( """
        program parameters: <key store path> <config file path> <key store password file path> <key password file path> <mode of encryption> <program mode>
          <key store path> - Path to key store
          <key store password file path> - Path to file with password to keystore
          <key identifier> - Identifier of a key in key store to be used in encryption
          <key password file path> - Path to file with password to key
          <mode of encryption> - Encryption mode, one of:
        """ + indentedPerLine( EncryptionMode.values(), EncryptionMode::getParameterName ) + """
          <program mode>: mode of a program, one of:
        """ + indentedPerLine( ProgramMode.values(), ProgramMode::toString )
        );
    }
    
    private static <T> String indentedPerLine(
        T[] values,
        Function< T, String > getter
    ) {
        return Arrays.stream( values )
                .map( getter )
                .collect( Collectors.joining( "\n    ", "    ", "\n" ));
    }
    
    private static ProgramMode getProgramMode( String arg ) {
        try {
            return ProgramMode.valueOf( arg );
        } catch ( Exception ex ) {
            printHelp();
            throw ex;
        }
    }
}
