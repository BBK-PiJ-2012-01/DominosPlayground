package artificial_player;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 02/02/2013
 * Time: 13:36
 */
public class GameStateTest {
    private GameState my_state;
    private GameState opponent_state;
    private Set<Bone2> my_bones;
    private Set<Bone2> opponent_bones;

    @Before
    public void setUp() throws Exception {
        my_bones = new HashSet<Bone2>();

//        my_bones.add(new Bone2(0, 0, true));
//        my_bones.add(new Bone2(0, 1, true));
//        my_bones.add(new Bone2(1, 1, true));
//        my_bones.add(new Bone2(2, 1, true));
//        my_bones.add(new Bone2(2, 2, true));
//        my_bones.add(new Bone2(2, 3, true));
//        my_bones.add(new Bone2(3, 3, true));

        List<Bone2> all_bones = new LinkedList<Bone2>(GameState.getAllBones());
        Collections.shuffle(all_bones);
        my_bones.addAll(all_bones.subList(0, 7));

//        AIContainer ai = new DefaultAIContainer();
//        my_state = new GameState(ai, my_bones, true);
//
//        all_bones = new LinkedList<Bone2>(GameState.getAllBones());
//        all_bones.removeAll(my_bones);
//        Collections.shuffle(all_bones);
//        opponent_bones = new HashSet<Bone2>();
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
