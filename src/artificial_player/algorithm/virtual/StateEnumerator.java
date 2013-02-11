package artificial_player.algorithm.virtual;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.Choice;

import java.util.List;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 20:54
 */
public interface StateEnumerator {

    /**
     * Returns all valid choices the AI could make, given the current state.
     *
     *
     * @param state the current state.
     * @return all valid choices the AI could make.
     */
    List<Choice> getMyValidChoices(GameState state);

    /**
     * Returns all valid choices the AI's opponent could make, given the current state.
     *
     *
     * @param state the current state.
     * @return all valid choices the AI's opponent could make.
     */
    List<Choice> getOpponentValidChoices(GameState state);
}
