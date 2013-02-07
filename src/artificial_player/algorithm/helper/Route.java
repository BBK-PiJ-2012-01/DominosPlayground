package artificial_player.algorithm.helper;

import artificial_player.algorithm.GameState;

import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 22:20
 */
public class Route {
    private final GameState finalState;
    private final GameState earliestState;
    private final Choice earliestChoice;
    private final int size;

    private double value;

    public Route(GameState finalState) {
        this.finalState = finalState;
        value = finalState.getValue();
        earliestState = finalState;
        earliestChoice = null;
        size = 0;
    }

    public Route(GameState state, Choice choice, Route routeToExtendBackward) {
        finalState = routeToExtendBackward.getFinalState();
        value = routeToExtendBackward.getValue();
        earliestChoice = choice;
        earliestState = state;
        size = routeToExtendBackward.size() + 1;
    }

    public int size() {
        return size;
    }

    public Choice getEarliestChoice() {
        return earliestChoice;
    }

    public GameState getFinalState() {
        return finalState;
    }

    public double getValue() {
        return value;
    }

    public void increaseValue(double cumulativeValue) {
        this.value = cumulativeValue;
    }

    public String toString() {
        String header = String.format("%n--- Choices (value = %.1f -> %.1f, route value = %.1f) ----%n",
                earliestState.getValue(), finalState.getValue(), value);

        StringBuilder sbuilder = new StringBuilder(header);

        for (GameState nextState : getAllStates()) {
            sbuilder.append(nextState.toString());
            sbuilder.append("\n");
        }

        return sbuilder.toString();
    }

    public List<GameState> getAllStates() {
        LinkedList<GameState> stack = new LinkedList<GameState>();

        GameState state = finalState;

        do {
            stack.addFirst(state);
            state = state.getPrevious();
        } while (state != earliestState);

        return stack;
    }
}
