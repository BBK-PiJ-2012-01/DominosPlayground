package artificial_player.algorithm.first_attempt;

import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.GameState;
import artificial_player.algorithm.virtual.HandEvaluator;

/**
 * Evaluates the state value as the expectation value of the opponent's hand's weight minus
 * my hand's weight.
 */
public class ExpectationWeightEvaluator implements HandEvaluator {
    private static final int COST_OF_MY_PICKUP = 20;
    private static final int VALUE_OF_OPPONENT_PICKUP = 5;

    @Override
    public double evaluateInitialValue(GameState initialState) {
        int opponentHandWeight = 0;

        for (CopiedBone bone : initialState.getPossibleOpponentBones())
            opponentHandWeight += bone.weight();

        int my_hand_weight = 0;

        for (CopiedBone bone : initialState.getMyBones())
            my_hand_weight += bone.weight();

        return opponentHandWeight * initialState.probThatOpponentHasBone() - my_hand_weight;
    }

    @Override
    public double addedValueFromChoice(Choice choice, GameState state) {
        int addedValue = 2;

        if (choice.getAction() == Choice.Action.PLACED_RIGHT
                || choice.getAction() == Choice.Action.PLACED_LEFT) {

            if (state.isMyTurn()) {
                addedValue += choice.getBone().weight();
            } else {
                addedValue -= choice.getBone().weight() * state.probThatOpponentHasBone();
            }

        } else if (choice.getAction() == Choice.Action.PICKED_UP) {

            double average_of_boneyard_cards = 0;
            for (CopiedBone pickupable_bone : state.getPossibleOpponentBones()) {
                average_of_boneyard_cards += pickupable_bone.weight();
            }
            average_of_boneyard_cards /= state.getSizeOfBoneyard() + state.getSizeOfOpponentHand();

            if (state.isMyTurn()) {
                addedValue -= average_of_boneyard_cards - COST_OF_MY_PICKUP;
            } else {
                addedValue += average_of_boneyard_cards + VALUE_OF_OPPONENT_PICKUP;
            }

        } else if (choice.getAction() == Choice.Action.PASS) {

            addedValue += 0;

        } else {
            throw new RuntimeException("Unhandled action");
        }

        return addedValue;
    }
}
