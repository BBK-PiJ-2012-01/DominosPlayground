package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Concrete implementation of StateEnumerator.
 *
 * @author Sam Wright
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
            List<ImmutableBone> pickupableBones = new LinkedList<ImmutableBone>();
            for (ImmutableBone bone : boneState.getUnknownBones())
                if (boneState.getProbThatBoneyardHasBone(bone) > 0.001)
                    pickupableBones.add(bone);

            validChoices.addAll(getValidPickupChoices(pickupableBones));
        }


        if (validChoices.isEmpty())
            // Nothing to pick up from boneyard, so pass
            validChoices.add(new Choice(Choice.Action.PASS, null));

        return validChoices;
    }

    @Override
    public List<Choice> getOpponentValidChoices(BoneState boneState) {
        List<Choice> validChoices;
        List<ImmutableBone> possibleOpponentBones = new LinkedList<ImmutableBone>();
        for (ImmutableBone bone : boneState.getUnknownBones())
            if (boneState.getProbThatOpponentHasBone(bone) > 0.001)
                possibleOpponentBones.add(bone);

        if (boneState.isLayoutEmpty()) {
            // If this is the first move of the game, the opponent will definitely place.
            validChoices = getValidInitialChoices(possibleOpponentBones);
        } else {

            if (boneState.getSizeOfOpponentHand() > 0) {
                // Assuming the opponent can place a bone
                validChoices = getValidPlacingChoices(possibleOpponentBones,
                        boneState.getLayoutLeft(), boneState.getLayoutRight());
            } else {
                validChoices = new ArrayList<Choice>(1);
            }

            // We want to know if the opponent must be able to play.  To do this, we
            // can ask "are there any bones matching the layout which MUST be in the
            // opponent's hand".  However, it's easier to ask the negative question,
            // "are all bones matching the layout DEFINITELY in the boneyard".
            int possibleOpponentBonesMatchingLayout = 0;
            int spacesLeftInBoneyard = boneState.getSizeOfBoneyard();
            for (ImmutableBone bone : boneState.getUnknownBones())
                if (boneState.getProbThatBoneyardHasBone(bone) > 0.999)
                    spacesLeftInBoneyard -= 1;
                else if (bone.matches(boneState.getLayoutLeft()) || bone.matches(boneState.getLayoutRight()))
                    possibleOpponentBonesMatchingLayout += 1;

            boolean opponentMustHavePlaceableBone = (possibleOpponentBonesMatchingLayout > spacesLeftInBoneyard);


            if (!opponentMustHavePlaceableBone) {
                if (boneState.getSizeOfBoneyard() > 0) {
                    // If the next BoneState after a pickup is invalid, it means
                    boolean opponentCanPickup = true;
                    try {
                        boneState.createNext(new Choice(Choice.Action.PICKED_UP, null), false);
                    } catch (IllegalStateException e) {
                        opponentCanPickup = false;
                    }
                    if (opponentCanPickup)
                        // Assuming the opponent can't place a bone, but can pick up:
                        validChoices.add(new Choice(Choice.Action.PICKED_UP, null));
                } else {
                    // Assuming the opponent can't place or pick up a bone:
                    validChoices.add(new Choice(Choice.Action.PASS, null));
                }
            }
        }

        return validChoices;
    }
}
