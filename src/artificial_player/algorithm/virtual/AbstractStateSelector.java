package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Route;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:19
 */
public abstract class AbstractStateSelector implements StateSelector {
    private static final Comparator<Map.Entry<Choice, Route>> mapComparator = new Comparator<Map.Entry<Choice, Route>>() {
        @Override
        public int compare (Map.Entry < Choice, Route > o1, Map.Entry < Choice, Route > o2){
            return compareRoutes(o1.getValue(), o2.getValue());
        }
    };
    private static final Comparator<Route> listComparator = new Comparator<Route>() {
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
    public Map<Choice, Route> getBestRoutesToChildStates(GameState state) {
        // If the state "desires" to be final (a LEAF or NOT_YET_CALCULATED) or "desires"
        // to have children but doesn't (short-circuited to avoid unnecessarily initialising
        // state.validChoices) ...
        if (state.getDesiredStatus() != GameState.Status.HAS_CHILD_STATES
                || state.getValidChoices().isEmpty())
            // ... then define the route as ending here:
            return null;


        // So now the state MUST have child states.

        Map<Choice, Route> bestRoutes = new HashMap<Choice, Route>();
        Map<Choice, Route> bestRoutesToChildStates;
        Route bestRouteToChild;

        for (Map.Entry<Choice, GameState> e : state.getValidChoices().entrySet()) {
            // For each childState...
            Choice choice = e.getKey();
            GameState childState = e.getValue();

            // .. get the best routes to this childState. (This is the recursive line)
            bestRoutesToChildStates = getBestRoutesToChildStates(childState);


            if (bestRoutesToChildStates == null)
                // If there are no grandchildren, this is the start of the route
                bestRouteToChild = new Route(state);
            else
                // If there's one or more, use a reduced/combined route of them all
                bestRouteToChild = getReducedRoute(childState, bestRoutesToChildStates);

            // and store the best route to this child.
            bestRoutes.put(choice, bestRouteToChild);
        }

        return bestRoutes;
    }

    @Override
    public Route getReducedRoute(GameState newEarliestState, Map<Choice, Route> childRoutes) {
        Map.Entry<Choice,Route> bestChoiceAndRoute;
        if (newEarliestState.isMyTurn())
            bestChoiceAndRoute = Collections.max(childRoutes.entrySet(), mapComparator);
        else
            bestChoiceAndRoute = Collections.min(childRoutes.entrySet(), mapComparator);

        Choice choiceLeadingToBestRoute = bestChoiceAndRoute.getKey();
        Route bestRoute = bestChoiceAndRoute.getValue();

        // The combined route will still lead to the best final state...
        Route extendedBestRoute = new Route(newEarliestState, choiceLeadingToBestRoute, bestRoute);

        // ... but the combined route's value will be some aggregate of the routes,
        // eg. so that a route with lots of good deviations can be preferred over
        // a route with one excellent best route but any deviation is terrible.

        Collection<Route> discardedRoutes = childRoutes.values();
        discardedRoutes.remove(bestRoute);
        double extraValue = extraValueFromDiscardedRoutes(bestRoute, discardedRoutes);
        extendedBestRoute.increaseValue(extraValue);

        return extendedBestRoute;
    }

    @Override
    public abstract double extraValueFromDiscardedRoutes(Route chosen, Collection<Route> discardedRoutes);

    @Override
    public List<Route> getBestRoutes(GameState state) {
        Map<Choice, Route> bestRoutesToChildStates = getBestRoutesToChildStates(state);

        List<Route> routes = new LinkedList<Route>();

        if (bestRoutesToChildStates == null) {
            routes.add(new Route(state));
            return routes;
        }

        for (Map.Entry<Choice, Route> e : bestRoutesToChildStates.entrySet()) {
            Choice choice = e.getKey();
            Route routeToExtendBackward = e.getValue();

            routes.add(new Route(state, choice, routeToExtendBackward));
        }

        Collections.sort(routes, listComparator);

        if (state.isMyTurn())
            Collections.reverse(routes);

        return routes;
    }
}
