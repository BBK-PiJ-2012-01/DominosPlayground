package artificial_player.algorithm.components;

import artificial_player.algorithm.helper.BoneState;
import artificial_player.algorithm.helper.Choice;

import java.util.List;

/**
 * Class to enumerate all possible future states from a GameState.
 */
public interface StateEnumerator {

    /**
     * Returns all valid choices the AI could make, given the current state.
     *
     * @param boneState the current state's bone manager.
     * @return all valid choices the AI could make.
     */
    List<Choice> getMyValidChoices(BoneState boneState);

    /**
     * Returns all valid choices the AI's opponent could make, given the current state.
     *
     * @param boneState the current state's bone manager.
     * @return all valid choices the AI's opponent could make.
     */
    List<Choice> getOpponentValidChoices(BoneState boneState);
}
