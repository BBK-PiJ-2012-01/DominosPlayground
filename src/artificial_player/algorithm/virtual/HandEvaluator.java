package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.GameState;

/**
 * Swappable component of AIContainer that associates a value with each GameState.
 *
 * NB. The suggested convention is for the larger of two values to represent the
 * GameState that is better.
 */
public interface HandEvaluator {
    /**
     * Evaluates the value of the given initial state.
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
