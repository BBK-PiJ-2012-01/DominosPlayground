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

    public DefaultAIContainer(Set<Bone2> myBones, boolean isMyTurn) {
        handEvaluator = new HandExpectationEvaluator();
        plyManager = new LinearPlyManager();

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


    // populate choices (calculateChildren)
    // determine which final states should be pursued (getNBestChoicesAndFinalStates)
    // determine ply / extra ply / other numbers (PlyManager)
}
