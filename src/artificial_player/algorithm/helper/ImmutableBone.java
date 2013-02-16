package artificial_player.algorithm.helper;


/**
 * An immutable bone (like dominoes.Bone, but not flippable).
 */
public class ImmutableBone {
    private int left, right;
    private int weight;

    public ImmutableBone(int left, int right) {
        this.weight = left + right;
        this.right = right;
        this.left = left;
    }

    public int left() {
        return left;
    }

    public int right() {
        return right;
    }

    public int weight() {
        return weight;
    }

    /**
     * Returns false if neither left nor right match the given number, or true
     * if either left or right match the given number.
     *
     * @param number the number to check for matches against.
     * @return whether the given number matches either left or right.
     */
    public boolean matches(int number) {
        return left() == number || right() == number;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof ImmutableBone)) return false;

        ImmutableBone b = (ImmutableBone) o;

        if (left() == b.left() && right() == b.right()) return true;
        if (left() == b.right() && right() == b.left()) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return weight;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", left(), right());
    }
}
