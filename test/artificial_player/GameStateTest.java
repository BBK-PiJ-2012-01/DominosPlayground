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
    GameState state;
    Set<Bone2> my_bones;

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

        List<Bone2> all_bones = new LinkedList<Bone2>(GameState.all_bones);
        Collections.shuffle(all_bones);
        my_bones.addAll(all_bones.subList(0, 7));
        for (Bone2 bone : my_bones) {
            bone.setMine(true);
        }

        state = new GameState(my_bones, true);
    }

    @Test
    public void test1() throws Exception {
        state.printBestN(1);
    }

    @Test
    public void test2() throws Exception {
        state.printBestN(2);
    }

    @Test
    public void test3() throws Exception {
        state.printBestN(5);
    }
}
