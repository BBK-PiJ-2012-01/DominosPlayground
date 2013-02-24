package dominoes.players.ai.algorithm.helper;

/**
 * A memo object shared among nodes in the GameState tree to record the number of moves played.
 */
public class MoveCounter {
    private int movesPlayed = 0;
    private final int minPly;

    /**
     * Initialises a MoveCounter with moves played = 0 and the given minimum ply.
     *
     * @param minPly the minimum ply to be assigned to all GameStates in the tree.
     */
    public MoveCounter(int minPly) {
        this.minPly = minPly;
    }

    /**
     * Increment the number of moves played.
     */
    public void incrementMovesPlayed() {
        ++movesPlayed;
    }

    /**
     * Gets the number of moves played.
     *
     * @return the number of moves played.
     */
    public int getMovesPlayed() {
        return movesPlayed;
    }

    /**
     * Gets the minimum ply (to be assigned to all GameStates in the tree).
     *
     * @return the minimum ply (to be assigned to all GameStates in the tree).
     */
    public int getMinPly() {
        return minPly;
    }
}
