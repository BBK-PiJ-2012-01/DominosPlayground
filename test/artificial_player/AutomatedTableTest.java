package artificial_player;

import artificial_player.algorithm.AIController;
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
        AIController probabilisticAI = AIControllerImpl.createProbablisticAI();
        AIController randomAI = AIControllerImpl.createRandomAI();
        AIController winner;

        int probWins = 0, randomWins = 0;

        for (int i = 0; i < 100; ++i) {
            winner = table.competeAIs(probabilisticAI, randomAI, 100);

            if (winner == probabilisticAI) {
                System.out.println(" ===== Hurray!  Probabilistic AI beat the random AI. ===== ");
                probWins += 1;
            } else {
                System.out.println(" ===== Aww... random AI beat the probabilistic AI. ===== ");
                randomWins += 1;
            }

            System.out.format("Probabilistic won %d vs Random won %d%n", probWins, randomWins);
        }

        System.out.println("\n\n ========== Tournament over! ==========");
        System.out.format("Probabilistic won %d vs Random won %d%n", probWins, randomWins);
    }
}
