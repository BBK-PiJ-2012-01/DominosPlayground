package artificial_player.algorithm.first_attempt;

import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.virtual.AbstractStateEnumerator;

import java.util.LinkedList;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 16:26
 */
public class StateEnumeratorImpl extends AbstractStateEnumerator {

    @Override
    public Set<Choice> getMyValidChoices(LinkedList<CopiedBone> layout, Set<CopiedBone> myBones, Set<CopiedBone> possibleBoneyardBones) {
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
            validChoices.add(new Choice(Choice.Action.PASS, null));

        return validChoices;
    }

    @Override
    public Set<Choice> getOpponentValidChoices(LinkedList<CopiedBone> layout, Set<CopiedBone> possibleOpponentBones, int sizeOfBoneyard) {
        Set<Choice> validChoices;

        // Assuming the opponent can place a bone
        validChoices = getValidPlacingChoices(possibleOpponentBones, layout.getFirst().left(), layout.getLast().right());

        // Assuming the opponent can't place a bone, but can pick up:
        if (sizeOfBoneyard != 0) {
            //validChoices.addAll(getValidPickupChoices(possibleOpponentBones));
            validChoices.add(new Choice(Choice.Action.PICKED_UP, null));
        } else {
            // Assuming the opponent can't place or pick up a bone:
            validChoices.add(new Choice(Choice.Action.PASS, null));
        }

        return validChoices;
    }
}
