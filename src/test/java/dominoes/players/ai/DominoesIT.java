package dominoes.players.ai;

import dominoes.BoneYard;
import dominoes.DominoUI;
import dominoes.Dominoes;
import dominoes.Table;
import dominoes.players.AIPlayer;
import dominoes.players.DominoPlayer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Any;

import javax.swing.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * User: Sam Wright
 * Date: 12/03/2013
 * Time: 23:38
 */
public class DominoesIT {
    public static class DummyUI implements DominoUI {

        @Override
        public void display(DominoPlayer[] dominoPlayers, Table table, BoneYard boneYard) {
            // Dummy implementation
        }

        @Override
        public void displayRoundWinner(DominoPlayer dominoPlayer) {
            // Dummy implementation
        }

        @Override
        public void displayInvalidPlay(DominoPlayer dominoPlayer) {
            // Dummy implementation
        }
    }

//    @Mock
    private static DominoUI ui = new DummyUI();
    private DominoPlayer player1, player2;



    @Test
    public void test() throws Exception {
        player1 = new AIPlayer();
        player2 = new AIPlayer();
        Dominoes game = new Dominoes(ui, player1, player2, 100, 6);
        game.play();
    }
}
