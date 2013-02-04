package artificial_player;

/**
 * User: Sam Wright
 * Date: 02/02/2013
 * Time: 13:47
 */
public abstract class AbstractHeuristic implements Heuristic {
    private final boolean my_turn;

    protected AbstractHeuristic(boolean my_turn) {
        this.my_turn = my_turn;
    }

    @Override
    public String toString() {
        return String.format("Heuristic for %s turn: value=%.1f, choice=%s", (isMyTurn() ? "my" : "opponent's"),
                getValue(), getChoice());
    }

    public boolean isMyTurn() {
        return my_turn;
    }
}
