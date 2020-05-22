package lab3.zad1;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface IndexedIterable extends Iterable<BigInteger> {
    List<BigInteger> values();
    
    default void forEach( IndexedConsumer<BigInteger> consumer ) {
        IntStream.range( 0, length() )
            .forEach( i -> consumer.apply( i, get( i ) ) );
    }
    
    @Override
    default Iterator<BigInteger> iterator() {
        return values().iterator();
    }
    
    default Iterable<Indexed<BigInteger>> indexedIterable() {
        return () -> indexedStream().iterator();
    }
    
    default BigInteger get( int i ) {
        return values().get( i );
    }
    
    default Indexed<BigInteger> getValue( int i ) {
        return new Indexed<>( i, get( i ) );
    }
    
    default Stream<BigInteger> stream() {
        return values().stream();
    }
    
    default Stream<Indexed<BigInteger>> indexedStream() {
        return IntStream.range( 0, length() )
            .mapToObj( this::getValue );
    }
    
    default int length() {
        return values().size();
    }
}
