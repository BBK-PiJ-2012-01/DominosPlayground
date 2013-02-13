package artificial_player.algorithm.virtual;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;

/**
 * Evaluates the hand purely on the AI's hand
 */
public class MockHandEvaluator implements HandEvaluator {
    @Override
    public double evaluateInitialValue(GameState initialState) {
        double value = 0;

        for (ImmutableBone bone : initialState.getMyBones()) {
            value += bone.weight();
        }

        return value;
    }

    @Override
    public double addedValueFromChoice(Choice choice, GameState state) {
        if (!state.isMyTurn())
            return 0;

        if (choice.getAction() == Choice.Action.PICKED_UP)
            return - choice.getBone().weight();

        else if (choice.getAction() == Choice.Action.PLACED_LEFT || choice.getAction() == Choice.Action.PLACED_RIGHT)
            return + choice.getBone().weight();

        else
            return 0;
    }
}
