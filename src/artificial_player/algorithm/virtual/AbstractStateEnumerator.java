package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.Choice;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:32
 */
public abstract class AbstractStateEnumerator implements StateEnumerator {
    /**
     * Given the available bones to use, return the complete set of valid initial placing
     * choices.
     *
     * @param availableBones the bones that can be placed.
     * @return the complete set of valid initial choices.
     */
    public Set<Choice> getValidInitialChoices(Set<CopiedBone> availableBones) {
        Set<Choice> new_states = new HashSet<Choice>();

        for (CopiedBone bone : availableBones) {
            // Can place any of my bones
            new_states.add(new Choice(Choice.Action.PLACED_RIGHT, bone));
        }

        return new_states;
    }

    /**
     * Given the available bones to use and the rightmost and leftmost values in the layout,
     * return the complete set of valid placing choices.
     *
     * @param availableBones the bones that can be placed.
     * @param layoutLeft the leftmost value in the layout.
     * @param layoutRight the rightmost value in the layout.
     * @return the complete set of valid placing choices.
     */
    public Set<Choice> getValidPlacingChoices(Set<CopiedBone> availableBones, int layoutLeft, int layoutRight) {
        Set<Choice> validChoices = new HashSet<Choice>();

        // Bones have already been placed
        for (CopiedBone bone : availableBones) {
            // Check right/last of placed bones
            if (layoutRight == bone.left()) {
                validChoices.add(new Choice(Choice.Action.PLACED_RIGHT, bone));
            } else if (layoutRight == bone.right()) {
                CopiedBone flipped_bone = new CopiedBone(bone);
                flipped_bone.flip();
                validChoices.add(new Choice(Choice.Action.PLACED_RIGHT, flipped_bone));
            }

            // Check left/first of placed bones
            if (layoutLeft == bone.right()) {
                validChoices.add(new Choice(Choice.Action.PLACED_LEFT, bone));
            } else if (layoutLeft == bone.left()) {
                CopiedBone flipped_bone = new CopiedBone(bone);
                flipped_bone.flip();
                validChoices.add(new Choice(Choice.Action.PLACED_LEFT, flipped_bone));
            }
        }

        return validChoices;
    }

    /**
     * Given the available bones that might be able to be picked up, this returns all
     * valid pickup choices.
     *
     * @param bonesThatCanBePickedUp the set of bones that could be picked up.
     * @return the complete set of valid pickup choices.
     */
    public Set<Choice> getValidPickupChoices(Set<CopiedBone> bonesThatCanBePickedUp) {
        Set<Choice> validChoices = new HashSet<Choice>();

        for (CopiedBone bone : bonesThatCanBePickedUp) {
            validChoices.add(new Choice(Choice.Action.PICKED_UP, bone));
        }

        return validChoices;
    }
}
