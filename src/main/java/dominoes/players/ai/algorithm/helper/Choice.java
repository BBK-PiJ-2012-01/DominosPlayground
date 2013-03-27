package dominoes.players.ai.algorithm.helper;

import dominoes.Bone;
import dominoes.CantPlayException;
import dominoes.Play;

/**
 * Class representing a choice which takes the game from one GameState to another GameState.
 */
public class Choice {
    public static enum Action {
        PLACED_RIGHT(true), PLACED_LEFT(true), PICKED_UP(false), PASS(false);

        private final boolean isPlacement;

        Action(boolean isPlacement) {
            this.isPlacement = isPlacement;
        }

        public boolean isPlacement() {
            return isPlacement;
        }
    }

    private final Action action;
    private final ImmutableBone bone;

    /**
     * Creates a new choice based on the given action and bone.
     *
     * A PASS action must give bone as null, and a PLACE action can't give a null bone.
     *
     * A PICKUP action may give the bone as null (ie. you don't know what the opponent picks up)
     * or not (ie. you know what you have picked up).
     *
     * @param action the action to take.
     * @param bone the bone to perform the action on.
     * @throws IllegalArgumentException if the bone given was not valid (as per the rules above).
     */
    public Choice(Action action, ImmutableBone bone) {
        this.action = action;
        this.bone = bone;

        if (action == Action.PLACED_LEFT || action == Action.PLACED_RIGHT) {
            if (bone == null)
                throw new IllegalArgumentException("Bone can't be null for placement");
        } else if (action == Action.PASS) {
            if (bone != null)
                throw new IllegalArgumentException("Bone must be null for pass");
        }
    }

    public Choice(Play play) {
        if (play.end() == Play.LEFT)
            this.action = Action.PLACED_LEFT;
        else
            this.action = Action.PLACED_RIGHT;
        this.bone = new ImmutableBone(play.bone());
    }

    /**
     * Gets the action taken in this choice.
     *
     * @return the action taken in this choice.
     */
    public Action getAction() {
        return action;
    }

    /**
     * Gets the bone acted upon.
     *
     * @return the bone acted upon.
     */
    public ImmutableBone getBone() {
        return bone;
    }

    /**
     * Converts this to a Play object.
     *
     * @param matchingValue if the action is placement, this is value for the bone to match.
     * @return this as a Play object.
     * @throws CantPlayException if the action is to pick up.
     */
    public Play convertToPlay(int matchingValue) throws CantPlayException {
        int end;
        Bone mutableBone = bone.cloneAsBone();

        if (action == Action.PLACED_LEFT) {
            end = Play.LEFT;
            if (matchingValue != -1) {
                if (mutableBone.left() == matchingValue)
                    mutableBone.flip();
                if (mutableBone.right() != matchingValue)
                    throw new RuntimeException(matchingValue + " didn't match " + this);
            }
        } else if (action == Action.PLACED_RIGHT) {
            end = Play.RIGHT;
            if (matchingValue != -1) {
                if (mutableBone.right() == matchingValue)
                    mutableBone.flip();
                if (mutableBone.left() != matchingValue)
                    throw new RuntimeException(matchingValue + " didn't match " + this);
            }
        } else
            throw new CantPlayException();

        return new Play(mutableBone, end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Choice choice = (Choice) o;

        if (this.getAction() != choice.getAction()) return false;
        if (this.getBone() == null) {
            if (choice.getBone() == null) return true;
            return false;
        }
        if (!this.getBone().equals(choice.getBone())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = action.hashCode();
        if (bone != null)
            result = 31 * result + bone.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return action + (bone == null? "" : " bone " + bone);
    }
}
