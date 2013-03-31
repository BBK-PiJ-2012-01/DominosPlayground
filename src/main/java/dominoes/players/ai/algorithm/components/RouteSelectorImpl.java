package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.Route;

import java.util.List;

/**
 * An implementation of AbstractRouteSelector which devalues routes which
 * have other bad routes the opponent could take.
 *
 * @author Sam Wright
 */
public class RouteSelectorImpl extends AbstractRouteSelector {

    @Override
    public double extraValueFromDiscardedRoutes(Route chosen, List<Route> discardedRoutes, boolean isMyTurn) {
        double extraValue = 0;

        if (!isMyTurn) {
            int n = 0;
            for (Route route : discardedRoutes) {
//                extraValue += chosen.getValue() / (1 + 0.5 * Math.abs(route.getValue() - chosen.getValue()));
                if (route.getValue() < 0) {
                    n += 1;
                    extraValue += route.getValue();// / n;
                }
            }
            if (n > 0)
                extraValue /= n;
        }

        return extraValue;
//        return 0;
    }

}
