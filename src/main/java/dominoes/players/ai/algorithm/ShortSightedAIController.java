package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.Choice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/03/2013
 * Time: 16:19
 */
public class ShortSightedAIController extends SimpleAIController {

    private static final Comparator<GameState> comparator = new Comparator<GameState>() {
        @Override
        public int compare(GameState o1, GameState o2) {
            int o1Weight = o1.getChoiceTaken().getBone().weight();
            int o2Weight = o2.getChoiceTaken().getBone().weight();
            return Integer.compare(o1Weight, o2Weight);
        }
    };

    @Override
    public Choice getBestChoice() {
        List<GameState> childStates = getChildStates();

        Choice bestChoice = Collections.max(childStates, comparator).getChoiceTaken();

        if (bestChoice.getAction() == Choice.Action.PICKED_UP)
            return new Choice(Choice.Action.PICKED_UP, null);
        else
            return bestChoice;
    }
}
