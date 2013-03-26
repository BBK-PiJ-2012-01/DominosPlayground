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

    private boolean firstMove;
    private Bone[] prevLayout;
    private Table currentTable;
    private BoneYard boneYard;
    private boolean pickingUp;

    private int points = 0;
    private String name;

    public AIPlayer() {
        newRound();
    }

    @Override
    public Play makePlay(Table table) throws CantPlayException {
        Bone[] table_layout = table.layout();
        currentTable = table;

        if (firstMove) {
            // First call to 'makePlay' of this game
            assert table_layout.length >= INITIAL_LAYOUT_SIZE;

            assert initialHand.size() == HAND_SIZE;

            // Save what was the initial layout, before the game started (ie. if the opponent placed anything first,
            // ignore those bones).
            // NB. it doesn't matter which bones were put there by the opponent and which were there to begin with.
            prevLayout = new Bone[INITIAL_LAYOUT_SIZE];
            System.arraycopy(table_layout, 0, prevLayout, 0, INITIAL_LAYOUT_SIZE);
            List<ImmutableBone> layoutBonesToProcess = Bones.convertToImmutableBoneList(prevLayout);
            ImmutableBone[] initialLayout = layoutBonesToProcess.toArray(new ImmutableBone[0]);

            // Was it my go first?
            boolean myGoFirst = table_layout.length == INITIAL_LAYOUT_SIZE;

            // Set the initial state for the AI
            ai.setInitialState(initialHand.subList(0, HAND_SIZE), myGoFirst, initialLayout);

            // If the opponent went first, process their choices.
            if (!myGoFirst)
                applyOpponentsLastChoices(table_layout);

        } else if(!pickingUp) {
            // Not the first call to 'makePlay' of this game, and the previous choice was the opponent's.
            applyOpponentsLastChoices(table_layout);
        }

        firstMove = false;

        // Now make my best choice:
        Choice myChoice;
        try {
            myChoice = ai.getBestChoice();
        } catch (GameOverException e) {
            throw new CantPlayException();
        }

        // But if I can't place, throw a CantPlayException
        if (!myChoice.getAction().isPlacement()) {
            if (!pickingUp) {
                pickingUp = true;
                prevLayout = currentTable.layout();
            }
            throw new CantPlayException();
        }

        // So now, the choice must be a placement.
        pickingUp = false;
        ai.choose(myChoice);
        Play myPlay;

        // And update my internal memory of the table
        prevLayout = new Bone[table_layout.length + 1];
        Bone bone = myChoice.getBone().cloneAsBone();
        int matchingValue = -1;

        // I should consider what happens when I'm given an empty layout (re defensive coding).
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
            throw new RuntimeException("Layout is never empty");
        else
            myPlay = myChoice.convertToPlay(matchingValue);

        return myPlay;
    }

    /**
     * Given the current layout, this works out what the opponent did and passes those
     * choices to this player's AI.
     *
     * @param layout the current layout (after the opponent made their move(s)).
     */
    private void applyOpponentsLastChoices(Bone[] layout) {
        int rightPos = layout.length - 1;
        int prevRightPos = prevLayout.length - 1;

        int numberOfPickups = ai.getGameState().getBoneState().getSizeOfBoneyard() - boneYard.size();

        for (int i = 0; i < numberOfPickups; ++i)
            ai.choose(new Choice(Choice.Action.PICKED_UP, null));

        if (!prevLayout[0].equals(layout[0])) {
            // The opponent put a bone on the left
            ai.choose(new Choice(Choice.Action.PLACED_LEFT, new ImmutableBone(layout[0])));
        } else if (!prevLayout[prevRightPos].equals(layout[rightPos])) {
            // The opponent put a bone on the right
            ai.choose(new Choice(Choice.Action.PLACED_RIGHT, new ImmutableBone(layout[rightPos])));
        } else {
            // The opponent must have passed
            ai.choose(new Choice(Choice.Action.PASS, null));
        }
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Wasn't expecting to takeBack a bone!");
    }

    @Override
    public void draw(BoneYard boneYard) {
        ImmutableBone pickedUpBone = new ImmutableBone(boneYard.draw());
        this.boneYard = boneYard;

        if (firstMove)
            // Just add to initialHand (they'll be given to the AI when 'makePlay' is first called.
            initialHand.add(pickedUpBone);
        else
            // Pick up
            ai.choose(new Choice(Choice.Action.PICKED_UP, pickedUpBone));
    }

    private List<ImmutableBone> getMyInternalBones() {
        try {
            return ai.getGameState().getBoneState().getMyBones();
        } catch (NullPointerException e) {
            return initialHand;
        }
    }

    @Override
    public int numInHand() {
        return ai.getGameState().getBoneState().getMyBones().size();
    }

    @Override
    public Bone[] bonesInHand() {
        return Bones.convertToBoneArray(getMyInternalBones());
    }

    @Override
    public void newRound() {
        pickingUp = false;
        firstMove = true;
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
