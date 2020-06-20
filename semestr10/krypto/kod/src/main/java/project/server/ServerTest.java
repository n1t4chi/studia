package project.server;

import lombok.*;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import project.*;
import project.Vote;
import project.dto.*;
import project.exceptions.*;

import java.math.BigInteger;
import java.util.*;

public class ServerTest {
    
    @Test
    void withoutConnectingFirst_cannotDoAnything() {
        var system = new VotingSystem();
        String id = "someId";
        throwsNotConnected( () -> system.checkForNewQuestion( new CheckForNewQuestionRequest( id ) ) );
        throwsNotConnected( () -> system.askQuestion( new AskQuestionRequest( id, "Can I?" ) ) );
        throwsNotConnected( () -> system.checkResults( new CheckResultsRequest( id ) ) );
        throwsNotConnected( () -> system.prepareToVote( new PrepareVoteRequest( id, BigInteger.ONE ) ) );
        throwsNotConnected( () -> system.vote( new VoteRequest( id, BigInteger.ONE ) ) );
    }
    
    @Test
    void questionAsker_cannotVote() {
        var system = new VotingSystem();
    
        var asker = new TestVoter();
        var voter1 = new TestVoter();
        var voter2 = new TestVoter();
        var voter3 = new TestVoter();
        
        asker.connect( system );
        voter1.connect( system );
        voter2.connect( system );
        voter3.connect( system );
        
        asker.askQuestion( system, "Can I habe question, pls?" );
        
        throwsNotVoting( () -> system.prepareToVote( new PrepareVoteRequest( asker.id, BigInteger.ONE ) ) );
        throwsNotVoting( () -> system.vote( new VoteRequest( asker.id, BigInteger.ONE ) ) );
    }
    
    private void throwsNotConnected( Executable call ) {
        Assertions.assertThrows( NotConnectedException.class, call );
    }
    
    private void throwsNotVoting( Executable call ) {
        Assertions.assertThrows( NotVotingException.class, call );
    }
    
    @Test
    void e2e() {
        var system = new VotingSystem();
        
        var asker = new TestVoter();
        var voter1 = new TestVoter();
        var voter2 = new TestVoter();
        var voter3 = new TestVoter();
    
        //connect
        Assertions.assertNull( asker.id() );
        asker.connect( system );
        Assertions.assertNotNull( asker.id() );
        
        Assertions.assertNull( voter1.id() );
        voter1.connect( system );
        Assertions.assertNotNull( voter1.id() );
        
        Assertions.assertNull( voter2.id() );
        voter2.connect( system );
        Assertions.assertNotNull( voter2.id() );
        
        Assertions.assertNull( voter3.id() );
        voter3.connect( system );
        Assertions.assertNotNull( voter3.id() );
        
        //check if there is question
        asker.checkForNewQuestion( system );
        voter1.checkForNewQuestion( system );
        voter2.checkForNewQuestion( system );
        voter3.checkForNewQuestion( system );
        
        Assertions.assertNull( asker.question() );
        Assertions.assertNull( voter1.question() );
        Assertions.assertNull( voter2.question() );
        Assertions.assertNull( voter3.question() );
        
        var question = "Can I haz cheeseburger?";
        
        asker.askQuestion( system, question );
        voter1.askQuestion( system, "Was I too late?" );
        Assertions.assertTrue( asker.questionAsked() );
        Assertions.assertFalse( voter1.questionAsked() );
        
        
        voter1.checkForNewQuestion( system );
        voter2.checkForNewQuestion( system );
        voter3.checkForNewQuestion( system );
    
        var group = system.currentGroup();
        
        assertWithNewQuestion( voter1, question, group, 3 );
        assertWithNewQuestion( voter2, question, group, 3 );
        assertWithNewQuestion( voter3, question, group, 3 );
        Assertions.assertEquals(
            Set.of( 0, 1, 2 ),
            Set.of(
                voter1.index(),
                voter2.index(),
                voter3.index()
            )
        );
        voter1.prepareToVote( system );
        voter2.prepareToVote( system );
        
        voter1.canVote( system );
        Assertions.assertFalse( voter1.canVote() );
        
        voter3.prepareToVote( system );
        
        asker.checkResults( system );
        Assertions.assertNull( asker.votingResults() );
        
        voter1.canVote( system );
        Assertions.assertTrue( voter1.canVote() );
        var expectedGtoXs = system.gToXs();
        Assertions.assertNotNull( expectedGtoXs );
        Assertions.assertEquals( expectedGtoXs, voter1.listOf_g_to_x() );
        voter2.canVote( system );
        Assertions.assertTrue( voter2.canVote() );
        Assertions.assertEquals( expectedGtoXs, voter2.listOf_g_to_x() );
        voter3.canVote( system );
        Assertions.assertTrue( voter3.canVote() );
        Assertions.assertEquals( expectedGtoXs, voter3.listOf_g_to_x() );
        
        voter1.vote( system, Vote.yes );
        voter2.vote( system, Vote.no );
        voter3.vote( system, Vote.no );
    
        var results = system.results();
        
        asker.checkResults( system );
        Assertions.assertEquals( results, asker.votingResults() );
        voter1.checkResults( system );
        Assertions.assertEquals( results, asker.votingResults() );
        voter2.checkResults( system );
        Assertions.assertEquals( results, asker.votingResults() );
        voter3.checkResults( system );
        Assertions.assertEquals( results, asker.votingResults() );
        
        Assertions.assertEquals( VoteResult.no, results.getMajority() );
        Assertions.assertEquals( 1, results.getYesCount() );
    }
    
    private void assertWithNewQuestion(
        TestVoter voter,
        String question,
        Group group,
        int participants
    ) {
        Assertions.assertEquals( question, voter.question() );
        Assertions.assertEquals( group, voter.group() );
        Assertions.assertEquals( participants, voter.participants() );
    }
    
    @Accessors(fluent = true)
    @Getter
    private static class TestVoter {
        private Vote vote;
    
        private String id;
        private String question;
        private int index;
        private int participants;
        private Group group;
        
        private Boolean questionAsked;
        private Boolean canVote;
        private VotingResults votingResults;
        
        private BigInteger x;
        private List<BigInteger> listOf_g_to_x;
    
        public BigInteger generateXandGetGtoX() {
            x = group.generateRandomExponent();
            return group.powG( x );
        }
    
        public void vote( VotingSystem system, Vote vote ) {
            this.vote = vote;
            var gToC = Util.getGtoC( group, vote, x, index, participants, listOf_g_to_x );
            var result = system.vote( new VoteRequest( id, gToC ) );
        }
    
        public int index() {
            return index;
        }
    
        public int participants() {
            return participants;
        }
    
        public void connect( VotingSystem system ) {
            var result = system.connect( new ConnectRequest() );
            id = result.getId();
        }
    
        public void checkForNewQuestion( VotingSystem system ) {
            var result = system.checkForNewQuestion( new CheckForNewQuestionRequest( id ) );
            question = result.getQuestion();
            index = result.getIndex();
            participants = result.getParticipants();
            group = result.getGroup();
        }
    
        public String id() {
            return id;
        }
    
        public void askQuestion( VotingSystem system, String question ) {
            var result = system.askQuestion( new AskQuestionRequest( id, question ) );
            questionAsked = result.isAccepted();
        }
    
        public void prepareToVote( VotingSystem system ) {
            var result = system.prepareToVote( new PrepareVoteRequest( id, generateXandGetGtoX() ) );
        }
    
        public void checkResults( VotingSystem system ) {
            var result = system.checkResults( new CheckResultsRequest( id ) );
            votingResults = result.getResults();
        }
    
        public void canVote( VotingSystem system ) {
            var result = system.canVote( new CanVoteRequest( id ) );
            canVote = result.isVotingAllowed();
            if( canVote ) {
                listOf_g_to_x = result.getGToXs();
            }
        }
    }
}
