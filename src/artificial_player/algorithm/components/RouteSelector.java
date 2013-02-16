package artificial_player.algorithm.components;

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
     * a choice in state.getValidChoices().  Routes starting with a pickup will
     * be ignored, since internally all possible pickups are enumerated (eg. pickup
     * bone A, pickup bone B, ...) but it doesn't make sense to choose one of these
     * as a route to follow since all we can do is pickup randomly from the boneyard.
     *
     * If a placement is possible, this will return a list for the best routes for
     * each legal placement.  If only a pickup is possible (ie. can't place but the
     * boneyard isn't empty) the retured list will be empty.  If the only option is to
     * pass, the list will return one route leading to the GameState a pass leads to.
     *
     * @param state the state the routes starts from.
     * @return the routes from the given state to the best final states.
     */
    List<Route> getBestRoutes(GameState state);
}
