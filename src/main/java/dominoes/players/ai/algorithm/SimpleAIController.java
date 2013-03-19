package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.components.ExpectationWeightEvaluator;
import dominoes.players.ai.algorithm.components.StateEnumeratorImpl;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/03/2013
 * Time: 16:15
 */
public abstract class SimpleAIController implements AIController {
    private static final int MAX_PLY = 1;
    private GameState currentState;


    @Override
    public void setInitialState(List<ImmutableBone> myBones, boolean isMyTurn, ImmutableBone... initialLayout) {
        currentState = new GameStateImpl(new StateEnumeratorImpl(), new ExpectationWeightEvaluator(),
                MAX_PLY, myBones, isMyTurn, initialLayout);
        // TODO: Implement SimpleBoneStateImpl so I don't have to use GameState (which has more functionality than is required here)
    }

    @Override
    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    @Override
    public void skipFirstChoices(List<ImmutableBone> bonesOpponentPlaced, List<ImmutableBone> bonesIPickedUp) {
        currentState = currentState.skipFirstChoices(bonesOpponentPlaced, bonesIPickedUp);
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
    public List<ImmutableBone> getMyBones() {
        return currentState.getBoneState().getMyBones();
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
    public boolean isBoneyardEmpty() {
        return currentState.getBoneState().getSizeOfBoneyard() == 0;
    }
}
