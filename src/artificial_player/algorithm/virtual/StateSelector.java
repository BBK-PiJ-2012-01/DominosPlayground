package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 00:47
 */
public interface StateSelector {
    /**
     * Returns a mapping of choices to routes where each choice, from the given state, would
     * lead to the associated route (which goes on to the end of the tree).
     *
     * @param state the state to start from.
     * @return a mapping of choices to the routes they link up to.
     */
    Map<Choice, Route> getBestRoutesToChildStates(GameState state);

    /**
     * Reduces (or combines) the given routes into one route.  These routes go from the child
     * state, which are got by starting at the given state and applying the associated choice.
     *
     * @param newEarliestState the state to start from.
     * @param childRoutes the routes from the state's children to the end.
     * @return a combined route (representing the best possible route).
     */
    Route getReducedRoute(GameState newEarliestState, Map<Choice, Route> childRoutes);

    /**
     * Gets all of the best routes to the given state's children, returning them as a list of
     * routes (extended to the given state).  Useful for debugging.
     *
     * @param state the state to start at.
     * @return a sorted (best-first) list of Routes from the given state.
     */
    List<Route> getBestRoutes(GameState state);

    double extraValueFromDiscardedRoutes(Route chosen, Collection<Route> discardedRoutes);
}
