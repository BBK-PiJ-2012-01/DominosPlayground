package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.Choice;

import java.util.Collections;
import java.util.List;

/**
 * An AI that chooses bones to place at random.
 *
 * @author Sam Wright
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
