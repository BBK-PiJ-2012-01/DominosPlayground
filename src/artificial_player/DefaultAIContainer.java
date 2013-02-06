package artificial_player;

import java.util.Set;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 11:12
 */
public class DefaultAIContainer implements AIContainer {
    private final GameState currentState;
    private final HandEvaluator handEvaluator;
    private final PlyManager plyManager;
    private final StateEnumerator stateEnumerator;

    public DefaultAIContainer(Set<Bone2> myBones, boolean isMyTurn) {
        handEvaluator = new HandExpectationEvaluator();
        plyManager = new LinearPlyManager();
        stateEnumerator = new StateEnumeratorImpl();

        currentState = new GameState(this, myBones, isMyTurn);
    }

    @Override
    public HandEvaluator getHandEvaluator() {
        return handEvaluator;
    }

    @Override
    public GameState getCurrentState() {
        return currentState;
    }

    @Override
    public PlyManager getPlyManager() {
        return plyManager;
    }

    @Override
    public StateEnumerator getStateEnumerator() {
        return stateEnumerator;
    }


    // populate choices (calculateChildren)
    // determine which final states should be pursued (getNBestChoicesAndFinalStates)

}
