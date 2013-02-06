package artificial_player;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 12:37
 */
public interface HandEvaluator {
    /**
     * Evaluates the value of the given initial state (where a large positive
     * value is good, and a large negative value is bad).
     *
     * @param initialState the initial state to evaluate the value for.
     * @return the value.
     */
    double evaluateInitialValue(GameState initialState);

    /**
     * Gets the added value from applying the given choice on the given state.
     *
     * @param choice the choice to be considered.
     * @param state the state the choice will be applied to.
     * @return the added value from applying the choice.
     */
    double addedValueFromChoice(Choice choice, GameState state);
}
