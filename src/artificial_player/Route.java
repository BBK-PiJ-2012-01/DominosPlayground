package artificial_player;

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
}
