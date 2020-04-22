package lab2.zad1;

import java.util.Arrays;
import java.util.Optional;

public enum EncryptionMode {
    aes_cbc( "AES_128/CBC/NOPADDING", "CBC"),
    aes_ofb( "AES_128/OFB/NOPADDING", "CTR" ),
    aes_ctr( "AES_128/GCM/NOPADDING", "CBC" ),
    ;
    
    public static Optional<EncryptionMode> get( String name )
    {
        return Arrays.stream( values() )
            .filter( mode -> mode.getParameterName().equalsIgnoreCase( name ) )
            .findAny()
        ;
    }
    
    private String algorithmName;
    private String parameterName;
    
    EncryptionMode( String algorithmName, String parameterName ) {
        
        this.algorithmName = algorithmName;
        this.parameterName = parameterName;
    }
    
    String getAlgorithmName() {
        return algorithmName;
    }
    
    String getParameterName() {
        return parameterName;
    }
}
