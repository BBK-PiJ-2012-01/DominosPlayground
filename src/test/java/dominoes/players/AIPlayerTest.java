package dominoes.players;

import dominoes.Bone;
import dominoes.BoneYard;
import dominoes.DominoUI;
import dominoes.Dominoes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 13/03/2013
 * Time: 10:52
 */
@RunWith(MockitoJUnitRunner.class)
public class AIPlayerTest {
    @Mock
    private DominoUI ui;
    private DominoPlayer player1, player2;

    private DominoPlayer createAIPlayer() {
        return new AIPlayer();
//        return new TestAIPlayer();
    }

    @Test
    public void testBoneyard() throws Exception {
        BoneYard boneYard = new BoneYard(6);

        assertEquals(28, boneYard.size());

        Set<Bone> allBones = new HashSet<Bone>();
        for (int i = 0; i < 28; ++i)
            allBones.add(boneYard.draw());

        assertEquals(28, allBones.size());

    }

    @Test
    public void test() throws Exception {
        player1 = createAIPlayer();
        player2 = createAIPlayer();
        player1.setName("First Player");
        player2.setName("Second Player");
        System.out.println("Playing game...");
        Dominoes game = new Dominoes(ui, player1, player2, 100, 6);
        game.play();

        System.out.println("Player 1 scored " + player1.getPoints());
        System.out.println("Player 2 scored " + player2.getPoints());

        assertTrue((player1.getPoints() > 100 && player2.getPoints() < 100)
                || (player2.getPoints() > 100 && player1.getPoints() < 100));
    }

}
