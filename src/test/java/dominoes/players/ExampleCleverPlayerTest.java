package dominoes.players;

import dominoes.*;
import dominoes.players.ai.CleverPlayer;
import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 26/03/2013
 * Time: 20:14
 */
public class ExampleCleverPlayerTest {
    private ExampleCleverPlayer player1, player2;
    private Table table;

    @Before
    public void setUp() throws Exception {
        player1 = setupPlayer("Player 1");
        player2 = setupPlayer("Player 2");
    }

    private ExampleCleverPlayer setupPlayer(String name) {
        ExampleCleverPlayer player = new ExampleCleverPlayer();
        player.setName(name);
        return player;
    }

    @Test
    public void testOneRound() throws Exception {
        BoneYard boneYard = new BoneYard(6);
        table = new Table();

        Bone initialBone = boneYard.draw();
        table.play(new Play(initialBone, 2));

        ExampleCleverPlayer playingPlayer = player2, opposingPlayer;
        Map<CleverPlayer, List<Choice>> previousChoices = new HashMap<CleverPlayer, List<Choice>>();
        previousChoices.put(player1, new LinkedList<Choice>());
        previousChoices.put(player2, new LinkedList<Choice>());

        boolean lastMoveWasPass = false;
        int moveNumber = 0;

        player1.newRound();
        player2.newRound();

        for (int i = 0; i < 7; ++i)
            player1.draw(boneYard);

        for (int i = 0; i < 7; ++i)
            player2.draw(boneYard);

        do {
            moveNumber += 1;
            opposingPlayer = playingPlayer;
            playingPlayer = (opposingPlayer == player1) ? player2 : player1;


            List<Choice> currentChoices = previousChoices.get(playingPlayer);
            currentChoices.clear();

            Play play = null;
            boolean firstTryOfGo = true;
            do {
                try {
                    play = playingPlayer.makePlay(table);
                    if (firstTryOfGo && moveNumber > 2) {
                        assertEquals(previousChoices.get(opposingPlayer), playingPlayer.getOpponentsLastChoices());
                        firstTryOfGo = false;
                    }
                    currentChoices.add(playingPlayer.getLastChoice());
                    lastMoveWasPass = false;
                    if (playingPlayer.numInHand() == 0)
                        return;
                } catch (CantPlayException e) {
                    if (firstTryOfGo && moveNumber > 2) {
                        assertEquals(previousChoices.get(opposingPlayer), playingPlayer.getOpponentsLastChoices());
                        firstTryOfGo = false;
                    }
                    if (boneYard.size() == 0) {
                        if (lastMoveWasPass)
                            return;
                        else
                            lastMoveWasPass = true;
                        currentChoices.add(new Choice(Choice.Action.PASS, null));
                        break;
                    } else {
                        playingPlayer.draw(boneYard);
                        currentChoices.add(new Choice(Choice.Action.PICKED_UP, null));
                    }
                }
            } while (play == null);

            if (play != null)
                table.play(play);

            if (moveNumber > 2)
                testBoneStates(playingPlayer.getBeforeMyTurn(), opposingPlayer.getAfterMyTurn());

        } while (true);
    }

    @Test
    public void testManyRounds() throws Exception {
        for (int gameId = 0; gameId < 1000; ++gameId)
            testOneRound();

    }

    private void testBoneStates(BoneState one, BoneState two) {
        assertEquals(one.getLayoutLeft(), two.getLayoutLeft());
        assertEquals(one.getLayoutRight(), two.getLayoutRight());
        assertEquals(one.getSizeOfBoneyard(), two.getSizeOfBoneyard());

        testUnknownBones(one, two);
        testUnknownBones(two, one);
    }

    private void testUnknownBones(BoneState one, BoneState two) {
        assertEquals(one.getMyBones().size(), two.getSizeOfOpponentHand());
        Set<ImmutableBone> unknownBones = new HashSet<ImmutableBone>(two.getUnknownBones());
        assertTrue(unknownBones.containsAll(one.getMyBones()));

        float total = 0;

        for (ImmutableBone bone : two.getUnknownBones()) {
            float prob = two.getProbThatOpponentHasBone(bone);

            total += prob;
            assertTrue(bone.toString(), prob < 1.05);
            assertTrue(bone.toString(), prob > -0.05);

            if (one.getMyBones().contains(bone) && two.getUnknownBones().size() == one.getMyBones().size())
                assertEquals(1.0, prob, 0.05);
        }

        assertEquals(two.getSizeOfOpponentHand(), total, 0.05);
    }
}
