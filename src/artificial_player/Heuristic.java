package artificial_player;

/**
 * If the heuristic is for my choice:
 *      - The heuristic will either be deterministic (ie. I can place a bone)
 *      - OR it will be probabilistic (ie. I have to pick up a bone)
 */
public interface Heuristic {
    double getValue();
    Choice getChoice();
    boolean isMyTurn();
}


