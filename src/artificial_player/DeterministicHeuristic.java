package artificial_player;

import java.util.Map;

public class DeterministicHeuristic extends AbstractHeuristic {
    private final Map<Choice,GameState> choices;

    public DeterministicHeuristic(boolean my_turn, Map<Choice,GameState> choices) {
        super(my_turn);
        this.choices = choices;
    }

    @Override
    public double getValue() {
        return StateStats.getBestOption(isMyTurn(), choices).getValue().getHeuristic().getValue();
    }

    @Override
    public Choice getChoice() {
        return StateStats.getBestOption(isMyTurn(), choices).getKey();
    }
}
