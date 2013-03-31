package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

/**
 * Evaluates the state value as the expectation value of the opponent's hand's weight minus
 * my hand's weight.
 *
 * @author Sam Wright
 */
public class ExpectationWeightEvaluator implements HandEvaluator {
    private static final int COST_OF_MY_PICKUP = 10;
    private static final int VALUE_OF_OPPONENT_PICKUP = 10;
    private static final int COST_OF_LOSING = 100;
    private static final int VALUE_OF_WINNING = 100;
    private static final double COST_FACTOR_OF_IMPASS = 3;
    private static final double COST_OF_IMPASS = 0;
    private static final double VALUE_OF_OPPONENT_PASS = 0;
    private static final double COST_OF_MY_PASS = 0;

    private int constantValueAdded;

    public ExpectationWeightEvaluator(int constantValueAdded) {
        this.constantValueAdded = constantValueAdded;
    }

    public ExpectationWeightEvaluator() {
        constantValueAdded = 1;
    }

    @Override
    public double evaluateInitialValue(BoneState boneState) {
        int opponentHandWeight = 0;

        for (ImmutableBone bone : boneState.getUnknownBones())
            opponentHandWeight += bone.weight() * boneState.getProbThatOpponentHasBone(bone);

        int my_hand_weight = 0;

        for (ImmutableBone bone : boneState.getMyBones())
            my_hand_weight += bone.weight();

        return opponentHandWeight - my_hand_weight;
    }

    @Override
    public double addedValueFromChoice(BoneState boneState, boolean isMyTurn, boolean prevChoiceWasPass, Choice choice) {
        int addedValue = constantValueAdded;

        if (choice.getAction() == Choice.Action.PLACED_RIGHT
                || choice.getAction() == Choice.Action.PLACED_LEFT) {

            if (isMyTurn) {
                addedValue += choice.getBone().weight();
            } else {
                addedValue -= choice.getBone().weight() * boneState.getProbThatOpponentHasBone(choice.getBone());
            }

        } else if (choice.getAction() == Choice.Action.PICKED_UP) {

            double weightedAverageOfBoneyardCards = 0;
            for (ImmutableBone pickupableBone : boneState.getUnknownBones())
                weightedAverageOfBoneyardCards += pickupableBone.weight() * boneState.getProbThatBoneyardHasBone(pickupableBone);

            weightedAverageOfBoneyardCards /= boneState.getUnknownBones().size();

            if (isMyTurn)
                addedValue -= weightedAverageOfBoneyardCards - COST_OF_MY_PICKUP;
            else
                addedValue += weightedAverageOfBoneyardCards + VALUE_OF_OPPONENT_PICKUP;

        } else if (choice.getAction() == Choice.Action.PASS) {

            if (isMyTurn)
                addedValue -= COST_OF_MY_PASS;
            else
                addedValue += VALUE_OF_OPPONENT_PASS;

        } else {
            throw new RuntimeException("Unhandled action");
        }

        if (choice.getAction() == Choice.Action.PLACED_LEFT || choice.getAction() == Choice.Action.PLACED_RIGHT) {
            if (isMyTurn && boneState.getMyBones().size() == 1)
                addedValue += VALUE_OF_WINNING;
            else if (!isMyTurn && boneState.getSizeOfOpponentHand() == 1)
                addedValue -= COST_OF_LOSING;
        }

        if (choice.getAction() == Choice.Action.PASS && prevChoiceWasPass) {
            addedValue *= COST_FACTOR_OF_IMPASS;
            addedValue -= COST_OF_IMPASS;
        }

        return addedValue;
    }
}

