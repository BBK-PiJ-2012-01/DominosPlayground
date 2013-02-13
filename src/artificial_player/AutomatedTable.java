package artificial_player;

import artificial_player.algorithm.AIController;
import artificial_player.algorithm.GameOverException;
import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;

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

    private void setUpBones() {
        List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);

        player1bones = all_bones.subList(0, 7);
        player2bones = all_bones.subList(7, 14);
        boneyardBones = new LinkedList<ImmutableBone>(all_bones.subList(14, 28));
    }

    private void playOnceEach(AIController player1, AIController player2, boolean verbose) {
        Choice my_choice = player1.getBestChoice();
        player1.choose(makePickupRandom(my_choice));
        player2.choose(my_choice);

        if (verbose)
            System.out.println("Player 1 " + player1);

        Choice opponent_choice = player2.getBestChoice();
        player2.choose(makePickupRandom(opponent_choice));
        player1.choose(opponent_choice);

        if (verbose)
            System.out.println("Player 2 " + player2);
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
                        playOnceEach(player1, player2, false);
                    else
                        playOnceEach(player2, player1, false);
                }
            } catch (GameOverException err) {
                final AIController winner = getWinner(player1, player2);

                System.out.format("Game %d: I %s (%d vs %d)%n", i,
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

        System.out.format("Player 1 won %d and scored a total of %d%n", player1wins, player1score);
        System.out.format("Player 2 won %d and scored a total of %d%n", player2wins, player2score);

        if (player1score > player2score)
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

}
