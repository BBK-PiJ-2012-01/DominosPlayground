package artificial_player.algorithm.randomAI;

import artificial_player.algorithm.helper.BoneState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.virtual.HandEvaluator;

/**
 * User: Sam Wright
 * Date: 11/02/2013
 * Time: 17:07
 */
public class RandomEvaluator implements HandEvaluator {
    @Override
    public double evaluateInitialValue(BoneState boneState) {
        return Math.abs(Math.random() * 50) - 25;
    }

    @Override
    public double addedValueFromChoice(BoneState boneState, boolean isMyTurn, boolean prevChoiceWasPass, Choice choice) {
        return Math.abs(Math.random() * 10) - 5;
    }
}
