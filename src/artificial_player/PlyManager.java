package artificial_player;

import java.util.List;

/**
 * The ply is the limit applied to the number of levels allowed in the decision tree.
 * 
 * The ply is set for each GameState, so certain favourable GameStates can be explored
 * further without unfavorable/unrealistic GameStates being wastefully pursued.
 */
public interface PlyManager {
    /**
     * Gets the initial ply.
     *
     * @return the initial ply.
     */
    int getInitialPly();

    /**
     * Given a list of GameStates which represent the best-possible states to end in
     * (though are not leafs in the tree - their children have not yet been calculated
     * because we used up all our ply) with the best first, return an array of ply
     * increases to be applied to these states.
     *
     * If null or an array of zeroes are returned, the algorithm will not continue
     * down the decision tree and will end.  The suggested choice will lead to
     * bestFinalStates.get(0).  This can be used to limit the potential intelligence
     * of the AI.
     *
     * @param bestFinalStates the final states considered to be best, and worth pursuing
     * @return an integer array of ply increases to be applied to the GameStates.
     */
    int[] getPlyIncreases(List<GameState> bestFinalStates);
}
