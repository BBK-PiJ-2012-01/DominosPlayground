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
    private Bone[] prevLayout, initialLayout;

    public BoneYard getBoneYard() {
        return boneYard;
    }

    private BoneYard boneYard;
    private Table table;
    private int prevBoneyardSize;

    private boolean firstMove;
    private boolean pickingUp;

    /**
     * Resets everything for a new round, which starts with the given initialLayout.
     *
     * The returned array of Choice objects are the choices the opponent has already made
     * (which will be empty, if it is my go first).
     * @param table the table for the new round.
     */
    private void setInitialState(Table table) {
        this.table = table;
        Bone[] tableLayout = table.layout();
        prevBoneyardSize = boneYard.size();
        initialLayout = prevLayout = tableLayout;
    }

    /**
     * Returns the choices the opponent made since my last turn.
     *
     * @return the choices the opponent made since my last turn.
     */
    private List<Choice> getOpponentsLastChoices() {
        // If this is the first move, and it's my move, the opponent hasn't played.
        if (firstMove)
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

    /**
     * Returns the layout after the given Play object has been played.
     *
     * @param play the Play object to play to the current layout.
     * @return the resulting layout from applying 'play'.
     */
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
        if (firstMove)
            setInitialState(table);

        Play play;

        try {
            play = makeObservantPlay(table, getOpponentsLastChoices());
            pickingUp = false;
            firstMove = false;
            prevLayout = layoutAfterPlay(play);
            prevBoneyardSize = boneYard.size();
        } catch (CantPlayException e) {
            pickingUp = ( boneYard.size() != 0 );
            if (pickingUp)
                prevBoneyardSize =- 1;
            prevLayout = table.layout();
            firstMove = false;
            throw e;
        }

        if (play == null)
            throw new NullPointerException("makeObservantPlay returned null!");

        return play;
    }

    /**
     * Make a play, using the choices I observed the opponent make.
     *
     * @param table the table the game is being played on.
     * @param opponentsLastChoices the list of choices (in chronological order) that the opponent made between
     *                             the previous call to makeObservantPlay and now.
     * @return the chosen play.
     * @throws CantPlayException when a play cannot be made.
     */
    abstract public Play makeObservantPlay(Table table, List<Choice> opponentsLastChoices) throws CantPlayException;

    @Override
    public void draw(BoneYard boneYard) {
        if (firstMove)
            this.boneYard = boneYard;
    }

    @Override
    public void newRound() {
        firstMove = true;
        pickingUp = false;
    }

    /**
     * Returns the initial layout, before either player placed any bones.
     *
     * @return the initial layout, before either player placed any bones.
     */
    public final Bone[] getInitialLayout() {
        Bone[] copiedInitialLayout = new Bone[initialLayout.length];
        System.arraycopy(initialLayout, 0, copiedInitialLayout, 0, initialLayout.length);
        return copiedInitialLayout;
    }

    /**
     * Returns true iff no-one has made a play yet.
     *
     * @return true iff no-one has made a play yet.
     */
    public final boolean isFirstMove() {
        return firstMove;
    }

    /**
     * Returns true iff the player has just picked up.
     *
     * @return true iff the player has just picked up.
     */
    public final boolean isPickingUp() {
        return pickingUp;
    }
}
