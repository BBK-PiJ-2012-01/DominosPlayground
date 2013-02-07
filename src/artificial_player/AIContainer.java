package artificial_player;

/**
 * User: Sam Wright
 * Date: 06/02/2013
 * Time: 12:49
 */
public interface AIContainer {
    HandEvaluator getHandEvaluator();

    GameState getCurrentState();

    PlyManager getPlyManager();

    StateEnumerator getStateEnumerator();


    StateSelector getStateSelector();
}
