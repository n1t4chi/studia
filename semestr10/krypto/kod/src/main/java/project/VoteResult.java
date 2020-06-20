package project;

public enum VoteResult {
    yes, no, indecisive, invalid;
    
    public static VoteResult get( int yesVotes, int count ) {
        if( yesVotes * 2 == count ) {
            return indecisive;
        }
        return  yesVotes * 2 > count ? yes : no;
    }
}
