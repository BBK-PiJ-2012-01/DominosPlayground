package dominoes.players.ai.algorithm.helper;

import dominoes.players.ai.algorithm.GameState;

import java.util.LinkedList;
import java.util.List;

/**
 * A LinkedList of GameStates, first defined at the final state then progressively
 * extended backward to the current state (with value being added along the way).
 */
public class Route {
    private final GameState finalState;

    private GameState earliestState;
    private Choice earliestChoice;
    private double value;
    private int length;

    /**
     * Create a route that ends with the given GameState.
     *
     * @param finalState the last GameState in the route.
     */
    public Route(GameState finalState) {
        this.finalState = finalState;
        value = finalState.getValue();
        earliestState = finalState;
        earliestChoice = null;
        length = 1;
    }

    /**
     * Extend the route backward (ie. prepend earliestState().getParent() to the route).
     *
     * NB. This doesn't affect the route's value, which is determined by the final state
     * and calls to increaseValue.
     */
    public void extendBackward() {
        earliestChoice = earliestState.getChoiceTaken();
        earliestState = earliestState.getParent();
        length += 1;
    }

    /**
     * Gets the choice to go from the first GameState to the second GameState.
     *
     * NB. If there is only one GameState in the route, this returns null.
     *
     * @return the choice to go from the first GameState to the second GameState.
     */
    public Choice getEarliestChoice() {
        return earliestChoice;
    }

    /**
     * Gets the final GameState in the route.
     *
     * @return the final GameState in the route.
     */
    public GameState getFinalState() {
        return finalState;
    }

    /**
     * Gets the value of this route.
     *
     * Initially this equals getFinalState().getValue(), but is added to using increaseValue().
     *
     * @return the value of this route.
     */
    public double getValue() {
        return value;
    }

    /**
     * Increases the value of this route.
     *
     * NB. this is intended to add value to this route based on factors other than the final
     * GameState's value (eg. in combining several potential routes into a best route).
     *
     * @param extraValue the value to add to the route.
     */
    public void increaseValue(double extraValue) {
        this.value += extraValue;
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

    /**
     * Returns a standard java.util.List view of this route (starting with the earliest state).
     *
     * @return a standard java.util.List view of this route (starting with the earliest state).
     */
    public List<GameState> getAllStates() {
        LinkedList<GameState> stack = new LinkedList<GameState>();

        GameState state = finalState;

        do {
            stack.addFirst(state);

            if (state == earliestState)
                break;

            state = state.getParent();

        } while (state != null);

        return stack;
    }

    public int length() {
        return length;
    }
}
