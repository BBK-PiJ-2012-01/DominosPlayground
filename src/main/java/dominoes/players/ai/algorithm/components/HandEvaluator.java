package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;

/**
 * Swappable component of AIContainer that associates a value with each GameState.
 *
 * NB. The suggested convention is for the larger of two values to represent the
 * GameState that is better.
 *
 * @author Sam Wright
 */
public interface HandEvaluator {
    /**
     * Evaluates the value of the given initial state.
     *
     * @param boneState the initial state to evaluate the value for.
     * @return the value.
     */
    double evaluateInitialValue(BoneState boneState);

    /**
     * Gets the added value from applying the given choice on the given state.
     *
     * @param boneState the BoneState to consider acting upon.
     * @param isMyTurn true if the choice to be made is the AI's.
     * @param choice the choice to be considered.
     * @param prevChoiceWasPass true if the previous choice made was a PASS.
     * @return the added value from applying the choice.
     */
    double addedValueFromChoice(BoneState boneState, boolean isMyTurn, boolean prevChoiceWasPass, Choice choice);
}
