package artificial_player;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.CopiedBone;
import org.junit.Before;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 02/02/2013
 * Time: 13:36
 */
public class GameStateTest {
    private GameState my_state;
    private GameState opponent_state;
    private Set<CopiedBone> my_bones;
    private Set<CopiedBone> opponent_bones;

    @Before
    public void setUp() throws Exception {
        my_bones = new HashSet<CopiedBone>();

//        my_bones.add(new CopiedBone(0, 0, true));
//        my_bones.add(new CopiedBone(0, 1, true));
//        my_bones.add(new CopiedBone(1, 1, true));
//        my_bones.add(new CopiedBone(2, 1, true));
//        my_bones.add(new CopiedBone(2, 2, true));
//        my_bones.add(new CopiedBone(2, 3, true));
//        my_bones.add(new CopiedBone(3, 3, true));

        List<CopiedBone> all_bones = new LinkedList<CopiedBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);
        my_bones.addAll(all_bones.subList(0, 7));

//        AIContainer ai = new DefaultAIContainer();
//        my_state = new GameState(ai, my_bones, true);
//
//        all_bones = new LinkedList<CopiedBone>(GameState.getAllBones());
//        all_bones.removeAll(my_bones);
//        Collections.shuffle(all_bones);
//        opponent_bones = new HashSet<CopiedBone>();
//
//        AIContainer ai_opponent = new DefaultAIContainer();
//        opponent_state = new GameState(ai_opponent, opponent_bones, false);
    }

//    @Test
//    public void test1() throws Exception {
//        my_state.printBestN(1);
//    }
//
//    @Test
//    public void test2() throws Exception {
//        my_state.printBestN(2);
//    }
//
//    @Test
//    public void test3() throws Exception {
//        my_state.printBestN(50);
//    }

//    @Test
//    public void testWithExtraPly() throws Exception {
//        my_state.printBestAfterSelectivelyIncreasingPly(1000);
//    }

//    @Test
//    public void testOpponent() throws Exception {
//        Choice best_choice = my_state.getBestChoice();
//        opponent_state.choose(best_choice);
//        opponent_state.printBestN(50);
//    }
}
