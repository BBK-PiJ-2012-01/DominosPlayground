package dominoes.players.ai.algorithm.helper;

import java.util.List;

/**
 * User: Sam Wright
 * Date: 14/02/2013
 * Time: 00:23
 */
public interface BoneState {
    BoneState createNext(Choice choiceTaken, boolean isMyTurn);

    /**
     * Gets the size of the boneyard.
     * @return the size of the boneyard.
     */
    int getSizeOfBoneyard();

    /**
     * Gets the size of the opponent's hand.
     *
     * @return the size of the opponent's hand.
     */
    int getSizeOfOpponentHand();

    /**
     * Returns my hand.
     *
     * @return my hand.
     */
    List<ImmutableBone> getMyBones();

    /**
     * Returns all bones which the opponent might have (ie. the bones that are not in my hand
     * or in the layout).
     *
     * @return the bones the opponent might have.
     */
    List<ImmutableBone> getUnknownBones();

    /**
     * Gets the probability that the given bone will be in the opponent's hand.
     *
     * @param bone the bone to check.
     * @return the probability that the given bone will be in the opponent's hand.
     */
    float getProbThatOpponentHasBone(ImmutableBone bone);

    /**
     * Gets the probability that the given bone will be in the boneyard.
     *
     * @param bone the bone to check.
     * @return the probability that the given bone will be in the boneyard.
     */
    float getProbThatBoneyardHasBone(ImmutableBone bone);

    /**
     * Gets the left value of the leftmost bone in the layout.
     * @return the left value of the leftmost bone in the layout.
     */
    int getLayoutLeft();

    /**
     * Gets the right value of the rightmost bone in the layout.
     * @return the right value of the rightmost bone in the layout.
     */
    int getLayoutRight();

    /**
     * Returns true if the layout is empty (ie. this is the initial state before
     * any moves have been made).
     *
     * @return true if the layout is empty.
     */
    boolean isLayoutEmpty();
}
