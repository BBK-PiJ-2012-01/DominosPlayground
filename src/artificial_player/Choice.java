package artificial_player;

public class Choice {
    private final GameState.Action action;
    private final Bone2 bone;

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

    Choice(GameState.Action action, Bone2 bone) {
        this.action = action;
        this.bone = bone;
    }

    public GameState.Action getAction() {
        return action;
    }

    public Bone2 getBone() {
        return bone;
    }

    @Override
    public String toString() {
        return "Choice{" +
                "action=" + action +
                ", bone=" + bone +
                '}';
    }
}
