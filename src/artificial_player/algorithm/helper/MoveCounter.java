package artificial_player.algorithm.helper;

/**
 * User: Sam Wright
 * Date: 02/02/2013
 * Time: 16:47
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
