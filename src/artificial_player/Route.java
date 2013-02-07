package artificial_player;

import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 22:20
 */
public class Route {
    private final GameState finalState;
    private GameState earliestState;
    private double cumulativeValue;
    private Choice earliestChoice;

    public Route(GameState finalState) {
        this.finalState = finalState;
        cumulativeValue = finalState.getValue();
        earliestState = finalState;
    }

    public Choice getEarliestChoice() {
        return earliestChoice;
    }

    public void setEarliestChoice(Choice earliestChoice) {
        this.earliestChoice = earliestChoice;
    }

    public GameState getFinalState() {
        return finalState;
    }

    public double getCumulativeValue() {
        return cumulativeValue;
    }

    public void setCumulativeValue(double cumulativeValue) {
        this.cumulativeValue = cumulativeValue;
    }

    public GameState getEarliestState() {
        return earliestState;
    }

    public void setEarliestState(GameState earliestState) {
        this.earliestState = earliestState;
    }

    public String toString() {
        String header = String.format("%n--- Choices (value = %.1f -> %.1f, route value = %.1f) ----%n",
                earliestState.getValue(), finalState.getValue(), cumulativeValue);

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
