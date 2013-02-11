package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.AIControllerImpl;
import artificial_player.algorithm.GameOverException;
import artificial_player.algorithm.first_attempt.ExpectationWeightEvaluator;
import artificial_player.algorithm.first_attempt.LinearPlyManager;
import artificial_player.algorithm.first_attempt.RouteSelectorImpl;
import artificial_player.algorithm.first_attempt.StateEnumeratorImpl;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

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

    @Before
    public void setUp() throws Exception {
        my_ai = createAI();
        opponent_ai = createAI();

        List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);

        my_bones = all_bones.subList(0, 7);
        opponent_bones = all_bones.subList(7, 14);
        boneyard_bones = new LinkedList<ImmutableBone>(all_bones.subList(14, 28));

        assertEquals(14, boneyard_bones.size());

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
        Choice opponent_choice, my_choice;

        for (int i = 0; i < 100; ++i) {
            my_choice = my_ai.getBestChoice();
            my_ai.choose(makePickupRandom(my_choice));
            opponent_ai.choose(my_choice);

            System.out.println("My " + my_ai);

            opponent_choice = opponent_ai.getBestChoice();
            opponent_ai.choose(makePickupRandom(opponent_choice));
            my_ai.choose(opponent_choice);

            System.out.println("Opponent " + opponent_ai);

        }
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
