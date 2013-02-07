package artificial_player.algorithm.helper;


/**
 * User: Sam Wright
 * Date: 31/01/2013
 * Time: 12:12
 */
public class CopiedBone {
    private int left, right;
    private int weight;

    public CopiedBone(int left, int right) {
        setValues(left, right);
    }

    private void setValues(int left, int right) {
        this.weight = left + right;
        this.right = right;
        this.left = left;
    }

    public CopiedBone(CopiedBone to_copy) {
        setValues(to_copy.left(), to_copy.right());
    }

    public void flip() {
        int temp = left;
        left = right;
        right = temp;
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof CopiedBone)) return false;

        CopiedBone b = (CopiedBone) o;

        if (left() == b.left() && right() == b.right()) return true;
        if (left() == b.right() && right() == b.left()) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return weight();
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", left(), right());
    }
}
