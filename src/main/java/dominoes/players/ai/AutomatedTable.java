package dominoes.players.ai;

import dominoes.players.ai.algorithm.AIController;
import dominoes.players.ai.algorithm.AIControllerImpl;
import dominoes.players.ai.algorithm.GameOverException;
import dominoes.players.ai.algorithm.helper.Bones;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 13/02/2013
 * Time: 00:11
 */
public class AutomatedTable {
    private List<ImmutableBone> player1bones, player2bones, boneyardBones;
    private boolean verbose = false;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private void setUpBones() {
        List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);

        player1bones = all_bones.subList(0, 7);
        player2bones = all_bones.subList(7, 14);
        boneyardBones = new LinkedList<ImmutableBone>(all_bones.subList(14, 28));
    }

    private void playOnceEach(AIController player1, AIController player2) {
        Choice my_choice = player1.getBestChoice();
        player1.choose(makePickupRandom(my_choice));
        player2.choose(my_choice);

        Choice opponent_choice = player2.getBestChoice();
        player2.choose(makePickupRandom(opponent_choice));
        player1.choose(opponent_choice);
    }

    private Choice makePickupRandom(Choice choice) {

        if (choice.getAction() == Choice.Action.PICKED_UP) {
            if (boneyardBones.isEmpty())
                throw new RuntimeException("Tried to take from empty boneyard! Choice = " + choice);

            choice = new Choice(choice.getAction(), boneyardBones.remove(0));
        }

        return choice;
    }

    /**
     * Plays a game of dominoes between the two AIControllers - first to get pointsToWin wins
     * (and is returned).
     *
     * @param player1 AI player 1.
     * @param player2 AI player 2.
     * @param pointsToWin the number of points required to win the game.
     * @return the winning player.
     */
    public AIController competeAIs(AIController player1, AIController player2, int pointsToWin) {
        int player1score = 0, player2score = 0, player1wins = 0, player2wins = 0, i = 0;
        boolean player1first = true;

        while (player1score < pointsToWin && player2score < pointsToWin) {
            i += 1;

            setUpBones();
            player2.setInitialState(player2bones, !player1first);
            player1.setInitialState(player1bones, player1first);

            try {
                while (true) {
                    if (player1first)
                        playOnceEach(player1, player2);
                    else
                        playOnceEach(player2, player1);
                }
            } catch (GameOverException err) {
                final AIController winner = getWinner(player1, player2);

                if (verbose)
                    System.out.format("Game %d: Player 1 %s (%d vs %d)%n", i,
                            (winner == player1 ? "won" : (winner == player2 ? "lost" : "draw")),
                            player1.getHandWeight(), player2.getHandWeight());

                if (winner == player1) {
                    player1wins += 1;
                    player1score += player2.getHandWeight();
                } else if (winner == player2) {
                    player2wins += 1;
                    player2score += player1.getHandWeight();
                } else {
                    --i;    // If draw, replay.
                }

                player1first = !player1first;
            }
        }

        if (verbose) {
            System.out.format("Player 1 won %d and scored a total of %d%n", player1wins, player1score);
            System.out.format("Player 2 won %d and scored a total of %d%n", player2wins, player2score);
        }

        if (player1score > player2score)
            return player1;
        else
            return player2;
    }

    /**
     * Plays a tournament between the two players.  Each game is won by the player who first reaches pointsToWin,
     * and the overall winner is the player who won the most games (after playing totalGames).
     *
     * @param player1 AI player 1.
     * @param player2 AI player 2.
     * @param pointsToWin the number of points required to win each game.
     * @param totalGames the total number of games to play.
     * @return the winner of the tournament.
     */
    public AIController competeAIsInTournament(AIController player1, AIController player2, int pointsToWin, int totalGames) {
        int player1wins = 0, player2wins = 0;

        for (int i = 0; i < totalGames; ++i) {
            AIController winner = competeAIs(player1, player2, pointsToWin);

            if (winner == player1)
                player1wins += 1;
            else
                player2wins += 1;

            if (verbose)
                System.out.format(" ---- So far, Player 1 has won %d while Player 2 has won %d%n", player1wins, player2wins);
        }

        if (verbose) {
            System.out.println("\n ========== Tournament over! ==========");
            System.out.format("Player 1 won %d vs Player 2 won %d%n", player1wins, player2wins);
        }

        if (player1wins > player2wins)
            return player1;
        else
            return player2;
    }

    private AIController getWinner(AIController player1, AIController player2) {
        final AIController winner;
        if (player1.hasEmptyHand() && !player2.hasEmptyHand())
            winner = player1;
        else if (!player1.hasEmptyHand() && player2.hasEmptyHand())
            winner = player2;
        else {
            if (player1.getHandWeight() < player2.getHandWeight())
                winner = player1;
            else if (player1.getHandWeight() > player2.getHandWeight())
                winner = player2;
            else
                winner = null;
        }

        return winner;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0)
            System.out.println("Ignoring arguments...");

        System.out.println("Player 1 is probabilistic AI, player 2 is a dumb (randomly-choosing) AI");
        AIController probabilisticAI = AIControllerImpl.createQuickerProbabilisticAI();
        AIController randomAI = AIControllerImpl.createRandomAI();
        AutomatedTable table = new AutomatedTable();
        table.setVerbose(true);

        int gameType = askUserForInteger("Play a tournament [1] or a single game [2]: ");

        if (gameType == 1) {
            int gamesToPlay = askUserForInteger("Enter number of games to play: ");
            int pointsToWin = askUserForInteger("Enter number of points required to win a game: ");
            table.competeAIsInTournament(probabilisticAI, randomAI, pointsToWin, gamesToPlay);

        } else if (gameType == 2) {
            int pointsToWin = askUserForInteger("Enter number of points required to win: ");
            table.competeAIs(probabilisticAI, randomAI, pointsToWin);

        } else {
            System.out.println("Not a valid choice");
        }


    }

    private static int askUserForInteger(String question) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(question);

            try {
                String input = br.readLine();
                return Integer.parseInt(input);
            } catch (IOException e) {
                System.out.print("Bad input - ");
            }
        }
    }
}
