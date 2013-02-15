package artificial_player.algorithm;

import artificial_player.algorithm.helper.BoneState;
import artificial_player.algorithm.helper.Choice;

import java.util.List;

/**
 * Immutable class which defines the state of the game at a point in time.  Each GameState
 * is a node in a decision tree, and contains children GameStates.  The links from a parent
 * to a child node are 'Choice's.
 */
public interface GameState {



    public static enum Status {NOT_YET_CALCULATED, HAS_CHILD_STATES, GAME_OVER}
    /**
     * Gets the current status.
     *
     * @return the current status.
     */
    Status getStatus();

    /**
     * Gets all possible child states (ie. states achieved after applying a valid choice to this state).
     *
     * @return all possible child states.
     */
    List<GameState> getChildStates();

    /**
     * Applies a choice, and returns the GameState representing the new state.
     *
     * @param choice the choice to apply.
     * @return the resulting GameState.
     */
    GameState choose(Choice choice);

    /**
     * Returns the choice taken to reach this state.
     *
     * @return the choice taken to reach this state.
     */
    Choice getChoiceTaken();

    /**
     * Returns true iff it is now my turn.
     *
     * @return true iff it is now my turn.
     */
    boolean isMyTurn();

    /**
     * Gets the parent state (ie. the state which this.getChoiceTaken() was applied to).
     *
     * @return the parent state.
     */
    GameState getParent();

    /**
     * Increase the ply for this state (ie. the depth allowed for this branch of the GameState tree).
     *
     * @param plyIncrease the amount to increase the ply by.
     */
    void increasePly(int plyIncrease);

    /**
     * Gets the value of my hand.
     *
     * @return the value of my hand.
     */
    double getValue();

    /**
     * Gets the BoneState object for this game state.
     *
     * @return the BoneState object for this game state.
     */
    BoneState getBoneState();

}
