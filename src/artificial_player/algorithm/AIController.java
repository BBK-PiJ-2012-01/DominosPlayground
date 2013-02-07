package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.MoveCounter;
import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.PlyManager;
import artificial_player.algorithm.virtual.StateEnumerator;
import artificial_player.algorithm.virtual.StateSelector;

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

    public AIController(PlyManager plyManager, StateSelector stateSelector,
                        StateEnumerator stateEnumerator, HandEvaluator handEvaluator) {

        this.plyManager = plyManager;
        this.stateSelector = stateSelector;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
    }

    public void setInitialState(Set<CopiedBone> myBones, boolean isMyTurn) {
        currentState = new GameState(stateEnumerator, handEvaluator, new MoveCounter(),
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
                bestRouteValues[i++] = route.getValue();

            plyIncreases = plyManager.getPlyIncreases(bestRouteValues);

            i = 0;
            for (Route route : bestRoutes) {
                GameState finalState = route.getFinalState();
                finalState.setPly(finalState.getPly() + plyIncreases[i++]);
            }
        } while(n++ < 100);

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
