package artificial_player.algorithm.helper;

/**
 * A memo object shared among nodes in the GameState tree to record the number of moves played.
 */
public class MoveCounter {
    private int moves_played = 0;

    public void incrementMovesPlayed() {
        ++moves_played;
    }

    public int getMovesPlayed() {
        return moves_played;
    }
}
