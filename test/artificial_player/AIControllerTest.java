package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.AIControllerImpl;
import artificial_player.algorithm.GameOverException;
import artificial_player.algorithm.probablisticAI.ExpectationWeightEvaluator;
import artificial_player.algorithm.probablisticAI.LinearPlyManager;
import artificial_player.algorithm.probablisticAI.RouteSelectorImpl;
import artificial_player.algorithm.probablisticAI.StateEnumeratorImpl;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.randomAI.ConstantPlyManager;
import artificial_player.algorithm.randomAI.RandomEvaluator;
import artificial_player.algorithm.randomAI.SimpleRouteSelector;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 14:22
 */
public class AIControllerTest {
    private AIController my_ai, opponent_ai;
    private List<ImmutableBone> my_bones, opponent_bones;
    private List<ImmutableBone> boneyard_bones;

    private AIController createAI() {
        return new AIControllerImpl(
                new LinearPlyManager(),
                new RouteSelectorImpl(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator());
    }

    private AIController createRandomAI() {
        return new AIControllerImpl(
                new ConstantPlyManager(),
                new SimpleRouteSelector(),
                new StateEnumeratorImpl(),
                new RandomEvaluator());
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
        my_ai = createAI();
        opponent_ai = createAI();

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
            my_ai.choose(choice);
            System.out.println(my_ai);
        }
    }

    @Test(expected = GameOverException.class)
    public void testCompetition() throws Exception {

        for (int i = 0; i < 100; ++i) {
            playOnceEach(my_ai, opponent_ai, true);
        }
    }

    private void playOnceEach(AIController me, AIController opponent, boolean verbose) {
        Choice my_choice = me.getBestChoice();
        me.choose(makePickupRandom(my_choice));
        opponent.choose(my_choice);

        if (verbose)
            System.out.println("My " + me);

        Choice opponent_choice = opponent.getBestChoice();
        opponent.choose(makePickupRandom(opponent_choice));
        me.choose(opponent_choice);

        if (verbose)
            System.out.println("Opponent " + opponent);
    }

    @Test
    public void testAgainstRandom() throws Exception {
        AIController randomAI = createRandomAI();
        randomAI.setInitialState(opponent_bones, false);

        try {
            while (true) {
                playOnceEach(my_ai, randomAI, true);
            }
        } catch (GameOverException err) {
            System.out.println("My AI scored: " + my_ai.getScore());
            System.out.println("Random AI scored: " + randomAI.getScore());
            assertTrue(my_ai.getScore() > randomAI.getScore());
        }
    }


    @Test
    public void testAgainstMyselfLots() throws Exception {
        testAIs(my_ai, opponent_ai);
    }

    @Test
    public void testAgainstRandomLots() throws Exception {
        AIController randomAI = createRandomAI();
        testAIs(my_ai, randomAI);
    }

    private void testAIs(AIController me, AIController opponent) {
        int myScore = 0, randomScore = 0, myWins = 0, randomWins = 0;

        for (int i = 0; i < 100; ++i) {
            setUpBones();
            opponent.setInitialState(opponent_bones, false);
            me.setInitialState(my_bones, true);
            System.gc();

            try {
                while (true) {
                    playOnceEach(me, opponent, false);
                }
            } catch (GameOverException err) {
                System.out.format("Game %d: I %s (%d vs %d)%n", i + 1, (me.getScore() > opponent.getScore() ? "won" : "lost"),
                        me.getScore(), opponent.getScore());

                if (me.getScore() > opponent.getScore()) {
                    myWins += 1;
                    myScore -= opponent.getScore();
                } else {
                    randomWins += 1;
                    randomScore -= me.getScore();
                }
            }
        }

        System.out.format("I won %d and scored a total of %d%n", myWins, myScore);
        System.out.format("Opponent won %d and scored a total of %d", randomWins, randomScore);
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
