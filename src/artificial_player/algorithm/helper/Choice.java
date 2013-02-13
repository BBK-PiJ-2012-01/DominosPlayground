package artificial_player.algorithm.helper;

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
