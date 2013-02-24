package dominoes.players.ai;

import org.junit.Before;

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

//    @Test
//    public void testAiInTournament() throws Exception {
////        AIController probabilisticAI = AIControllerImpl.createProbabilisticAI();
//        AIController probabilisticAI = AIControllerImpl.createQuickerProbabilisticAI();
//        AIController randomAI = AIControllerImpl.createRandomAI();
//        AIController winner;
//
//        int probWins = 0, randomWins = 0;
//
//        for (int i = 0; i < 100; ++i) {
//            winner = playGame(probabilisticAI, randomAI, 100);
//
//            if (winner == probabilisticAI)
//                probWins += 1;
//            else
//                randomWins += 1;
//
//            System.out.format("Probabilistic won %d vs Random won %d%n", probWins, randomWins);
//        }
//
//        System.out.println("\n ========== Tournament over! ==========");
//        System.out.format("Probabilistic won %d vs Random won %d%n", probWins, randomWins);
//    }
//
//    private AIController playGame(AIController player1, AIController player2, int pointsToWin) {
//        AIController winner = table.competeAIs(player1, player2, pointsToWin);
//
//        if (winner == player1)
//            System.out.println(" ===== Hurray!  Probabilistic AI beat the random AI. ===== ");
//        else
//            System.out.println(" ===== Aww... random AI beat the probabilistic AI. ===== ");
//
//        return winner;
//    }
//
//    @Test
//    public void testAiInLongGame() throws Exception {
//        AIController probabilisticAI = AIControllerImpl.createProbabilisticAI();
////        AIController randomAI = AIControllerImpl.createRandomAI();
//        AIController randomAI = new RandomAIController();
//
//        AIController winner = playGame(probabilisticAI, randomAI, 1000);
//    }
//
//    @Test
//    public void testQuickVersionInLongGame() throws Exception {
////        AIController probabilisticAI = AIControllerImpl.createProbabilisticAI();
//        AIController benchmark = AIControllerImpl.createRandomAI();
//        AIController quickProbablistic = AIControllerImpl.createQuickerProbabilisticAI();
//        AIController winner = playGame(quickProbablistic, benchmark, 1000);
//    }
//
//    @Test
//    public void testMinStableIterations() throws Exception {
//        AIControllerImpl higherStabilityAI = AIControllerImpl.createQuickerProbabilisticAI();
//        AIControllerImpl lowerStabilityAI = AIControllerImpl.createQuickerProbabilisticAI();
//
//        higherStabilityAI.setStableIterationRequirement(100);
//        lowerStabilityAI.setStableIterationRequirement(50);
//
//        playGame(higherStabilityAI, lowerStabilityAI, 1000);
//    }
//
//    @Test
//    public void testValueAddedPerChoice() throws Exception {
//        AIController higherValueAddedAI = AIControllerImpl.createAIWithValueAddedPerChoice(3);
//        AIController lowerValueAddedAI = AIControllerImpl.createAIWithValueAddedPerChoice(1);
//
//        playGame(higherValueAddedAI, lowerValueAddedAI, 1000);
//    }
}

