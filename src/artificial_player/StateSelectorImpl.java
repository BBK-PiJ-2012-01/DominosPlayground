package artificial_player;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 21:55
 */
public class StateSelectorImpl implements StateSelector {

    private static final Comparator<Map.Entry<Choice, Route>> mapComparator = new Comparator<Map.Entry<Choice, Route>>() {
        @Override
        public int compare (Map.Entry < Choice, Route > o1, Map.Entry < Choice, Route > o2){
        return Double.compare(
                o1.getValue().getCumulativeValue(),
                o2.getValue().getCumulativeValue()
            );
        }
    };

    private static final Comparator<Route> listComparator = new Comparator<Route>() {
        @Override
        public int compare(Route o1, Route o2) {
            return Double.compare(o1.getCumulativeValue(), o2.getCumulativeValue());
        }
    };

    @Override
    public Route getBestRoute(GameState state) {

        Map<Choice, Route> bestRoutesToChildStates = getBestRoutesToChildStates(state);

        if (bestRoutesToChildStates == null)
            return new Route(state);

        // Otherwise reduce (or combine) those routes
        return getReducedRoute(state, bestRoutesToChildStates);
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

        Map<Choice, Route> bestRoutesToChildStates = new HashMap<Choice, Route>();

        for (Map.Entry<Choice, GameState> e : state.getValidChoices().entrySet()) {
            // For each childState...
            Choice choice = e.getKey();
            GameState childState = e.getValue();

            // .. collect the best route to it.
            bestRoutesToChildStates.put(choice, getBestRoute(childState));
        }

        return bestRoutesToChildStates;
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
        extendRouteToStateThroughChoice(bestRoute, newEarliestState, choiceLeadingToBestRoute);

        // ... but the combined route's value will be some aggregate of the routes,
        // eg. so that a route with lots of good deviations can be preferred over
        // a route with one excellent best route while any deviation is terrible.

        double newValue = bestRoute.getCumulativeValue() + newEarliestState.getValue();

//        for (Route route : routes.values()) {
//            if (route.getCumulativeValue() > bestRoute.getCumulativeValue() - 2)
//                newValue += 1;
//        }

        bestRoute.setCumulativeValue(newValue);

        // PS. I've made Route mutable and have reused instead of created a new Route
        // because these Routes are used on the way up the decision tree - ie. they are
        // never used again.  Is this a case of premature optimisation??

        return bestRoute;
    }

    private void extendRouteToStateThroughChoice(Route route, GameState state, Choice choice) {
        route.setEarliestChoice(choice);
        route.setEarliestState(state);
    }

    @Override
    public List<Route> getBestRoutes(GameState state) {
        Map<Choice, Route> bestRoutesToChildStates = getBestRoutesToChildStates(state);

        List<Route> routes = new LinkedList<Route>();

        if (bestRoutesToChildStates == null) {
            routes.add(new Route(state));
            return routes;
        }

        for (Map.Entry<Choice, Route> e : bestRoutesToChildStates.entrySet()) {
            extendRouteToStateThroughChoice(e.getValue(), state, e.getKey());
        }

        routes.addAll(bestRoutesToChildStates.values());
        Collections.sort(routes, listComparator);

        if (state.isMyTurn())
            Collections.reverse(routes);

        return routes;
    }

}
