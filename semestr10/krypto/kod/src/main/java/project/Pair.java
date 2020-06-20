package project;

import java.util.StringJoiner;

public record Pair<O1,O2>(O1 o1, O2 o2) {
    
    @Override
    public String toString() {
        return "("+o1+","+o2+")";
    }
}
