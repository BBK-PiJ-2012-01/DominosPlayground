package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.ImmutableBone;
import dominoes.players.ai.algorithm.helper.Choice;

import java.util.*;

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
    public List<Choice> getValidInitialChoices(List<ImmutableBone> availableBones) {
        List<Choice> new_states = new ArrayList<Choice>(availableBones.size());

        for (ImmutableBone bone : availableBones) {
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
    public List<Choice> getValidPlacingChoices(List<ImmutableBone> availableBones, int layoutLeft, int layoutRight) {
        List<Choice> validChoices = new LinkedList<Choice>();

        // Bones have already been placed
        for (ImmutableBone bone : availableBones) {
            // Check right/last of placed bones
            if (bone.matches(layoutRight))
                validChoices.add(new Choice(Choice.Action.PLACED_RIGHT, bone));

            // Check left/first of placed bones
            if (bone.matches(layoutLeft))
                validChoices.add(new Choice(Choice.Action.PLACED_LEFT, bone));
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
    public List<Choice> getValidPickupChoices(List<ImmutableBone> bonesThatCanBePickedUp) {
        List<Choice> validChoices = new ArrayList<Choice>(bonesThatCanBePickedUp.size());

        for (ImmutableBone bone : bonesThatCanBePickedUp) {
            validChoices.add(new Choice(Choice.Action.PICKED_UP, bone));
        }

        return validChoices;
    }
}
