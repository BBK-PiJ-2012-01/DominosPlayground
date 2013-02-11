package artificial_player.algorithm.probablisticAI;

import artificial_player.algorithm.GameState;
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
    public List<Choice> getMyValidChoices(GameState state) {
        List<Choice> validChoices;

        if (state.getChoiceTaken() == null)
            validChoices = getValidInitialChoices(state.getMyBones());
        else
            validChoices = getValidPlacingChoices(state.getMyBones(), state.getLayoutLeft(), state.getLayoutRight());

        if (validChoices.isEmpty() && state.getSizeOfBoneyard() > 0) {
            // No possible move - must pick up from boneyard
            validChoices.addAll(getValidPickupChoices( state.getPossibleOpponentBones() ));
        }


        if (validChoices.isEmpty())
            // Nothing to pick up from boneyard, so pass
            validChoices.add(new Choice(Choice.Action.PASS, null));

        return validChoices;
    }

    @Override
    public List<Choice> getOpponentValidChoices(GameState state) {
        List<Choice> validChoices;
        List<ImmutableBone> possibleOpponentBones = state.getPossibleOpponentBones();

        if (state.getChoiceTaken() == null) {
            // If this is the first move of the game, the opponent will definitely place.
            validChoices = getValidInitialChoices(possibleOpponentBones);
        } else {

            if (state.getSizeOfOpponentHand() > 0)
                // Assuming the opponent can place a bone
                validChoices = getValidPlacingChoices(possibleOpponentBones, state.getLayoutLeft(), state.getLayoutRight());
            else
                validChoices = new ArrayList<Choice>(1);

            if (state.getSizeOfBoneyard() > 0) {
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
