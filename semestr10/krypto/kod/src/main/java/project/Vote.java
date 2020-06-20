package project;

import project.client.Voter;

import java.util.concurrent.ThreadLocalRandom;

public enum Vote {
    yes,
    no;
    
    public static Vote random() {
        return ThreadLocalRandom.current().nextBoolean() ? yes : no;
    }
    
    public static boolean votedYes( Voter voter ) {
        return voter.getVote() == yes;
    }
}
