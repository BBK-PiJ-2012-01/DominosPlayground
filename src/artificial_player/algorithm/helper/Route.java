package artificial_player.algorithm.helper;

import artificial_player.algorithm.GameState;

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

    public Route(GameState finalState) {
        this.finalState = finalState;
        value = finalState.getValue();
        earliestState = finalState;
        earliestChoice = null;
        length = 1;
    }

    public void extendBackward() {
        earliestChoice = earliestState.getChoiceTaken();
        earliestState = earliestState.getParent();
        length += 1;
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
        this.value += cumulativeValue;
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
            state = state.getParent();
        } while (state != earliestState);

        return stack;
    }

    public int length() {
        return length;
    }
}
