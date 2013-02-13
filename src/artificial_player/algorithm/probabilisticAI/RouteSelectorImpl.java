package artificial_player.algorithm.probabilisticAI;

import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.AbstractRouteSelector;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 21:55
 */
public class RouteSelectorImpl extends AbstractRouteSelector {

    @Override
    public double extraValueFromDiscardedRoutes(Route chosen, List<Route> discardedRoutes, boolean isMyTurn) {
        double extraValue = 0;

//        for (Route route : discardedRoutes) {
//            if (Math.abs(route.getValue() - chosen.getValue()) < 4)
//                extraValue += 3;
//        }

//        for (int i = 0; i < discardedRoutes.size() / 2 && i < 3; ++i) {
//            if (!isMyTurn) extraValue += discardedRoutes.get(i).getValue() / (i+1);
//        }

        if (!isMyTurn) {
            int n = 0;
            for (Route route : discardedRoutes) {
//                extraValue += chosen.getValue() / (1 + 0.5 * Math.abs(route.getValue() - chosen.getValue()));
                if (route.getValue() < 0) {
                    n += 1;
                    extraValue += route.getValue() / n;
                }
            }
//            if (n > 0)
//                extraValue /= n;
        }

        return extraValue;
    }

}
