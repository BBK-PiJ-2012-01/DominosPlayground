package artificial_player;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

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

        my_bones.add(new Bone2(0, 0, true));
        my_bones.add(new Bone2(0, 1, true));
        my_bones.add(new Bone2(1, 1, true));
        my_bones.add(new Bone2(2, 1, true));
        my_bones.add(new Bone2(2, 2, true));
        my_bones.add(new Bone2(2, 3, true));
        my_bones.add(new Bone2(3, 3, true));

        state = new GameState(my_bones, true);
    }

    @Test
    public void testGetBestChoice() throws Exception {
        System.out.println(state.getBestChoice());
        System.out.println(state);
    }

    @Test
    public void testGetHeuristic() throws Exception {

    }

    @Test
    public void testChoose() throws Exception {

    }

    @Test
    public void testGetValue() throws Exception {

    }
}
