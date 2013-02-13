package artificial_player.algorithm.probabilisticAI;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.BoneManager;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.virtual.AbstractStateEnumerator;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 16:26
 */
public class StateEnumeratorImpl extends AbstractStateEnumerator {

    @Override
    public List<Choice> getMyValidChoices(BoneManager boneManager) {
        List<Choice> validChoices;

        if (boneManager.isLayoutEmpty())
            validChoices = getValidInitialChoices(boneManager.getMyBones());
        else
            validChoices = getValidPlacingChoices(boneManager.getMyBones(),
                    boneManager.getLayoutLeft(), boneManager.getLayoutRight());

        if (validChoices.isEmpty() && boneManager.getSizeOfBoneyard() > 0) {
            // No possible move - must pick up from boneyard
            validChoices.addAll(getValidPickupChoices(boneManager.getUnknownBones() ));
        }


        if (validChoices.isEmpty())
            // Nothing to pick up from boneyard, so pass
            validChoices.add(new Choice(Choice.Action.PASS, null));

        return validChoices;
    }

    @Override
    public List<Choice> getOpponentValidChoices(BoneManager boneManager) {
        List<Choice> validChoices;
        List<ImmutableBone> possibleOpponentBones = boneManager.getUnknownBones();

        if (boneManager.isLayoutEmpty()) {
            // If this is the first move of the game, the opponent will definitely place.
            validChoices = getValidInitialChoices(possibleOpponentBones);
        } else {

            if (boneManager.getSizeOfOpponentHand() > 0)
                // Assuming the opponent can place a bone
                validChoices = getValidPlacingChoices(possibleOpponentBones,
                        boneManager.getLayoutLeft(), boneManager.getLayoutRight());
            else
                validChoices = new ArrayList<Choice>(1);

            if (boneManager.getSizeOfBoneyard() > 0) {
                // Assuming the opponent can't place a bone, but can pick up:
                validChoices.add(new Choice(Choice.Action.PICKED_UP, null));
            } else {
                // Assuming the opponent can't place or pick up a bone:
                validChoices.add(new Choice(Choice.Action.PASS, null));
            }
        }

        return validChoices;
    }
}
