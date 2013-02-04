package artificial_player;

public class LeafHeuristic extends AbstractHeuristic {
    private final double value;

    public LeafHeuristic(boolean my_turn, GameState leaf_state) {
        super(my_turn);
        value = leaf_state.getValue();
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Choice getChoice() {
        return null;
    }
}
