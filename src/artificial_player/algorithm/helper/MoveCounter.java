package artificial_player.algorithm.helper;

/**
 * A memo object shared among nodes in the GameState tree to record the number of moves played.
 */
public class MoveCounter {
    private int movesPlayed = 0;
    private final int minPly;

    public MoveCounter(int minPly) {
        this.minPly = minPly;
    }

    public void incrementMovesPlayed() {
        ++movesPlayed;
    }

    public int getMovesPlayed() {
        return movesPlayed;
    }

    public int getMinPly() {
        return minPly;
    }
}
