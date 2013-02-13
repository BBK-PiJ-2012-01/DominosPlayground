package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.probabilisticAI.StateEnumeratorImpl;
import artificial_player.algorithm.virtual.MockHandEvaluator;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 22:54
 */
public class GameStateImplTest {
    private GameState topState;
    private List<ImmutableBone> myBones;
    private final int initialPly = 4;

    @Before
    public void setUp() throws Exception {
        myBones = new LinkedList<ImmutableBone>();
        myBones.add(new ImmutableBone(0, 1));
        myBones.add(new ImmutableBone(0, 2));
        myBones.add(new ImmutableBone(2, 3));
        myBones.add(new ImmutableBone(2, 4));
        myBones.add(new ImmutableBone(4, 5));
        myBones.add(new ImmutableBone(4, 6));

        topState = new GameStateImpl(
                        new StateEnumeratorImpl(),
                        new MockHandEvaluator(),
                        initialPly,
                        myBones,
                        true
                    );
    }

    @Test
    public void testGetStatus() throws Exception {
        GameState state = topState;
        int ply = initialPly;
        int depth = 0;

        while(!state.getChildStates().isEmpty()) {
            assertEquals(GameState.Status.HAS_CHILD_STATES, state.getStatus());

            state = state.getChildStates().get(0);
            depth += 1;
            System.out.println("Depth: " + depth);


            if (depth == ply) {
                assertEquals(GameState.Status.NOT_YET_CALCULATED, state.getStatus());
                state.increasePly(2);
                ply += 1;
                System.out.println("Ply increased to : " + ply);
            }
        }

        assertEquals(GameState.Status.GAME_OVER, state.getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void testChooseBadChoice() {
        // Can't pass on first go, because I am able to place.
        topState.choose(new Choice(Choice.Action.PASS, null));
    }

    // TODO: write these tests (ughh...)

    @Test
    public void testGetChildStates() throws Exception {

    }

    @Test
    public void testChoose() throws Exception {

    }

    @Test
    public void testGetPossibleOpponentBones() throws Exception {

    }

    @Test
    public void testGetChoiceTaken() throws Exception {

    }

    @Test
    public void testIsMyTurn() throws Exception {

    }

    @Test
    public void testGetMyBones() throws Exception {

    }

    @Test
    public void testGetParent() throws Exception {

    }

    @Test
    public void testIncreasePly() throws Exception {

    }

    @Test
    public void testGetValue() throws Exception {

    }

    @Test
    public void testGetSizeOfOpponentHand() throws Exception {

    }

    @Test
    public void testGetSizeOfBoneyard() throws Exception {

    }

    @Test
    public void testGetLayoutRight() throws Exception {

    }

    @Test
    public void testGetLayoutLeft() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }
}
