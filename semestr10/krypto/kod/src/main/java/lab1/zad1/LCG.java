package lab1.zad1;

/**
 * Linear congruential generator
 * (i+1)-th next() call returns lcg(i+1) equal to:
 * lcg(i+1) = (multiplier * lcg(i) + increment) % modulus
 * lcg(0) = seed
 *
 * lcg(i) is stored in currentValue;
 * @author n1t4chi
 */
public class LCG {
    
    private final int seed;
    private final LcgParams lcgParams;
    private long currentValue;
    
    public LCG( int seed, LcgParams lcgParams )
    {
        this.seed = seed;
        this.lcgParams = lcgParams;
        
        this.currentValue = seed;
    }
    
    int next()
    {
        var mult = ( long ) lcgParams.getMultiplier() * currentValue;
        var addition = mult + lcgParams.getIncrement();
        int nextValue = (int)( addition % lcgParams.getModulus());
        currentValue = nextValue;
        return nextValue;
    }
    
}
