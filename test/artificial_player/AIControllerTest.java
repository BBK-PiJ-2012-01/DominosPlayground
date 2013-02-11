package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.first_attempt.ExpectationWeightEvaluator;
import artificial_player.algorithm.first_attempt.LinearPlyManager;
import artificial_player.algorithm.first_attempt.RouteSelectorImpl;
import artificial_player.algorithm.first_attempt.StateEnumeratorImpl;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.Route;
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
        return new AIController(
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

    @Test
    public void testChoosing() throws Exception {
        my_ai.setInitialState(my_bones, true);

        Choice choice = my_ai.getBestChoice();

        while (choice != null) {
            my_ai.choose(choice);
            System.out.println((!my_ai.getState().isMyTurn()? "I" : "Opponent") + " chose to: " + choice);
            System.out.println("\t" + my_ai.getLayout() + " size = " + my_ai.getLayout().size());
            System.out.println("\tmy bones = " + my_ai.getMyBones() + " boneyard = " + my_ai.getState().getSizeOfBoneyard()
                    + " opponent's bones = " + my_ai.getState().getSizeOfOpponentHand());

//            choice = my_ai.getRouteSelector().getBestRoute(my_ai.getState()).getEarliestChoice();
            choice = my_ai.getBestChoice();
        }
    }

    @Test
    public void testGetBestRoutes() throws Exception {
        my_ai.setInitialState(my_bones, false);

        for (Route route : my_ai.getBestRoutes()) {
            System.out.println(route);
        }

        Choice opponent_choice = my_ai.getBestChoice();
        System.out.println("Opponent chose to: " + opponent_choice);
        my_ai.choose(opponent_choice);

        for (Route route : my_ai.getBestRoutes()) {
            System.out.println(route);
        }

        for (Route route : my_ai.getBestRoutes()) {
            System.out.println(route);
        }
    }

    private String getLayoutString(AIController ai) {
        return String.format("[%d, ... , %d]", ai.getState().getLayoutLeft(), ai.getState().getLayoutRight());
    }

    @Test
    public void testCompetition() throws Exception {
        Choice opponent_choice, my_choice;

        for (int i = 0; i < 100; ++i) {
            my_choice = makePickupRandom(my_ai.getBestChoice());
            if (my_choice == null) {
                System.out.println("My desired status: " + my_ai.getState().getDesiredStatus());
                System.out.println("My choice was null!\n" + my_ai.getState());
                //System.out.println(my_ai.getBestRoute());
            }

            try {
                my_ai.choose(my_choice);
            } catch (OutOfMemoryError err) {
                throw new RuntimeException();
            }

            try {
                opponent_ai.choose(makePickupUnknown(my_choice));
            } catch (OutOfMemoryError err) {
                throw new RuntimeException();
            }

            System.out.println("My move - " + my_choice);
            System.out.println("\t" + getLayoutString(my_ai) + " size = " + my_ai.getLayout().size());
            System.out.println("\tmy bones = " + my_ai.getMyBones() + " , opp_bones = " + opponent_ai.getMyBones() + " boneyard = " + boneyard_bones);

            opponent_choice = makePickupRandom(opponent_ai.getBestChoice());

            if (opponent_choice == null) {
                System.out.println("Opponent desired status: " + my_ai.getState().getDesiredStatus());
                System.out.println("Opponent choice was null!\n" + opponent_ai.getState());
//                System.out.println(my_ai.getBestRoute());
            }

            try {
                opponent_ai.choose(opponent_choice);
            } catch (OutOfMemoryError err) {
                throw new RuntimeException();
            }
            try {
                my_ai.choose(makePickupUnknown(opponent_choice));
            } catch (OutOfMemoryError err) {
                throw new RuntimeException();
            }

            System.out.println("Opponent move - " + opponent_choice);
            System.out.println("\t" + getLayoutString(opponent_ai) + " size = " + opponent_ai.getLayout().size());
            System.out.println("\tmy bones = " + my_ai.getMyBones() + " , opp_bones = " + opponent_ai.getMyBones() + " boneyard = " + boneyard_bones);

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

    private Choice makePickupUnknown(Choice choice) {
        if (choice.getAction() == Choice.Action.PICKED_UP)
            return new Choice(choice.getAction(), null);

        return choice;
    }

    @Test
    public void testChoose() throws Exception {
        my_ai.setInitialState(my_bones, true);

        List<Route> bestRoutes = my_ai.getBestRoutes();
        for (Route route : bestRoutes) {
            System.out.println(route);
        }

        Choice choiceTaken = bestRoutes.get(0).getEarliestChoice();

        System.out.println("\n ========= Chose: " + choiceTaken + "  ========= \n");

        my_ai.choose(choiceTaken);

        bestRoutes = my_ai.getBestRoutes();
        for (Route route : bestRoutes) {
            System.out.println(route);
        }
    }
}
