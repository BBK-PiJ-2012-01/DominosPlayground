package artificial_player;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 11:41
 */
public class HandExpectationEvaluator implements HandEvaluator {
    private static final int COST_OF_MY_PICKUP = 20;
    private static final int VALUE_OF_OPPONENT_PICKUP = 5;

    @Override
    public double evaluateInitialValue(GameState initialState) {
        int opponentHandWeight = 0;

        for (Bone2 bone : initialState.getPossibleOpponentBones()) {
            opponentHandWeight += bone.weight();
        }

        int my_hand_weight = 0;

        for (Bone2 bone : initialState.getMyBones()) {
            my_hand_weight += bone.weight();
        }

        return opponentHandWeight * initialState.probThatOpponentHasBone() - my_hand_weight;
    }

    @Override
    public double addedValueFromChoice(Choice choice, GameState state) {
        if (choice.getAction() == GameState.Action.PLACED_RIGHT
                || choice.getAction() == GameState.Action.PLACED_LEFT) {

            if (state.isMyTurn()) {
                return state.getValue() + choice.getBone().weight();
            } else {
                return state.getValue() - choice.getBone().weight() * state.probThatOpponentHasBone();
            }

        } else if (choice.getAction() == GameState.Action.PICKED_UP) {

            double average_of_boneyard_cards = 0;
            for (Bone2 pickupable_bone : state.getPossibleOpponentBones()) {
                average_of_boneyard_cards += pickupable_bone.weight();
            }
            average_of_boneyard_cards /= state.getSizeOfBoneyard() + state.getSizeOfOpponentHand();

            if (state.isMyTurn()) {
                return state.getValue() - average_of_boneyard_cards - COST_OF_MY_PICKUP;
            } else {
                return state.getValue() + average_of_boneyard_cards + VALUE_OF_OPPONENT_PICKUP;
            }

        } else if (choice.getAction() == GameState.Action.PASS) {

            return state.getValue();

        } else {
            throw new RuntimeException("Unhandled action");
        }
    }
}
