package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.components.ExpectationWeightEvaluator;
import dominoes.players.ai.algorithm.components.StateEnumeratorImpl;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of AIController.
 *
 * @author Sam Wright
 */
public abstract class SimpleAIController implements AIController {
    private static final int MAX_PLY = 1;
    private GameState currentState;


    @Override
    public void setInitialState(List<ImmutableBone> myBones, boolean isMyTurn, int sizeOfBoneyard, ImmutableBone... initialLayout) {
        currentState = new GameStateImpl(new StateEnumeratorImpl(), new ExpectationWeightEvaluator(),
                MAX_PLY, myBones, isMyTurn, sizeOfBoneyard, initialLayout);

    }

    @Override
    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    @Override
    public int getHandWeight() {
        int score = 0;

        for (ImmutableBone bone : currentState.getBoneState().getMyBones()) {
            score += bone.weight();
        }

        return score;
    }

    public List<GameState> getChildStates() {
        List<GameState> childStates = new ArrayList<GameState>(currentState.getChildStates());
        if (childStates.isEmpty()) {
            assert currentState.getStatus() == GameState.Status.GAME_OVER;
            throw new GameOverException();
        }

        return childStates;
    }

    @Override
    public GameState getGameState() {
        return currentState;
    }
}
