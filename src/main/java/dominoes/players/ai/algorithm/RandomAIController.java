package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import dominoes.players.ai.algorithm.components.ExpectationWeightEvaluator;
import dominoes.players.ai.algorithm.components.LinearPlyManager;
import dominoes.players.ai.algorithm.components.StateEnumeratorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 16/02/2013
 * Time: 10:25
 */
public class RandomAIController extends SimpleAIController {

    @Override
    public Choice getBestChoice() {
        List<GameState> childStates = getChildStates();

        Collections.shuffle(childStates);
        Choice randomChoice = childStates.get(0).getChoiceTaken();

        if (randomChoice.getAction() == Choice.Action.PICKED_UP)
            return new Choice(Choice.Action.PICKED_UP, null);
        else
            return randomChoice;
    }


}
