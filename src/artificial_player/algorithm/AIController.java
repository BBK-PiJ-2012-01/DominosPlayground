package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.PlyManager;
import artificial_player.algorithm.virtual.RouteSelector;
import artificial_player.algorithm.virtual.StateEnumerator;

import java.util.List;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 10:46
 */
public class AIController {
    private final PlyManager plyManager;
    private final RouteSelector routeSelector;
    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;

    private GameState currentState;

    public AIController(PlyManager plyManager, RouteSelector routeSelector,
                        StateEnumerator stateEnumerator, HandEvaluator handEvaluator) {

        this.plyManager = plyManager;
        this.routeSelector = routeSelector;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
    }

    public void setInitialState(Set<ImmutableBone> myBones, boolean isMyTurn) {
        currentState = new GameState(stateEnumerator, handEvaluator,
                plyManager.getInitialPly(), myBones, isMyTurn);
    }

    public List<Route> getBestRoutes() {
        List<Route> bestRoutes;
        int[] plyIncreases;
        int i;

        //int n = 0;
        //do {
            bestRoutes = routeSelector.getBestRoutes(currentState, true);

            double[] bestRouteValues = new double[bestRoutes.size()];
            i = 0;
            for (Route route : bestRoutes)
                bestRouteValues[i++] = route.getValue();

            plyIncreases = plyManager.getPlyIncreases(bestRouteValues);

            i = 0;
            for (Route route : bestRoutes) {
                GameState finalState = route.getFinalState();
                //int new_ply = finalState.getPly() + plyIncreases[i++];
                finalState.increasePly(plyIncreases[i++]);
            }
        //} while(n++ < 0);


        return bestRoutes;
    }

    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    public Set<ImmutableBone> getMyBones() {
        return currentState.getMyBones();
    }

    public Set<ImmutableBone> getLayout() {
        return currentState.getLayout();
    }

    public GameState getState() {
        return currentState;
    }

    public Choice getBestChoice() {
        List<Route> bestRoutes = getBestRoutes();
//        if (bestRoutes.isEmpty()) {
//            System.out.println("Problem in AIController.getBestRoute");
//            System.out.println(currentState);
//            System.out.println(currentState.getValidChoices());
//            System.out.println(currentState.getDesiredStatus());
//        }
        // getBestRoutes() is empty if I need to pick up
        if (bestRoutes.isEmpty()) {
            if (currentState.getDesiredStatus() == GameState.Status.IS_LEAF)
                throw new RuntimeException("Game over!");
            else
                return new Choice(Choice.Action.PICKED_UP, null);
        } else
            return bestRoutes.get(0).getEarliestChoice();
    }

}
