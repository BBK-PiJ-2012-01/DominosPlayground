package artificial_player;

import java.util.Set;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 11:12
 */
public class AIContainer {
    private final GameState currentState;
    private final HandEvaluator handEvaluator;

    public AIContainer(Set<Bone2> myBones, boolean isMyTurn, HandEvaluator handEvaluator) {
        this.handEvaluator = handEvaluator;
        currentState = new GameState(this, myBones, isMyTurn);
    }

    public HandEvaluator getHandEvaluator() {
        return handEvaluator;
    }

    public GameState getCurrentState() {
        return currentState;
    }


    // populate choices (calculateChildren)
    // determine which final states should be pursued (getNBestChoicesAndFinalStates)
    // determine ply / extra ply / other numbers (PlyManager)
    // determine value, given previous state and change (HandEvaluator)
}
