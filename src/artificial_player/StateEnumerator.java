package artificial_player;

import java.util.LinkedList;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 20:54
 */
public interface StateEnumerator {
    /**
     * Given the available bones to use, return the complete set of valid initial placing
     * choices.
     *
     * @param availableBones the bones that can be placed.
     * @return the complete set of valid initial choices.
     */
    Set<Choice> getValidInitialChoices(Set<Bone2> availableBones);

    /**
     * Given the available bones to use and the rightmost and leftmost values in the layout,
     * return the complete set of valid placing choices.
     *
     * @param availableBones the bones that can be placed.
     * @param layoutLeft the leftmost value in the layout.
     * @param layoutRight the rightmost value in the layout.
     * @return the complete set of valid placing choices.
     */
    Set<Choice> getValidPlacingChoices(Set<Bone2> availableBones, int layoutLeft, int layoutRight);

    /**
     * Given the available bones that might be able to be picked up, this returns all
     * valid pickup choices.
     *
     * @param bonesThatCanBePickedUp the set of bones that could be picked up.
     * @return the complete set of valid pickup choices.
     */
    Set<Choice> getValidPickupChoices(Set<Bone2> bonesThatCanBePickedUp);

    /**
     * Returns all valid choices the AI could make, given the bones that have already been placed,
     * the bones in the AI's hand, and the bones that might be in the boneyard.
     *
     * @param layout the bones that have already been placed in the layout.
     * @param myBones the bones in the AI's hand.
     * @param possibleBoneyardBones all bones that might be in the boneyard.
     * @return all valid choices the AI could make.
     */
    Set<Choice> getMyValidChoices(LinkedList<Bone2> layout, Set<Bone2> myBones, Set<Bone2> possibleBoneyardBones);

    /**
     * Returns all valid choices the AI's opponent could make, given the bones that have already
     * been placed and the bones that might be in the opponent's hand.
     *
     * @param layout the bones that have already been placed in the layout.
     * @param possibleOpponentBones all bones that might be in the opponent's hand.
     * @return all valid choices the AI's opponent could make.
     */
    Set<Choice> getOpponentValidChoices(LinkedList<Bone2> layout, Set<Bone2> possibleOpponentBones);
}
