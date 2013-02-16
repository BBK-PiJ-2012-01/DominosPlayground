package artificial_player.algorithm.components;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.Route;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 15/02/2013
 * Time: 17:57
 */
public class RouteSelectorBinary implements RouteSelector {

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
        List<Route> bestRoutes = new LinkedList<Route>();

        for (GameState childState : state.getChildStates()) {
            // Skip pickup child states
            Choice choiceTaken = childState.getChoiceTaken();
            if (choiceTaken != null && choiceTaken.getAction() == Choice.Action.PICKED_UP)
                continue;

            bestRoutes.add(getBestRoute(childState));
        }

        Collections.sort(bestRoutes, routeValueComparator);
        if (state.isMyTurn())
            Collections.reverse(bestRoutes);

        return bestRoutes;
    }

    public Route getBestRoute(GameState state) {
        Route bestRoute = null;
        int n = 0;
        int sumOfOpponentValues = 0;

        for (GameState childState : state.getChildStates()) {
            Route bestRouteFromChild = getBestRoute(childState);

            if (bestRoute == null)
                bestRoute = bestRouteFromChild;
            else if (state.isMyTurn() && bestRouteFromChild.getValue() > bestRoute.getValue())
                bestRoute = bestRouteFromChild;
            else if (!state.isMyTurn()) {
                if (bestRouteFromChild.getValue() < bestRoute.getValue())
                    bestRoute = bestRouteFromChild;
                n += 1;
                sumOfOpponentValues += bestRouteFromChild.getValue();
            }
        }

        double extraValue = (n == 0 ? 0 : sumOfOpponentValues / n / 3);

        if (bestRoute == null)
            bestRoute = new Route(state);

        bestRoute.extendBackward();
        bestRoute.increaseValue(extraValue);
        return bestRoute;
    }
}
