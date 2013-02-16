package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.AIControllerImpl;
import artificial_player.algorithm.GameOverException;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AIControllerTest {
    private AIController my_ai, opponent_ai;
    private List<ImmutableBone> my_bones, opponent_bones;
    private List<ImmutableBone> boneyard_bones;

    @Test(expected = GameOverException.class)
    public void testSlowHand() throws Exception {
        my_bones = new LinkedList<ImmutableBone>();
        my_bones.add(new ImmutableBone(3,4));
        my_bones.add(new ImmutableBone(1, 4));
        my_bones.add(new ImmutableBone(1, 5));
        my_bones.add(new ImmutableBone(1, 2));
        my_bones.add(new ImmutableBone(0, 6));
        my_bones.add(new ImmutableBone(0, 3));
        my_bones.add(new ImmutableBone(3, 6));

        opponent_bones = new LinkedList<ImmutableBone>();
        opponent_bones.add(new ImmutableBone(0, 2));
        opponent_bones.add(new ImmutableBone(0, 4));
        opponent_bones.add(new ImmutableBone(1, 1));
        opponent_bones.add(new ImmutableBone(0, 0));
        opponent_bones.add(new ImmutableBone(0, 5));
        opponent_bones.add(new ImmutableBone(5, 5));
        opponent_bones.add(new ImmutableBone(1, 6));

        boneyard_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        boneyard_bones.removeAll(my_bones);
        boneyard_bones.removeAll(opponent_bones);
        Collections.shuffle(boneyard_bones);

        assertEquals(14, boneyard_bones.size());

        my_ai.setInitialState(my_bones, true);
        opponent_ai.setInitialState(opponent_bones, false);

        testCompetition();
    }

    private void setUpBones() {
        List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);

        my_bones = all_bones.subList(0, 7);
        opponent_bones = all_bones.subList(7, 14);
        boneyard_bones = new LinkedList<ImmutableBone>(all_bones.subList(14, 28));
    }

    @Before
    public void setUp() throws Exception {
        my_ai = AIControllerImpl.createProbabilisticAI();
        opponent_ai = AIControllerImpl.createProbabilisticAI();

        setUpBones();

        my_ai.setInitialState(my_bones, true);
        opponent_ai.setInitialState(opponent_bones, false);
    }

    @Test
    public void testSetInitialState() throws Exception {
        my_ai.setInitialState(my_bones, true);
    }

    @Test(expected = GameOverException.class)
    public void testChoosing() throws Exception {
        my_ai.setInitialState(my_bones, true);

        int n = 0;

        while (n++ < 1000) {
            Choice choice = my_ai.getBestChoice();
            my_ai.choose(makePickupRandom(choice));
            System.out.println(my_ai);
        }
    }

    @Test(expected = GameOverException.class)
    public void testCompetition() throws Exception {

        for (int i = 0; i < 100; ++i) {
            playOnceEach(my_ai, opponent_ai, true);
        }
    }

    private void playOnceEach(AIController player1, AIController player2, boolean verbose) {
        Choice my_choice = player1.getBestChoice();
        player1.choose(makePickupRandom(my_choice));
        player2.choose(my_choice);

        if (verbose)
            System.out.println("Player 1 " + player1);

        Choice opponent_choice = player2.getBestChoice();
        player2.choose(makePickupRandom(opponent_choice));
        player1.choose(opponent_choice);

        if (verbose)
            System.out.println("Player 2 " + player2);
    }

    @Test
    public void testAgainstRandom() throws Exception {
        AIController randomAI = AIControllerImpl.createRandomAI();
        randomAI.setInitialState(opponent_bones, false);

        try {
            while (true) {
                playOnceEach(my_ai, randomAI, true);
            }
        } catch (GameOverException err) {
            System.out.println("My AI hand weighed: " + my_ai.getHandWeight());
            System.out.println("Random AI's hand weighed: " + randomAI.getHandWeight());
            //assertTrue(my_ai.getHandWeight() < randomAI.getHandWeight());
            if (my_ai.hasEmptyHand() || my_ai.getHandWeight() < randomAI.getHandWeight())
                System.out.println("I beat the random AI!");
            else
                System.out.println("Aww, I didn't beat the random AI :-( ");
        }
    }


    @Test
    public void testAgainstMyselfLots() throws Exception {
        testAIs(my_ai, opponent_ai, 100);
    }

    @Test
    public void testAgainstRandomLots() throws Exception {
        my_ai = AIControllerImpl.createQuickerProbabilisticAI();
        AIController randomAI = AIControllerImpl.createRandomAI();
        assertEquals(my_ai, testAIs(my_ai, randomAI, 10000));
    }

    @Test
    public void testRandomAgainstRandomLots() throws Exception {
        AIController randomAI = AIControllerImpl.createRandomAI();
        my_ai = AIControllerImpl.createRandomAI();
        testAIs(my_ai, randomAI, 1000);
    }

    /**
     * Plays a game of dominos between the two AIControllers - first to get pointsToWin wins
     * (and is returned).
     *
     * @param me the player representing me (ie. the AIController to test).
     * @param opponent the player representing the opponent (ie. the AIController to benchmark against).
     * @param pointsToWin the number of points required to win the game.
     * @return the winning AIController.
     */
    private AIController testAIs(AIController me, AIController opponent, int pointsToWin) {
        int myScore = 0, opponentScore = 0, myWins = 0, opponentWins = 0, i = 0;
        boolean meFirst = true;

        while (myScore < pointsToWin && opponentScore < pointsToWin) {
            i += 1;

            setUpBones();
            opponent.setInitialState(opponent_bones, !meFirst);
            me.setInitialState(my_bones, meFirst);
            System.gc();

            try {
                while (true) {
                    if (meFirst)
                        playOnceEach(me, opponent, false);
                    else
                        playOnceEach(opponent, me, false);
                }
            } catch (GameOverException err) {
                final AIController winner;
                if (me.hasEmptyHand() && !opponent.hasEmptyHand())
                    winner = me;
                else if (!me.hasEmptyHand() && opponent.hasEmptyHand())
                    winner = opponent;
                else {
                    if (me.getHandWeight() < opponent.getHandWeight())
                        winner = me;
                    else if (me.getHandWeight() > opponent.getHandWeight())
                        winner = opponent;
                    else
                        winner = null;
                }

                System.out.format("Game %d: I %s (%d vs %d)%n", i,
                        (winner == me ? "won" : (winner == opponent ? "lost" : "draw") ),
                        me.getHandWeight(), opponent.getHandWeight());

                if (winner == me) {
                    myWins += 1;
                    myScore += opponent.getHandWeight();
                } else if (winner == opponent) {
                    opponentWins += 1;
                    opponentScore += me.getHandWeight();
                } else {
                    --i;    // If draw, replay.
                }

                meFirst = !meFirst;
            }
        }

        System.out.format("I won %d and scored a total of %d%n", myWins, myScore);
        System.out.format("Opponent won %d and scored a total of %d", opponentWins, opponentScore);

        if (myScore > opponentScore)
            return me;
        else
            return opponent;
    }

    private Choice makePickupRandom(Choice choice) {

        if (choice.getAction() == Choice.Action.PICKED_UP) {
            if (boneyard_bones.isEmpty()) {
                System.out.println("Tried to take from empty boneyard! Choice = " + choice);
                return null;
            }
            return new Choice(choice.getAction(), boneyard_bones.remove(0));
        }

        return choice;
    }
}
