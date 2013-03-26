package dominoes.players.ai;

import dominoes.*;
import dominoes.players.DominoPlayer;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 25/03/2013
 * Time: 13:27
 */
public abstract class ObservantPlayer implements DominoPlayer {
    private final static int HAND_SIZE = 7, INITIAL_LAYOUT_SIZE = 1, INITIAL_BONEYARD_SIZE = 28;

    private boolean firstMove;
    private Bone[] prevLayout, initialLayout;
    private BoneYard boneYard;
    private boolean pickingUp;
    private Table table;
    private int prevBoneyardSize;

    /**
     * Resets everything for a new round, which starts with the given initialLayout.
     *
     * The returned array of Choice objects are the choices the opponent has already made
     * (which will be empty, if it is my go first).
     */
    private void setInitialState() {
        Bone[] tableLayout = table.layout();
        prevBoneyardSize = INITIAL_BONEYARD_SIZE - HAND_SIZE * 2 - INITIAL_LAYOUT_SIZE;

        // Save what was the initial layout, before the game started (ie. if the opponent placed anything first,
        // ignore those bones).
        // NB. it doesn't matter which bones were put there by the opponent and which were there to begin with.
        initialLayout = prevLayout = new Bone[INITIAL_LAYOUT_SIZE];
        System.arraycopy(tableLayout, 0, prevLayout, 0, INITIAL_LAYOUT_SIZE);
    }

    /**
     * Returns the choices the opponent made since my last turn.
     *
     * @return the choices the opponent made since my last turn.
     */
    private List<Choice> getOpponentsLastChoices() {
        // If this is the first move, and it's my move, the opponent hasn't played.
        if (firstMove && table.layout().length == INITIAL_LAYOUT_SIZE)
            return Collections.emptyList();

        // If I'm picking up, the opponent hasn't had a chance to play
        if (pickingUp)
            return Collections.emptyList();

        Bone[] tableLayout = table.layout();
        int rightPos = tableLayout.length - 1;
        int prevRightPos = prevLayout.length - 1;

        int numberOfPickups = prevBoneyardSize - boneYard.size();

        List<Choice> choices = new LinkedList<Choice>();

        for (int i = 0; i < numberOfPickups; ++i)
            choices.add(new Choice(Choice.Action.PICKED_UP, null));

        if (!prevLayout[0].equals(tableLayout[0])) {
            // The opponent put a bone on the left
            choices.add(new Choice(Choice.Action.PLACED_LEFT, new ImmutableBone(tableLayout[0])));
        } else if (!prevLayout[prevRightPos].equals(tableLayout[rightPos])) {
            // The opponent put a bone on the right
            choices.add(new Choice(Choice.Action.PLACED_RIGHT, new ImmutableBone(tableLayout[rightPos])));
        } else {
            // The opponent must have passed
            choices.add(new Choice(Choice.Action.PASS, null));
        }

        return choices;
    }



    private Bone[] layoutAfterPlay(Play play) {
        Bone[] tableLayout = table.layout();
        Bone placedBone = play.bone();

        Bone[] newLayout = new Bone[tableLayout.length + 1];

        if (play.end() == Play.RIGHT) {
            newLayout[tableLayout.length] = placedBone;
            System.arraycopy(tableLayout, 0, newLayout, 0, tableLayout.length);
        } else {
            newLayout[0] = placedBone;
            System.arraycopy(tableLayout, 0, newLayout, 1, tableLayout.length);
        }

        return newLayout;
    }

    @Override
    public final Play makePlay(Table table) throws CantPlayException {
        if (firstMove) {
            this.table = table;
            setInitialState();
        }

        Play play;

        try {
            play = makeEducatedPlay(table, getOpponentsLastChoices());
            pickingUp = false;
            firstMove = false;
            prevLayout = layoutAfterPlay(play);
            prevBoneyardSize = boneYard.size();
        } catch (CantPlayException e) {
            pickingUp = ( boneYard.size() != 0 );
            prevLayout = table.layout();
            firstMove = false;
            throw e;
        }

        return play;
    }

    abstract public Play makeEducatedPlay(Table table, List<Choice> opponentsLastChoices) throws CantPlayException;

    @Override
    public final void draw(BoneYard boneYard) {
        if (firstMove)
            this.boneYard = boneYard;

        makeEducatedDraw(boneYard, firstMove);
        prevBoneyardSize = boneYard.size();
    }

    abstract public void makeEducatedDraw(BoneYard boneYard, boolean initialPickup);

    @Override
    public void newRound() {
        firstMove = true;
        pickingUp = false;
    }

    public Bone[] getInitialLayout() {
        return initialLayout;
    }

    public boolean isFirstMove() {
        return firstMove;
    }
}
