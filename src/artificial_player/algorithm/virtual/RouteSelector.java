package artificial_player.algorithm.virtual;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Route;

import java.util.List;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 00:47
 */
public interface RouteSelector {
    /**
     * Gets the route from the given state to the best final state.
     *
     * @param state the state the route starts from.
     * @return the route from the given state to the best final state.
     */
    Route getBestRoute(GameState state);

    /**
     * Gets the list of best routes from the given state to the best final state,
     * ordered from best to worst.  Each route corresponds to the best route for
     * a choice in state.getValidChoices().
     *
     * @param state the state the routes starts from.
     * @return the route from the given state to the best final state.
     */
    List<Route> getBestRoutes(GameState state);
}
