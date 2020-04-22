package lab2.zad1;

import java.security.SecureRandomSpi;

class BotchedSecureRandomSpi extends SecureRandomSpi {
    private byte[] iv;
    
    @Override
    protected void engineSetSeed( byte[] iv ) {
//        System.out.println( "Current iv: " + EncryptionEngine.toNiceString( iv ) );
        this.iv = iv;
    }
    
    protected byte[] getIv() {
        return iv;
    }
    
    @Override
    protected void engineNextBytes( byte[] bytes ) {
        initSeed( bytes );
        incrementIv();
        System.arraycopy( iv, 0, bytes, 0, bytes.length );
    }
    
    private void initSeed( byte[] bytes ) {
        if( iv == null )
        {
            engineSetSeed( engineGenerateSeed( bytes.length ) );
        }
    }
    
    protected void incrementIv() {
        for ( int i = iv.length - 1; i >= 0; i-- ) {
            iv[i]++;
            if( iv[i] > Byte.MIN_VALUE )
            {
                break;
            }
        }
        engineSetSeed( iv );
    }
    
    @Override
    protected byte[] engineGenerateSeed( int numBytes ) {
        return new byte[ numBytes ];
    }
}
