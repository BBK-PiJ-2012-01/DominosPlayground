package artificial_player.algorithm.randomAI;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.GameStateImpl;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.RouteSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 11/02/2013
 * Time: 17:11
 */
public class SimpleRouteSelector implements RouteSelector {

    @Override
    public List<Route> getBestRoutes(GameState state, boolean excludePickup) {
        List<GameState> childStates = state.getChildStates();
        List<Route> bestRoutes = new ArrayList<Route>(childStates.size());

        // If the state "desires" to be final (a LEAF or NOT_YET_CALCULATED) or "desires"
        // to have children but doesn't (short-circuited to avoid unnecessarily initialising
        // state.validChoices) ...
        if (state.getStatus() != GameStateImpl.Status.HAS_CHILD_STATES) {
            // ... then define the route as ending here:
            return bestRoutes;
        }

        // So now the state MUST have child states.

        // TODO: if there's only one childState, just pick it... but that won't work nicely if called by getBestRoute...

//        System.out.format("level %d has %d children%n", state.depth(), childStates.size());

        for (GameState childState : childStates) {
            // If excludePickup, then skip if this childState is a pick-up
            if (excludePickup) {
                Choice choiceTaken = childState.getChoiceTaken();
                if (choiceTaken != null && choiceTaken.getAction() == Choice.Action.PICKED_UP) {
                    continue;
                }
            }

            // Get the best route to each childState (from the final state)
            Route bestRoute = new Route(childState);

            // But extend the route from childState to state.
            bestRoute.extendBackward();

            bestRoutes.add(bestRoute);
        }

        // Return the reduced version of these routes
        return bestRoutes;
    }
}
