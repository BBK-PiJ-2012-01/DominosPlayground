package artificial_player.algorithm.components;

import artificial_player.MockGameState;
import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.Route;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 19:55
 */
public class AbstractRouteSelectorTest {
    private AbstractRouteSelector routeSelector;
    private MockGameState parent, child1, child2, grandChild1a, grandChild1b, grandChild2a, grandChild2b;


    @Before
    public void setUp() throws Exception {
        routeSelector = new AbstractRouteSelector() {
            @Override
            public double extraValueFromDiscardedRoutes(Route chosen, List<Route> discardedRoutes, boolean isMyTurn) {
                return 0;
            }
        };

        parent = MockGameState.createRoot();

        child1 = new MockGameState(parent, 10);
        child2 = new MockGameState(parent, 20);

        // child1 represents picking up a bone
        child1.setChoiceTaken(new Choice(Choice.Action.PASS, null));

        grandChild1a = new MockGameState(child1, 9);
        grandChild1b = new MockGameState(child1, 11);

        grandChild2a = new MockGameState(child2, 19);
        grandChild2b = new MockGameState(child2, 21);
    }

    private void setFirstChoiceAsMine() {
        setTurnOwnership(parent, true);
    }

    private void setFirstChoiceAsOpponents() {
        setTurnOwnership(parent, false);
    }

    private void setTurnOwnership(MockGameState gameState, boolean isTurnMine) {
        gameState.setMyTurn(isTurnMine);
        for (GameState childState : gameState.getChildStates()) {
            setTurnOwnership((MockGameState) childState, !isTurnMine);
        }
    }

    @Test
    public void testGetBestRouteWhenMyTurn() throws Exception {
        setFirstChoiceAsMine();
        Route bestRoute = routeSelector.getBestRoute(parent);

        // First choice is mine, should go to maximum value (so parent -> child2)
        // Second choice is therefore opponent's, so should go to minimum value (ie. child2 -> grandChild2a)
        assertEquals(Arrays.asList(parent, child2, grandChild2a), bestRoute.getAllStates());
    }

    @Test
    public void testGetBestRouteWhenOpponentTurn() throws Exception {
        setFirstChoiceAsOpponents();
        Route bestRoute = routeSelector.getBestRoute(parent);

        // First choice is opponent's, should go to minimum value (so parent -> child1)
        // Second choice is therefore mine, so should go to maximum value (ie. child1 -> grandChild1b)
        assertEquals(Arrays.asList(parent, child1, grandChild1b), bestRoute.getAllStates());
    }

    @Test
    public void testGetAllBestRoutesWhenMyTurn() throws Exception {
        setFirstChoiceAsMine();
        List<Route> bestRoutes = routeSelector.getBestRoutes(parent);

        // Will get a best route for each child.
        assertEquals(2, bestRoutes.size());

        // The first move is mine, so best is parent -> child2 -> grandChild2a
        assertEquals(Arrays.asList(parent, child2, grandChild2a), bestRoutes.get(0).getAllStates());

        // and second best comes from going via child1, after which the opponent chooses
        // the worst thing for me (ie. grandChild1a)
        assertEquals(Arrays.asList(parent, child1, grandChild1a), bestRoutes.get(1).getAllStates());
    }

    @Test
    public void testGetAllBestRoutesWhenOpponentTurn() throws Exception {
        setFirstChoiceAsOpponents();
        List<Route> bestRoutes = routeSelector.getBestRoutes(parent);

        // Will get a best route for each child.
        assertEquals(2, bestRoutes.size());

        // The first move is opponent's, so best is parent -> child1 -> grandChild1b
        assertEquals(Arrays.asList(parent, child1, grandChild1b), bestRoutes.get(0).getAllStates());

        // and second best comes from going via child2, after which I would choose
        // the best thing for me (ie. grandChild2b)
        assertEquals(Arrays.asList(parent, child2, grandChild2b), bestRoutes.get(1).getAllStates());
    }
}
