package project.server;

import project.*;
import project.dto.*;
import project.exceptions.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class VotingSystem {
    public static final int BIT_LENGTH = 1024;
    private final Map<String, ServerSideVoter> connected = new HashMap<>();
    
    private Map<String, ServerSideVoter> currentVoters;
    private String currentQuestion;
    private Group currentGroup;
    private Map<Integer,BigInteger> gToXs = new HashMap<>();
    private Map<Integer,BigInteger> votes = new HashMap<>();
    private VotingResults votingResults;
    
    public ConnectResponse connect( ConnectRequest request ) {
        var uuid = UUID.randomUUID().toString();
        connected.put( uuid, new ServerSideVoter() );
        return new ConnectResponse( uuid );
    }
    
    public CheckForNewQuestionResponse checkForNewQuestion( CheckForNewQuestionRequest request ) {
        assertConnected( request.getId() );
        if( currentQuestion == null )
        {
            return new CheckForNewQuestionResponse( null, null, -1, -1 );
        }
        assertVoting( request.getId() );
        return new CheckForNewQuestionResponse(
            currentQuestion,
            currentGroup,
            indexOf( request.getId() ),
            currentVoters.size()
        );
    }
    
    public PrepareVoteResponse prepareToVote( PrepareVoteRequest request ) {
        assertConnected( request.getId() );
        assertVoting( request.getId() );
        addGtoX( indexOf( request.getId() ), request.getGToX() );
        return null;
    }
    
    public CanVoteResponse canVote( CanVoteRequest canVoteRequest ) {
        assertConnected( canVoteRequest.getId() );
        assertVoting( canVoteRequest.getId() );
        if( everyonePreparedVote() ) {
            return new CanVoteResponse( true, gToXs() );
        }
        return new CanVoteResponse( false, null );
    }
    
    public AskQuestionResponse askQuestion( AskQuestionRequest request ) {
        assertConnected( request.getId() );
        if( currentQuestion == null ) {
            currentQuestion = request.getQuestion();
            currentGroup = Group.create( BIT_LENGTH );
    
            currentVoters = new TreeMap<>( connected );
            currentVoters.remove( request.getId() );
            
            return new AskQuestionResponse( true );
        }
        return new AskQuestionResponse( false );
    }
    
    public VoteResponse vote( VoteRequest request ) {
        assertConnected( request.getId() );
        assertVoting( request.getId() );
        if( !everyonePreparedVote() ) {
            throw new NotEveryoneVotedException();
        }
        addVote( indexOf( request.getId() ), request.getVote() );
        if( everyoneVoted() ) {
            currentQuestion = null;
    
            var votes = votes();
            var g_to_cyi = votes
                .stream()
                .reduce( currentGroup::multiply )
                .orElseThrow()
            ;
            var yesCount = currentGroup.indexOf( g_to_cyi );
            if( yesCount.isEmpty() )
            {
                votingResults = new VotingResults( null, null, VoteResult.invalid, -1 );
            }
            else
            {
                var yesCountAsInt = yesCount.getAsInt();
                votingResults = new VotingResults(
                    gToXs(),
                    votes,
                    VoteResult.get( yesCountAsInt, currentVoters.size() ),
                    yesCountAsInt
                );
            }
        }
        return new VoteResponse();
    }
    
    private List<BigInteger> toSortedListByIndex( Map<Integer, BigInteger> indexedMap ) {
        var indices = new ArrayList<>( indexedMap.keySet() );
        indices.sort( Integer::compareTo );
        return  indexedMap.entrySet().stream()
            .sorted( Comparator.comparingInt( Map.Entry::getKey ) )
            .map( Map.Entry::getValue )
            .collect( Collectors.toList() );
    }
    
    private void addVote( int index, BigInteger vote ) {
        votes.put( index, vote );
    }
    
    private void addGtoX( int index, BigInteger gToX ) {
        gToXs.put( index, gToX );
    }
    
    public CheckResultsResponse checkResults( CheckResultsRequest request ) {
        assertConnected( request.getId() );
        if( votingResults != null ) {
            return new CheckResultsResponse( votingResults );
        }
        return new CheckResultsResponse( null );
    }
    
    public List<BigInteger> votes() {
        
        return toSortedListByIndex( votes );
    }
    
    public List<BigInteger> gToXs() {
        return toSortedListByIndex( gToXs );
    }
    
    public Group currentGroup() {
        return currentGroup;
    }
    
    public VotingResults results() {
        return votingResults;
    }
    
    private boolean everyoneVoted() {
        return votes.size() == currentVoters.size();
    }
    
    private boolean everyonePreparedVote() {
        return gToXs.size() == currentVoters.size();
    }
    
    private void assertConnected( String id ) {
        if( !connected.containsKey( id ) ) {
            throw new NotConnectedException();
        }
    }
    
    private void assertVoting( String id ) {
        if( !currentVoters.containsKey( id ) ) {
            throw new NotVotingException();
        }
    }
    
    private int indexOf( String id ) {
        var list = new ArrayList<>( currentVoters.keySet() );
        list.sort( String::compareTo );
        return list.indexOf( id );
    }
}
