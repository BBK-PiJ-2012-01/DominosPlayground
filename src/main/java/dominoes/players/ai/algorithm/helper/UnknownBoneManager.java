package dominoes.players.ai.algorithm.helper;

import java.util.List;
import java.util.Map;

/**
 * Keeps track of the unknown bones (ie. those in the opponent's hand and the boneyard).
 */
public interface UnknownBoneManager {
    /**
     * Creates the next UnknownBoneManager, given the choice and who made it.
     *
     * @param choiceTaken the choice taken to move to the new state.
     * @param isMyTurn whether the choice was made by me.
     * @param layoutLeft the left-most value of the layout.
     * @param layoutRight the right-most value of the layout.
     * @return the next UnknownBoneManager
     * @throws IllegalStateException when the 'choiceTaken' implies being able to pick up
     *                                  the same bone more than once.
     *
     */
    UnknownBoneManager createNext(Choice choiceTaken, boolean isMyTurn, int layoutLeft, int layoutRight);

    /**
     * Returns the mapping of bones with the probabilities of the opponent holding them.
     *
     * @return the mapping of bones with the probabilities of the opponent holding them.
     */
    Map<ImmutableBone, Float> getOpponentBoneProbs();

    /**
     * Returns the size of the opponent's hand.
     *
     * @return the size of the opponent's hand.
     */
    int getSizeOfOpponentHand();

    /**
     * Returns the size of the boneyard.
     *
     * @return the size of the boneyard.
     */
    int getSizeOfBoneyard();

    /**
     * Returns all bones which are in either the opponent's hand or the boneyard.
     *
     * @return all bones which are in either the opponent's hand or the boneyard.
     */
    List<ImmutableBone> getUnknownBones();
}
