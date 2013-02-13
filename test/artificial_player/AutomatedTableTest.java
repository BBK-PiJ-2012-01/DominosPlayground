package artificial_player;

import artificial_player.algorithm.AIControllerImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * User: Sam Wright
 * Date: 13/02/2013
 * Time: 00:37
 */
public class AutomatedTableTest {
    private AutomatedTable table;

    @Before
    public void setUp() throws Exception {
        table = new AutomatedTable();
    }

    @Test
    public void testCompeteAIs() throws Exception {
        table.competeAIs(AIControllerImpl.createProbablisticAI(), AIControllerImpl.createRandomAI(), 1000);
    }
}
