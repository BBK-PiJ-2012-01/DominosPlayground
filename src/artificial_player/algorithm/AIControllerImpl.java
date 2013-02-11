package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.PlyManager;
import artificial_player.algorithm.virtual.RouteSelector;
import artificial_player.algorithm.virtual.StateEnumerator;

import java.util.List;

/**
 * Implementation of AIController, using an iterative process to get the best routes through the decision tree.
 */
public class AIControllerImpl implements AIController {
    private final PlyManager plyManager;
    private final RouteSelector routeSelector;
    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;

    private GameState currentState;

    public AIControllerImpl(PlyManager plyManager, RouteSelector routeSelector,
                            StateEnumerator stateEnumerator, HandEvaluator handEvaluator) {

        this.plyManager = plyManager;
        this.routeSelector = routeSelector;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
    }

    @Override
    public void setInitialState(List<ImmutableBone> myBones, boolean isMyTurn) {
        currentState = new GameStateImpl(stateEnumerator, handEvaluator,
                plyManager.getInitialPly(), myBones, isMyTurn);
    }

    /**
     * Gets the best possible routes from the current state.  This is where the ply of good states is incremented.
     *
     * @return a list of the best routes, ordered best-first.
     */
    private List<Route> getBestRoutes() {
        List<Route> bestRoutes;
        int[] plyIncreases;
        int i;


        // TODO: make this nicer, with adaptive maximum for n (ie. if taken too long or result is static, reduce it.) possibly put in PlyManager?

        int n = 0;
        do {
            bestRoutes = routeSelector.getBestRoutes(currentState, true);

            double[] bestRouteValues = new double[bestRoutes.size()];
            i = 0;
            for (Route route : bestRoutes) {
                bestRouteValues[i++] = route.getValue();
            }

            plyIncreases = plyManager.getPlyIncreases(bestRouteValues);

            i = 0;
            for (Route route : bestRoutes) {
                GameState finalState = route.getFinalState();
                finalState.increasePly(plyIncreases[i++]);
            }
        } while(n++ < 200);


        return bestRoutes;
    }

    @Override
    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    @Override
    public Choice getBestChoice() {
        List<Route> bestRoutes = getBestRoutes();

        // getBestRoutes() is empty if I need to pick up
        if (bestRoutes.isEmpty()) {
            if (currentState.getStatus() == GameStateImpl.Status.GAME_OVER)
                throw new GameOverException();
            else
                return new Choice(Choice.Action.PICKED_UP, null);
        } else
            return bestRoutes.get(0).getEarliestChoice();
    }


    @Override
    public int getScore() {
        int score = 0;

        for (ImmutableBone bone : currentState.getMyBones()) {
            score -= bone.weight();
        }

        return score;
    }

    @Override
    public String toString() {
        StringBuilder sbuilder = new StringBuilder();
        if (currentState.getParent().isMyTurn())
            sbuilder.append("Move was ");
        else
            sbuilder.append("Opponent's move was ");
        sbuilder.append(currentState.getChoiceTaken());

        sbuilder.append(String.format("%n\tlayout = [%d ... %d] , size of boneyard = %d , size of opponent's hand = %d",
                currentState.getLayoutLeft(), currentState.getLayoutRight(),
                currentState.getSizeOfBoneyard(), currentState.getSizeOfOpponentHand()));

        sbuilder.append("\n\tMy bones: ");
        sbuilder.append(currentState.getMyBones());

        return sbuilder.toString();
    }
}
