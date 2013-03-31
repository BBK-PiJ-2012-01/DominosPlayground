package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import org.mockito.Mock;

/**
 * Evaluates the hand purely on the AI's hand
 *
 * @author Sam Wright
 */
public class MockHandEvaluator implements HandEvaluator {
    @Mock
    private HandEvaluator mock;

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

        else if (choice.getAction().isPlacement())
            return + choice.getBone().weight();

        else
            return 0;
    }
}
