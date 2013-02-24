package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.GameState;
import dominoes.players.ai.algorithm.GameStateImpl;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.Route;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:19
 */
public abstract class AbstractRouteSelector implements RouteSelector {

    private static final Comparator<Route> routeValueComparator = new Comparator<Route>() {
        @Override
        public int compare(Route o1, Route o2) {
            return compareRoutes(o1, o2);
        }
    };

    private static int compareRoutes(Route o1, Route o2) {
        int route_comparison = Double.compare(
                o1.getValue(),
                o2.getValue()
        );

        if (route_comparison == 0)
            return Double.compare(
                    o1.getFinalState().getValue(),
                    o1.getFinalState().getValue()
            );
        else
            return route_comparison;
    }

    @Override
    public List<Route> getBestRoutes(GameState state) {
        List<Route> bestRoutes = getBestRoutesInternal(state, true);

        // Sort the routes by "best" (according to who's turn it is):

        // Sort ascending (ie. lowest value first = order if it's the opponent's turn)
        Collections.sort(bestRoutes, routeValueComparator);

        // If my turn, reverse it so the best (highest-valued) route comes first.
        if (state.isMyTurn())
            Collections.reverse(bestRoutes);

        return bestRoutes;

    }

    /**
     * This does the same job as getBestRoutes, with two differences:
     *  1. It allows for pickup choices to not be followed (useful for the first level of recursion,
     *      ie. in the next immediate choice, there's no need to distinguish between picking up bone A
     *      and picking up bone B because all I can do is pick SOMETHING up - compared to further down
     *      into the recursion where I want to know how damaging it would be to have to pick up something
     *      (in which case I would need to look individually at picking up bones A, B, etc...).
     *  2. It doesn't sort the returned list of routes (since this is not needed internally - only getBestRoutes
     *      needs to do that).
     *
     * @param state the state the routes start from.
     * @param excludePickup if true, ignores routes starting with a pickup.
     * @return the route from the given state to the best final state.
     */
    public List<Route> getBestRoutesInternal(GameState state, boolean excludePickup) {
        // If the state "desires" to be final (a LEAF or NOT_YET_CALCULATED) or "desires"
        // to have children but doesn't (short-circuited to avoid unnecessarily initialising
        // state.validChoices) ...
        if (state.getStatus() != GameStateImpl.Status.HAS_CHILD_STATES)
            // ... then define the route as ending here:
            return Collections.emptyList();

        List<GameState> childStates = state.getChildStates();
//        List<Route> bestRoutes = new ArrayList<Route>(childStates.size());
        List<Route> bestRoutes = new LinkedList<Route>();

        // So now the state MUST have child states.


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
            Route bestRoute = getBestRoute(childState);

            // But extend the route from childState to state.
            bestRoute.extendBackward();

            bestRoutes.add(bestRoute);
        }

        // Return the reduced version of these routes
        return bestRoutes;
    }

    /**
     * Gets the route from the given state to the best final state.
     *
     * NB. This calls getBestRoutes with 'excludePickup == false'.
     *
     * @param state the state the route starts from.
     * @return the route from the given state to the best final state.
     */
    public Route getBestRoute(GameState state) {
        List<Route> bestRoutes = getBestRoutesInternal(state, false);

        if (bestRoutes.isEmpty())
            // If the given state is a leaf, create the route from here
            return new Route(state);
        else
            // Else choose the best of the best routes
            return getReducedRoute(bestRoutes, state.isMyTurn());
    }

    public Route getReducedRoute(List<Route> routes, boolean isMyTurn) {
        Route bestRoute;

        // Get the best route (according to whose turn it is) ...
        if (isMyTurn)
            bestRoute = Collections.max(routes, routeValueComparator);
        else
            bestRoute = Collections.min(routes, routeValueComparator);

        routes.remove(bestRoute);

        // ... and the combined route's value will be increased by some aggregate of the routes,
        // eg. so that a route with lots of good deviations can be preferred over
        // a route with one excellent best route but any deviation is terrible.

        double extraValue = extraValueFromDiscardedRoutes(bestRoute, routes, isMyTurn);
        bestRoute.increaseValue(extraValue);

        return bestRoute;
    }

    public abstract double extraValueFromDiscardedRoutes(Route chosen, List<Route> discardedRoutes, boolean isMyTurn);
}
