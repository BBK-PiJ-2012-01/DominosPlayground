package artificial_player.algorithm;

import artificial_player.algorithm.helper.BoneState;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.Route;
import artificial_player.algorithm.probabilisticAI.ExpectationWeightEvaluator;
import artificial_player.algorithm.probabilisticAI.LinearPlyManager;
import artificial_player.algorithm.probabilisticAI.RouteSelectorImpl;
import artificial_player.algorithm.probabilisticAI.StateEnumeratorImpl;
import artificial_player.algorithm.randomAI.ConstantPlyManager;
import artificial_player.algorithm.randomAI.RandomEvaluator;
import artificial_player.algorithm.randomAI.SimpleRouteSelector;
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

    public static AIController createProbablisticAI() {
        return new AIControllerImpl(
                new LinearPlyManager(),
                new RouteSelectorImpl(),
                new StateEnumeratorImpl(),
                new ExpectationWeightEvaluator());
    }

    public static AIController createRandomAI() {
        return new AIControllerImpl(
                new ConstantPlyManager(),
                new SimpleRouteSelector(),
                new StateEnumeratorImpl(),
                new RandomEvaluator());
    }

    private AIControllerImpl(PlyManager plyManager, RouteSelector routeSelector,
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
     * Gets the best possible choice from the current state.  This is where the ply of good states is incremented.
     *
     * @return the best possible choice from the current state.
     */
    private Choice getBestChoiceAfterIncreasingPly() {
        List<Route> bestRoutes;
        int[] plyIncreases;
        int i;

        int n = 0;
        Choice bestChoice = null;
        int iterationsBestChoiceHasBeenBestFor = 0;
        int minIterationsBestChoiceMustBeBestFor = 50;
//        System.out.println("Number of child states = " + currentState.getChildStates().size());
        do {
            bestRoutes = routeSelector.getBestRoutes(currentState, true);

            if (bestRoutes.isEmpty())
                return null;
            else if (bestRoutes.size() == 1)
                return bestRoutes.get(0).getEarliestChoice();

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

            Choice newBestChoice = bestRoutes.get(0).getEarliestChoice();

            if (bestChoice != newBestChoice) {
                iterationsBestChoiceHasBeenBestFor = 0;
                bestChoice = newBestChoice;
            } else if (bestChoice != null && iterationsBestChoiceHasBeenBestFor == minIterationsBestChoiceMustBeBestFor) {
                break;
            } else {
                ++iterationsBestChoiceHasBeenBestFor;
            }

            if (plyIncreases[0] == 0) {
                break;
            }


        } while(n++ < 300);

//        System.out.println("Best route length is " + bestRoutes.get(0).length() + " with value " + bestRoutes.get(0).getValue());
        return bestChoice;
    }

    @Override
    public void choose(Choice choice) {
        currentState = currentState.choose(choice);
    }

    @Override
    public Choice getBestChoice() {
        Choice bestChoice = getBestChoiceAfterIncreasingPly();

        // getBestChoiceAfterIncreasingPly is null if I need to pick up
        if (bestChoice == null) {
            if (currentState.getStatus() == GameStateImpl.Status.GAME_OVER)
                throw new GameOverException();
            else
                return new Choice(Choice.Action.PICKED_UP, null);
        } else
            return bestChoice;
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
    public boolean hasEmptyHand() {
        return currentState.getBoneState().getMyBones().isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sbuilder = new StringBuilder();
        if (!currentState.isMyTurn())
            sbuilder.append("Move was ");
        else
            sbuilder.append("Opponent's move was ");
        sbuilder.append(currentState.getChoiceTaken());

        BoneState boneState = currentState.getBoneState();

        sbuilder.append(String.format("%n\tlayout = [%d ... %d] , size of boneyard = %d , size of opponent's hand = %d",
                boneState.getLayoutLeft(), boneState.getLayoutRight(),
                boneState.getSizeOfBoneyard(), boneState.getSizeOfOpponentHand()));

        sbuilder.append("\n\tMy bones: ");
        sbuilder.append(boneState.getMyBones());

        return sbuilder.toString();
    }
}
