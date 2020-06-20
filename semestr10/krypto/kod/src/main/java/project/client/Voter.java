package project.client;

import lombok.Data;
import project.*;

import java.math.BigInteger;
import java.util.List;

import static project.Util.leftPad;

@Data
public class Voter {
    private int index;
    private BigInteger x;
    private Vote vote;
    
    public Voter( int index, BigInteger x, Vote vote ) {
        this.index = index;
        this.x = x;
        this.vote = vote;
    }
    
    public Voter() {}
    
    @Override
    public String toString() {
        return index+"[" + leftPad( vote, 3 ) + "]x=" + x;
    }
    
    public BigInteger getGtoC( Group group, List<BigInteger> listOf_g_to_x, int n ) {
        return Util.getGtoC( group, vote, x, index, n, listOf_g_to_x );
    }
    
    public BigInteger getGtoX( Group group ) {
        return group.powG( x );
    }
}
