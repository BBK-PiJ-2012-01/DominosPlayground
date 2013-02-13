package artificial_player.algorithm.helper;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 17:57
 */
public class MoveCounterTest {
    private MoveCounter moveCounter;
    private final int minPly = 4;

    @Before
    public void setUp() throws Exception {
        moveCounter = new MoveCounter(minPly);
    }

    @Test
    public void testInitialMovesPlayed() throws Exception {
        assertEquals(0, moveCounter.getMovesPlayed());
    }

    @Test
    public void testIncrementMovesPlayed() throws Exception {
        for (int i = 0; i < 100; ++i) {
            assertEquals(i, moveCounter.getMovesPlayed());
            moveCounter.incrementMovesPlayed();
        }
    }

    @Test
    public void testGetMinPly() throws Exception {
        assertEquals(minPly, moveCounter.getMinPly());
    }
}
