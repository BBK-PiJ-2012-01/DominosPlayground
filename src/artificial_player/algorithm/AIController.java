package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.MoveCounter;
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

    private  MoveCounter moveCounter;
    private GameState currentState;

    public AIController(PlyManager plyManager, RouteSelector routeSelector,
                        StateEnumerator stateEnumerator, HandEvaluator handEvaluator) {

        this.plyManager = plyManager;
        this.routeSelector = routeSelector;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
    }

    public void setInitialState(Set<ImmutableBone> myBones, boolean isMyTurn) {
        moveCounter = new MoveCounter();
        currentState = new GameState(stateEnumerator, handEvaluator, moveCounter,
                plyManager.getInitialPly(), myBones, isMyTurn);
    }

    public List<Route> getBestRoutes() {
        List<Route> bestRoutes;
        int[] plyIncreases;
        int i;

        //int n = 0;
        //do {
            bestRoutes = routeSelector.getBestRoutes(currentState);

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
        GameState nextState = currentState.getValidChoices().get(choice);
        if (nextState == null)
            throw new RuntimeException("Choice was not valid: " + choice + " and opponent bones were: " + currentState);

        moveCounter.incrementMovesPlayed();

        currentState = nextState;
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

    public RouteSelector getRouteSelector() {
        return routeSelector;
    }

    // TODO: only apply ply update when isMyTurn.  Also, can choosing opponent move be made less expensive?

    public Choice getBestChoice() {
        List<Route> bestRoutes = getBestRoutes();
//        if (bestRoutes.isEmpty()) {
//            System.out.println("Problem in AIController.getBestRoute");
//            System.out.println(currentState);
//            System.out.println(currentState.getValidChoices());
//            System.out.println(currentState.getDesiredStatus());
//        }
        if (bestRoutes.isEmpty())
            return null;
        else
            return bestRoutes.get(0).getEarliestChoice();
    }

}
