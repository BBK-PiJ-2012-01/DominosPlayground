package dominoes.players;


import dominoes.*;
import dominoes.players.ai.algorithm.AIBuilder;
import dominoes.players.ai.algorithm.AIController;
import dominoes.players.ai.algorithm.GameOverException;
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
public class AIPlayer implements dominoes.players.DominoPlayer {
    private final static int HAND_SIZE = 7, INITIAL_LAYOUT_SIZE = 1;
    private final AIController ai = AIBuilder.createAI("ProbabilisticAI");
    private final List<ImmutableBone> initialHand = new ArrayList<ImmutableBone>();

    private boolean firstMove, iRequestedPickup;
    private int points = 0;
    private String name = "AI Bob";
    private Bone[] prevLayout;
    private Table currentTable;
    private BoneYard boneYard;


    public AIPlayer() {
        newRound();
    }

    @Override
    public Play makePlay(Table table) throws CantPlayException {
        Bone[] table_layout = table.layout();
        System.out.println(name + " makePlay()");
        System.out.println("\t (table Layout is: " + layoutToString(table_layout));
//        System.out.println("\t (my initial hand has size " + initialHand.size());


        if (firstMove) {
            // First call to 'makePlay' of this game
            assert table_layout.length >= INITIAL_LAYOUT_SIZE;

            List<ImmutableBone> layoutBonesToProcess = Bones.convertToImmutableBoneList(table_layout);

            // Was it my go first?
            int numberOfBonesIPickedUp = initialHand.size() - HAND_SIZE;
            int numberOfMovesIMade = numberOfBonesIPickedUp;
            int numberOfBonesOpponentPickedUp = Bones.getAllBones().size() - boneYard.size() - 2 * HAND_SIZE - INITIAL_LAYOUT_SIZE - numberOfBonesIPickedUp;
            int numberOfOpponentMoves = numberOfBonesOpponentPickedUp + table_layout.length - INITIAL_LAYOUT_SIZE;
            boolean myGoFirst = numberOfMovesIMade == numberOfOpponentMoves;
//            System.out.println("\tMe first? " + myGoFirst);

            for (int i = 0; i < INITIAL_LAYOUT_SIZE; ++i)
                ai.setInitialState(initialHand.subList(0, HAND_SIZE), myGoFirst, layoutBonesToProcess.remove(0));

            if (!layoutBonesToProcess.isEmpty()) {
                List<ImmutableBone> bonesIPickedUp = new ArrayList<ImmutableBone>(initialHand.subList(7, initialHand.size()));
                ai.skipFirstChoices(layoutBonesToProcess, bonesIPickedUp);
            }

            currentTable = table;

        } else {
            // Not the first call to 'makePlay' of this game
            // First, work out what the opponent did:
            ai.choose(getOpponentsLastChoice(table_layout));

            assert table == currentTable;
        }

        System.out.println("\t (my hand is " + ai.getMyBones());

        // Now make my best choice:
        Choice myChoice;

        try {
            myChoice = ai.getBestChoice();
        } catch (GameOverException e) {
            throw new CantPlayException();
        }

        // But if I can't place, throw a CantPlayException
        if (!myChoice.getAction().isPlacement()) {
//            myChoice = new Choice(Choice.Action.PICKED_UP, new ImmutableBone(boneYard.draw()));
            iRequestedPickup = true;
            System.out.println("\t REQUEST PICKUP!");
            throw new CantPlayException();
        }

        // So now, the choice must be a placement.
        ai.choose(myChoice);
        Play myPlay;

        // And update my internal memory of the table
        prevLayout = new Bone[table_layout.length + 1];
        Bone bone = myChoice.getBone().cloneAsBone();
        int matchingValue = -1;
        boolean layoutIsEmpty = table_layout.length == 0;

        if (myChoice.getAction() == Choice.Action.PLACED_RIGHT) {
            prevLayout[table_layout.length] = bone;
            System.arraycopy(table_layout, 0, prevLayout, 0, table_layout.length);
            if (!layoutIsEmpty)
                matchingValue = table.right();
        } else {
            prevLayout[0] = bone;
            System.arraycopy(table_layout, 0, prevLayout, 1, table_layout.length);
            if (!layoutIsEmpty)
                matchingValue = table.left();
        }

        if (layoutIsEmpty)
            myPlay = myChoice.convertToPlay(true);
        else {
            myPlay = myChoice.convertToPlay(false, matchingValue);
            if (myPlay.bone().left() != bone.left())
                bone.flip();
        }



        System.out.println("\t my choice was: " + myChoice);
//        System.out.println("\t (my play was: " + myPlay.end() + " with bone [" + myPlay.bone().left() + "," + myPlay.bone().right() + "]");
//        System.out.println("\t (my prevLayout is: " + layoutToString(prevLayout));

        firstMove = false;
        return myPlay;
    }

    private String layoutToString(Bone[] layout) {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("{ ");

        for (Bone bone : layout) {
            if (bone != null)
                sbuilder.append("[").append(bone.left()).append(",").append(bone.right()).append("] ");
            else {
                System.out.println("\t\t======= found null =========");
                sbuilder.append("[null] ");
            }
        }

        sbuilder.append("}");
        return sbuilder.toString();
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
            if (ai.isBoneyardEmpty())
                opponentsChoice = new Choice(Choice.Action.PASS, null);
            else
                opponentsChoice = new Choice(Choice.Action.PICKED_UP, null);
        }

        System.out.println("\t opponent choice was: " + opponentsChoice);

        return opponentsChoice;
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Wasn't expecting to takeBack a bone!");
    }

    @Override
    public void draw(BoneYard boneYard) {
        ImmutableBone pickedUpBone = new ImmutableBone(boneYard.draw());
        this.boneYard = boneYard;
        System.out.println(name + " draw()");

        if (firstMove) {
            // Just add to initialHand (they'll be given to the AI when 'makePlay' is first called.
            initialHand.add(pickedUpBone);
            if (initialHand.size() > HAND_SIZE)
                System.out.println("Picked up on first move");
        } else {
            // First, process the opponent's move:
            if (!iRequestedPickup) {
                System.out.println("TOLD TO PICK UP!!!!");
                ai.choose(getOpponentsLastChoice(currentTable.layout()));
            } else
                iRequestedPickup = false;

            // And update my internal memory of the table
            prevLayout = currentTable.layout();

            // Then pick up
            Choice choice = new Choice(Choice.Action.PICKED_UP, pickedUpBone);
            ai.choose(choice);
            System.out.println("\t chose to: " + choice);
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
        iRequestedPickup = false;
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
