package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import dominoes.players.ai.algorithm.components.ExpectationWeightEvaluator;
import dominoes.players.ai.algorithm.components.LinearPlyManager;
import dominoes.players.ai.algorithm.components.StateEnumeratorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 16/02/2013
 * Time: 10:25
 */
public class RandomAIController implements AIController {
    private GameState currentState;


    @Override
    public void setInitialState(List<ImmutableBone> myBones, boolean isMyTurn) {
        currentState = new GameStateImpl(new StateEnumeratorImpl(), new ExpectationWeightEvaluator(),
                new LinearPlyManager().getInitialPly(), myBones, isMyTurn);
    }

    @Override
    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    @Override
    public Choice getBestChoice() {
        List<GameState> childStates = new ArrayList<GameState>(currentState.getChildStates());
        if (childStates.isEmpty()) {
            assert currentState.getStatus() == GameState.Status.GAME_OVER;
            throw new GameOverException();
        }

        Collections.shuffle(childStates);
        Choice randomChoice = childStates.get(0).getChoiceTaken();

        if (randomChoice.getAction() == Choice.Action.PICKED_UP)
            return new Choice(Choice.Action.PICKED_UP, null);
        else
            return randomChoice;
    }

    @Override
    public int getHandWeight() {
        int score = 0;

        for (ImmutableBone bone : currentState.getBoneState().getMyBones()) {
            score += bone.weight();
        }

        return score;
    }

    @Override
    public boolean hasEmptyHand() {
        return currentState.getBoneState().getMyBones().isEmpty();
    }
}
