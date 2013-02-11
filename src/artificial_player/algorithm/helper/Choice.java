package artificial_player.algorithm.helper;

/**
 * Class representing a choice which takes the game from one GameState to another GameState.
 */
public class Choice {
    public static enum Action {PLACED_RIGHT, PLACED_LEFT, PICKED_UP, PASS}

    private final Action action;
    private final ImmutableBone bone;

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

    public Choice(Action action, ImmutableBone bone) {
        this.action = action;
        this.bone = bone;
    }

    public Action getAction() {
        return action;
    }

    public ImmutableBone getBone() {
        return bone;
    }

    @Override
    public String toString() {
        return action + (bone == null? "" : " bone " + bone);
    }
}
