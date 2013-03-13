package dominoes.players;


import dominoes.*;
import dominoes.players.ai.algorithm.AIBuilder;
import dominoes.players.ai.algorithm.AIController;
import dominoes.players.ai.algorithm.helper.Bones;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:50
 */
public class AIPlayer implements dominoes.players.DominoPlayer{
    private final AIController ai = AIBuilder.createAI("ProbabilisticAI");
    private final List<ImmutableBone> initialHand = new ArrayList<ImmutableBone>();

    private boolean firstMove;
    private int points = 0;
    private String name = "AI Bob";
    private Bone[] prevLayout;
    private Table currentTable;


    public AIPlayer() {
        newRound();
    }

    @Override
    public Play makePlay(Table table) throws CantPlayException {
        Bone[] table_layout = table.layout();

        if (firstMove) {
            // First call to 'makePlay' of this game

            if (table_layout.length == 0) {
                // The first move of the game is mine
                ai.setInitialState(initialHand, true);
            } else {
                ai.setInitialState(initialHand, false);
                ImmutableBone opponentBone = new ImmutableBone(table.left(), table.right());
                Choice opponentsChoice = new Choice(Choice.Action.PLACED_RIGHT, opponentBone);
                ai.choose(opponentsChoice);
            }

            currentTable = table;

        } else {
            // Not the first call to 'makePlay' of this game
            // First, work out what the opponent did:
            ai.choose(getOpponentsLastChoice(table_layout));

            assert table == currentTable;
        }

        // Now make my best choice:
        Choice myChoice = ai.getBestChoice();
        ai.choose(myChoice);
        int matchingValue = 0;
        Play myPlay;

        // And update my internal memory of the table
        if (myChoice.getAction().isPlacement()) {
            prevLayout = new Bone[table_layout.length + 1];
            Bone bone = myChoice.getBone().cloneAsBone();

            if (firstMove) {
                prevLayout[0] = bone;
                myPlay = myChoice.convertToPlay(true);
            } else if (myChoice.getAction() == Choice.Action.PLACED_RIGHT) {
                prevLayout[table_layout.length] = bone;
                System.arraycopy(table_layout, 0, prevLayout, 0, table_layout.length);
                myPlay = myChoice.convertToPlay(false, table.right());
            } else {
                prevLayout[0] = bone;
                System.arraycopy(table_layout, 0, prevLayout, 1, table_layout.length);
                myPlay = myChoice.convertToPlay(false, table.left());
            }

            myPlay = myChoice.convertToPlay(firstMove, matchingValue);
        } else {
            prevLayout = table_layout;
            myPlay = myChoice.convertToPlay(firstMove);
        }


        firstMove = false;
        return myPlay;
    }

    private Choice getOpponentsLastChoice(Bone[] layout) {
        int rightPos = layout.length - 1;
        int prevRightPos = prevLayout.length - 1;
        Choice opponentsChoice;

        if (!prevLayout[0].equals(layout[0])) {
            // The opponent put a bone on the left
            opponentsChoice = new Choice(Choice.Action.PLACED_LEFT, new ImmutableBone(layout[0]));
        } else if (!prevLayout[prevRightPos].equals(layout[rightPos])) {
            // The opponent put a bone on the right
            opponentsChoice = new Choice(Choice.Action.PLACED_RIGHT, new ImmutableBone(layout[rightPos]));
        } else {
            // The opponent must have picked up from the boneyard
            opponentsChoice = new Choice(Choice.Action.PICKED_UP, null);
        }

        return opponentsChoice;
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Wasn't expecting to takeBack a bone!");
    }

    @Override
    public void draw(BoneYard boneYard) {
        ImmutableBone pickedUpBone = new ImmutableBone(boneYard.draw());

        if (firstMove) {
            // Just add to initialHand (they'll be given to the AI when 'makePlay' is first called.
            initialHand.add(pickedUpBone);
        } else {
            // First, process the opponent's move:
            ai.choose(getOpponentsLastChoice(currentTable.layout()));
            // Then pick up
            Choice choice = new Choice(Choice.Action.PICKED_UP, pickedUpBone);
            ai.choose(choice);
        }
    }

    private List<ImmutableBone> getMyInternalBones() {
        try {
            return ai.getMyBones();
        } catch (NullPointerException e) {
            return initialHand;
        }
    }

    @Override
    public int numInHand() {
        return ai.getMyBones().size();
    }

    @Override
    public Bone[] bonesInHand() {
        return Bones.convertToBoneArray(getMyInternalBones());
    }

    @Override
    public void newRound() {
        firstMove = true;
        points = 0;
        initialHand.clear();
    }

    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
