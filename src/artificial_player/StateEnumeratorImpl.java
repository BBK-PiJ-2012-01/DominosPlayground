package artificial_player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 16:26
 */
public class StateEnumeratorImpl implements StateEnumerator {

    @Override
    public Set<Choice> getValidInitialChoices(Set<Bone2> availableBones) {
        Set<Choice> new_states = new HashSet<Choice>();

        for (Bone2 bone : availableBones) {
            // Can place any of my bones
            new_states.add(new Choice(GameState.Action.PLACED_RIGHT, bone));
        }

        return new_states;
    }

    @Override
    public Set<Choice> getValidPlacingChoices(Set<Bone2> availableBones, int layoutLeft, int layoutRight) {
        Set<Choice> validChoices = new HashSet<Choice>();

        // Bones have already been placed
        for (Bone2 bone : availableBones) {
            // Check right/last of placed bones
            if (layoutRight == bone.left()) {
                validChoices.add(new Choice(GameState.Action.PLACED_RIGHT, bone));
            } else if (layoutRight == bone.right()) {
                Bone2 flipped_bone = new Bone2(bone);
                flipped_bone.flip();
                validChoices.add(new Choice(GameState.Action.PLACED_RIGHT, flipped_bone));
            }

            // Check left/first of placed bones
            if (layoutLeft == bone.right()) {
                validChoices.add(new Choice(GameState.Action.PLACED_LEFT, bone));
            } else if (layoutLeft == bone.left()) {
                Bone2 flipped_bone = new Bone2(bone);
                flipped_bone.flip();
                validChoices.add(new Choice(GameState.Action.PLACED_LEFT, flipped_bone));
            }
        }

        return validChoices;
    }

    @Override
    public Set<Choice> getValidPickupChoices(Set<Bone2> bonesThatCanBePickedUp) {
        Set<Choice> validChoices = new HashSet<Choice>();

        for (Bone2 bone : bonesThatCanBePickedUp) {
            validChoices.add(new Choice(GameState.Action.PICKED_UP, bone));
        }

        return validChoices;
    }

    @Override
    public Set<Choice> getMyValidChoices(LinkedList<Bone2> layout, Set<Bone2> myBones, Set<Bone2> possibleBoneyardBones) {
        Set<Choice> validChoices;

        if (layout.isEmpty())
            validChoices = getValidInitialChoices(myBones);
        else
            validChoices = getValidPlacingChoices(myBones, layout.getFirst().left(), layout.getLast().right());

        if (validChoices.isEmpty())
            // No possible move - must pick up from boneyard
            validChoices.addAll(getValidPickupChoices(possibleBoneyardBones));

        if (validChoices.isEmpty())
            // Nothing to pick up from boneyard, so pass
            validChoices.add(new Choice(GameState.Action.PASS, null));

        return validChoices;
    }

    @Override
    public Set<Choice> getOpponentValidChoices(LinkedList<Bone2> layout, Set<Bone2> possibleOpponentBones, int sizeOfBoneyard) {
        Set<Choice> validChoices;

        // Assuming the opponent can place a bone
        validChoices = getValidPlacingChoices(possibleOpponentBones, layout.getFirst().left(), layout.getLast().right());

        // Assuming the opponent can't place a bone, but can pick up:
        if (sizeOfBoneyard != 0) {
            //validChoices.addAll(getValidPickupChoices(possibleOpponentBones));
            validChoices.add(new Choice(GameState.Action.PICKED_UP, null));
        } else {
            // Assuming the opponent can't place or pick up a bone:
            validChoices.add(new Choice(GameState.Action.PASS, null));
        }

        return validChoices;
    }
}
