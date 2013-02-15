package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.BoneState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;

/**
 * Evaluates the hand purely on the AI's hand
 */
public class MockHandEvaluator implements HandEvaluator {
    @Override
    public double evaluateInitialValue(BoneState boneState) {
        double value = 0;

        for (ImmutableBone bone : boneState.getMyBones()) {
            value += bone.weight();
        }

        return value;
    }

    @Override
    public double addedValueFromChoice(BoneState boneState, boolean isMyTurn, boolean prevChoiceWasPass, Choice choice) {
        if (!isMyTurn)
            return 0;

        if (choice.getAction() == Choice.Action.PICKED_UP)
            return - choice.getBone().weight();

        else if (choice.getAction() == Choice.Action.PLACED_LEFT || choice.getAction() == Choice.Action.PLACED_RIGHT)
            return + choice.getBone().weight();

        else
            return 0;
    }
}
