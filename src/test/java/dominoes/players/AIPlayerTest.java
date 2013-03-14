package dominoes.players;

import dominoes.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 13/03/2013
 * Time: 10:52
 */
public class AIPlayerTest {
    private Table table;
    private BoneYard boneYard;
    private AIPlayer ai1, ai2;

    @Before
    public void setUp() throws Exception {
        table = new Table();
        ai1 = new AIPlayer();
        ai2 = new AIPlayer();
        boneYard = new BoneYard(6);
        table.play(new Play(boneYard.draw(), 2));
    }

    private boolean aiHasMatchingBone(AIPlayer ai, int endValue) {
        for (Bone bone : ai.bonesInHand())
            if (bone.left() == endValue || bone.right() == endValue)
                return true;

        return false;
    }

    private void play(AIPlayer ai) throws CantPlayException, InvalidPlayException {

        if (table.layout().length == 0)
            table.play(ai.makePlay(table));
        else if (aiHasMatchingBone(ai, table.left()) || aiHasMatchingBone(ai, table.right()))
            table.play(ai.makePlay(table));
        else
            ai.draw(boneYard);

        printLayout();
    }

    private void printLayout() {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(table.left()).append(" : { ");

        for (Bone bone : table.layout())
            sbuilder.append("[").append(bone.left()).append(",").append(bone.right()).append("] ");

        sbuilder.append("} : ").append(table.right());
        System.out.println(sbuilder.toString());
    }

    @Test
    public void testMakePlay() throws Exception {
        for (int i = 0; i < 7; ++i) {
            ai1.draw(boneYard);
            ai2.draw(boneYard);
        }

        for (int i = 0; i < 5; ++i) {
            play(ai1);
            play(ai2);
        }

    }

    @Test
    public void testTable() throws Exception {
        Bone bone1 = new Bone(0, 1);
        table.play(new Play(bone1, 2));
        assertArrayEquals(new Bone[]{bone1}, table.layout());
    }

    @Test
    public void testTakeBack() throws Exception {

    }

    @Test
    public void testDraw() throws Exception {
        ai1.draw(boneYard);
        ai2.draw(boneYard);
    }

    @Test
    public void testNumInHand() throws Exception {

    }

    @Test
    public void testBonesInHand() throws Exception {

    }

    @Test
    public void testNewRound() throws Exception {

    }

    @Test
    public void testSetPoints() throws Exception {

    }

    @Test
    public void testGetPoints() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }
}
