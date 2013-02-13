package artificial_player.algorithm.probabilisticAI;

import artificial_player.algorithm.helper.ImmutableBone;
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
    private static final int COST_OF_LOSING = 300;
    private static final int VALUE_OF_WINNING = 300;
    private static final double COST_FACTOR_OF_IMPASS = 1;
    private static final double COST_OF_IMPASS = 50;
    private static final double VALUE_OF_OPPONENT_PASS = 10;
    private static final double COST_OF_MY_PASS = 10;

    @Override
    public double evaluateInitialValue(GameState initialState) {
        int opponentHandWeight = 0;

        for (ImmutableBone bone : initialState.getPossibleOpponentBones())
            opponentHandWeight += bone.weight();

        int my_hand_weight = 0;

        for (ImmutableBone bone : initialState.getMyBones())
            my_hand_weight += bone.weight();

        return opponentHandWeight * probThatOpponentHasBone(initialState) - my_hand_weight;
    }

    @Override
    public double addedValueFromChoice(Choice choice, GameState state) {
        int addedValue = 1;

        if (choice.getAction() == Choice.Action.PLACED_RIGHT
                || choice.getAction() == Choice.Action.PLACED_LEFT) {

            if (state.isMyTurn()) {
                addedValue += choice.getBone().weight();
            } else {
                addedValue -= choice.getBone().weight();// * probThatOpponentHasBone(state);
            }

        } else if (choice.getAction() == Choice.Action.PICKED_UP) {

            double average_of_boneyard_cards = 0;
            for (ImmutableBone pickupable_bone : state.getPossibleOpponentBones()) {
                average_of_boneyard_cards += pickupable_bone.weight();
            }
            average_of_boneyard_cards /= state.getSizeOfBoneyard() + state.getSizeOfOpponentHand();

            if (state.isMyTurn()) {
                addedValue -= average_of_boneyard_cards - COST_OF_MY_PICKUP;
            } else {
                addedValue += average_of_boneyard_cards + VALUE_OF_OPPONENT_PICKUP;
            }

        } else if (choice.getAction() == Choice.Action.PASS) {

            if (state.isMyTurn())
                addedValue -= COST_OF_MY_PASS;
            else
                addedValue += VALUE_OF_OPPONENT_PASS;

        } else {
            throw new RuntimeException("Unhandled action");
        }

        if (choice.getAction() == Choice.Action.PLACED_LEFT || choice.getAction() == Choice.Action.PLACED_RIGHT) {
            if (state.isMyTurn() && state.getMyBones().size() == 1)
                addedValue += VALUE_OF_WINNING;
            else if (!state.isMyTurn() && state.getSizeOfOpponentHand() == 1)
                addedValue -= COST_OF_LOSING;
        }

        if (choice.getAction() == Choice.Action.PASS && state.getChoiceTaken().getAction() == Choice.Action.PASS) {
            addedValue *= COST_FACTOR_OF_IMPASS;
            addedValue -= COST_OF_IMPASS;
        }

        return addedValue;
    }

    private double probThatOpponentHasBone(GameState state) {
        // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone is low
        int totalPossibleOpponentBones = state.getSizeOfBoneyard() + state.getSizeOfOpponentHand();

        if (totalPossibleOpponentBones == 0)
            return 0;
        else
            return ((double) state.getSizeOfOpponentHand()) / totalPossibleOpponentBones;
    }
}