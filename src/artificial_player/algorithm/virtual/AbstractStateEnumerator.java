package artificial_player.algorithm.virtual;

import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.Choice;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:32
 */
public abstract class AbstractStateEnumerator implements StateEnumerator {
    @Override
    public Set<Choice> getValidInitialChoices(Set<CopiedBone> availableBones) {
        Set<Choice> new_states = new HashSet<Choice>();

        for (CopiedBone bone : availableBones) {
            // Can place any of my bones
            new_states.add(new Choice(Choice.Action.PLACED_RIGHT, bone));
        }

        return new_states;
    }

    @Override
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

    @Override
    public Set<Choice> getValidPickupChoices(Set<CopiedBone> bonesThatCanBePickedUp) {
        Set<Choice> validChoices = new HashSet<Choice>();

        for (CopiedBone bone : bonesThatCanBePickedUp) {
            validChoices.add(new Choice(Choice.Action.PICKED_UP, bone));
        }

        return validChoices;
    }

    @Override
    public abstract Set<Choice> getMyValidChoices(LinkedList<CopiedBone> layout, Set<CopiedBone> myBones, Set<CopiedBone> possibleBoneyardBones);

    @Override
    public abstract Set<Choice> getOpponentValidChoices(LinkedList<CopiedBone> layout, Set<CopiedBone> possibleOpponentBones, int sizeOfBoneyard);
}
