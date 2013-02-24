package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import dominoes.players.ai.algorithm.helper.Choice;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 16:26
 */
public class StateEnumeratorImpl extends AbstractStateEnumerator {

    @Override
    public List<Choice> getMyValidChoices(BoneState boneState) {
        List<Choice> validChoices;

        if (boneState.isLayoutEmpty())
            validChoices = getValidInitialChoices(boneState.getMyBones());
        else
            validChoices = getValidPlacingChoices(boneState.getMyBones(),
                    boneState.getLayoutLeft(), boneState.getLayoutRight());

        if (validChoices.isEmpty() && boneState.getSizeOfBoneyard() > 0) {
            // No possible move - must pick up from boneyard
            validChoices.addAll(getValidPickupChoices(boneState.getUnknownBones() ));
        }


        if (validChoices.isEmpty())
            // Nothing to pick up from boneyard, so pass
            validChoices.add(new Choice(Choice.Action.PASS, null));

        return validChoices;
    }

    @Override
    public List<Choice> getOpponentValidChoices(BoneState boneState) {
        List<Choice> validChoices;
        List<ImmutableBone> possibleOpponentBones = boneState.getUnknownBones();

        if (boneState.isLayoutEmpty()) {
            // If this is the first move of the game, the opponent will definitely place.
            validChoices = getValidInitialChoices(possibleOpponentBones);
        } else {

            if (boneState.getSizeOfOpponentHand() > 0)
                // Assuming the opponent can place a bone
                validChoices = getValidPlacingChoices(possibleOpponentBones,
                        boneState.getLayoutLeft(), boneState.getLayoutRight());
            else
                validChoices = new ArrayList<Choice>(1);

            if (boneState.getSizeOfBoneyard() > 0) {
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
