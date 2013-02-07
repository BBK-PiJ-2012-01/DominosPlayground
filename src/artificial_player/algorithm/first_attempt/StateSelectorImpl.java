package artificial_player.algorithm.first_attempt;

import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.AbstractStateSelector;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 21:55
 */
public class StateSelectorImpl extends AbstractStateSelector {


    @Override
    public double extraValueFromDiscardedRoutes(Route chosen, Collection<Route> discardedRoutes) {
        double extraValue = 0;

        for (Route route : discardedRoutes) {
            if (route.getValue() > chosen.getValue() - 2)
                extraValue += 1;
        }

        return extraValue;
    }

}
