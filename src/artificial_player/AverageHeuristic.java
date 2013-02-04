package artificial_player;

import java.util.Map;

public class AverageHeuristic extends DeterministicHeuristic {
    private final Map<Choice,GameState> choices;

    public AverageHeuristic(boolean my_turn, Map<Choice, GameState> choices) {
        super(my_turn, choices);
        this.choices = choices;
    }

    @Override
    public double getValue() {
        return StateStats.getAverage(choices);
    }
}
