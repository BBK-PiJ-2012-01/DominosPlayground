package artificial_player.algorithm.helper;

import artificial_player.MockGameState;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 18:03
 */
public class RouteTest {
    private Route route;
    private MockGameState parent;
    private MockGameState child;
    private final int childValue = 150;

    @Before
    public void setUp() throws Exception {
        parent = MockGameState.createRoot();
        child = new MockGameState(parent, childValue);
        route = new Route(child);
    }

    @Test
    public void testExtendBackward() throws Exception {
        route.extendBackward();
    }

    @Test
    public void testGetEarliestChoice() throws Exception {
        assertEquals(null, route.getEarliestChoice());
    }

    @Test
    public void testGetFinalState() throws Exception {
        assertEquals(child, route.getFinalState());
    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals(child.getValue(), route.getValue());
    }

    @Test
    public void testGetAllStates() throws Exception {
        assertEquals(Arrays.asList(child), route.getAllStates());
    }

    @Test
    public void testLength() throws Exception {
        assertEquals(1, route.length());
    }

    @Test
    public void testGetEarliestChoiceAfterExtended() throws Exception {
        route.extendBackward();
        assertEquals(child.getChoiceTaken(), route.getEarliestChoice());
    }

    @Test
    public void testGetFinalStateAfterExtended() throws Exception {
        route.extendBackward();
        assertEquals(child, route.getFinalState());
    }

    @Test
    public void testGetValueAfterExtended() throws Exception {
        route.extendBackward();
        assertEquals(child.getValue(), route.getValue());
    }

    @Test
    public void testGetAllStatesAfterExtended() throws Exception {
        route.extendBackward();
        assertEquals(Arrays.asList(parent, child), route.getAllStates());
    }

    @Test
    public void testLengthAfterExtended() throws Exception {
        route.extendBackward();
        assertEquals(2, route.length());
    }

    @Test
    public void testIncreaseValue() throws Exception {
        route.increaseValue(75);
        assertEquals(child.getValue() + 75, route.getValue());
    }

    @Test
    public void testToString() throws Exception {
        // Just test that it doesn't throw an exception, as it's quite a big string.
        route.toString();
        route.extendBackward();
        route.toString();
    }
}
