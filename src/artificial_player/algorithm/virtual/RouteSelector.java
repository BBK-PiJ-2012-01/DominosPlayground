package artificial_player.algorithm.virtual;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Route;

import java.util.List;

/**
 * Class to calculate the best routes to take from a GameState.
 */
public interface RouteSelector {

    /**
     * Gets the list of best routes from the given state to the best final state,
     * ordered from best to worst.  Each route corresponds to the best route for
     * a choice in state.getValidChoices().  If 'excludePickup' is true, routes starting
     * with a pickup will be ignored.
     *
     * @param state the state the routes starts from.
     * @param excludePickup only return routes which do NOT start with a pick up.
     * @return the route from the given state to the best final state.
     */
    List<Route> getBestRoutes(GameState state, boolean excludePickup);
}
