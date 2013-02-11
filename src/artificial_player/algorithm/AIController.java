package artificial_player.algorithm;

import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;

import java.util.List;

/**
 * The top-level class in the AI algorithm which ties together the GameState, PlyManager, RouteSelector,
 * StateEnumerator, and HandEvaluator classes.
 */
public interface AIController {
    /**
     * Sets the initial GameState given the bones I have been dealt, and whether it is my turn or not.
     *
     * @param myBones the bones I have been dealt.
     * @param isMyTurn true iff the first move is mine.
     */
    void setInitialState(List<ImmutableBone> myBones, boolean isMyTurn);

    /**
     * Make a choice, or record the opponent making a choice.  This updates the internal current state.
     *
     * @param choice the choice to make.
     */
    void choose(Choice choice);

    /**
     * Gets the best choice to make.  If it is my turn, this will be what's best for me.
     * If it's the opponent's turn, this will be what's best for them (ie. what's worst
     * for me).
     *
     * @return the best choice to make.
     */
    Choice getBestChoice();
}
