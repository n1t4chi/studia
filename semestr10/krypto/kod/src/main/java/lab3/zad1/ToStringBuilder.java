package lab3.zad1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToStringBuilder {
    private final String objectName;
    private final List<NameAndValue> parameters = new ArrayList<>();
    
    public ToStringBuilder( String objectName ) {
        this.objectName = objectName;
    }
    
    ToStringBuilder add(String name, List<?> value) {
        parameters.add( new NameAndValue( name, listToString( value ) ) );
        return this;
    }
    
    private String listToString( List<?> value ) {
        return "{\n"+value.stream().map( this::indent ).collect( Collectors.joining("\n"))+"\n}";
    }
    
    ToStringBuilder add(String name, Object value) {
        parameters.add( new NameAndValue( name, value ) );
        return this;
    }
    
    private String indent( Object value )
    {
        return "\t" + String.valueOf( value ).replace( "\n", "\n\t" );
    }
    
    String build() {
        return objectName + "{\n" +
            parameters.stream().map( param -> param.name + "="+param.value ).map( this::indent ).collect( Collectors.joining("\n") ) +
        "\n}";
    }
    
    private record NameAndValue (String name, Object value) {}
}
