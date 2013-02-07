package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.first_attempt.ExpectationWeightEvaluator;
import artificial_player.algorithm.first_attempt.LinearPlyManager;
import artificial_player.algorithm.first_attempt.StateEnumeratorImpl;
import artificial_player.algorithm.first_attempt.StateSelectorImpl;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.Route;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 14:22
 */
public class AIControllerTest {
    private AIController ai;
    private Set<CopiedBone> my_bones;

    @Before
    public void setUp() throws Exception {
        ai = new AIController(
                new LinearPlyManager(),
                new StateSelectorImpl(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator()
        );

        my_bones = new HashSet<CopiedBone>();
        List<CopiedBone> all_bones = new LinkedList<CopiedBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);
        my_bones.addAll(all_bones.subList(0, 7));
    }

    @Test
    public void testSetInitialState() throws Exception {
        ai.setInitialState(my_bones, true);
    }

    @Test
    public void testGetBestRoutes() throws Exception {
        ai.setInitialState(my_bones, true);

        for (Route route : ai.getBestRoutes()) {
            System.out.println(route);
        }
    }

    @Test
    public void testChoose() throws Exception {
        ai.setInitialState(my_bones, true);

        List<Route> bestRoutes = ai.getBestRoutes();
        for (Route route : bestRoutes) {
            System.out.println(route);
        }

        Choice choiceTaken = bestRoutes.get(0).getEarliestChoice();

        System.out.println("\n ========= Chose: " + choiceTaken + "  ========= \n");

        ai.choose(choiceTaken);

        bestRoutes = ai.getBestRoutes();
        for (Route route : bestRoutes) {
            System.out.println(route);
        }
    }
}
