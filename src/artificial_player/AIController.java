package artificial_player;

import java.util.List;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 10:46
 */
public class AIController {
    private final PlyManager plyManager;
    private final StateSelector stateSelector;
    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;

    private GameState currentState;
    private MoveCounter moveCounter;

    public AIController(PlyManager plyManager, StateSelector stateSelector,
                        StateEnumerator stateEnumerator, HandEvaluator handEvaluator) {

        this.plyManager = plyManager;
        this.stateSelector = stateSelector;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
    }

    public void setInitialState(Set<Bone2> myBones, boolean isMyTurn) {
        moveCounter = new MoveCounter();
        currentState = new GameState(stateEnumerator, handEvaluator, moveCounter,
                plyManager.getInitialPly(), myBones, isMyTurn);
    }

    public List<Route> getBestRoutes() {
        List<Route> bestRoutes;
        int[] plyIncreases;
        int i;

        int n = 0;
        do {
            bestRoutes = stateSelector.getBestRoutes(currentState);

            double[] bestRouteValues = new double[bestRoutes.size()];
            i = 0;
            for (Route route : bestRoutes)
                bestRouteValues[i++] = route.getCumulativeValue();

            plyIncreases = plyManager.getPlyIncreases(bestRouteValues);

            i = 0;
            for (Route route : bestRoutes) {
                GameState finalState = route.getFinalState();
                finalState.setPly(finalState.getPly() + plyIncreases[i++]);
            }
        } while(n++ < 5);

        return bestRoutes;
    }

    public void choose(Choice choice) {
        GameState nextState = currentState.getValidChoices().get(choice);
        if (nextState == null)
            throw new RuntimeException("Choice was not valid: " + choice);
        currentState.getMoveCounter().incrementMovesPlayed();

        currentState = nextState;
    }

}
