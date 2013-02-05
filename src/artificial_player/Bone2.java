package artificial_player;


/**
 * User: Sam Wright
 * Date: 31/01/2013
 * Time: 12:12
 */
class Bone2 {
    private int left, right;
    private int weight;

    public Bone2(int left, int right) {
        setValues(left, right);
    }

    private void setValues(int left, int right) {
        this.weight = left + right;
        this.right = right;
        this.left = left;
    }

    public Bone2(Bone2 to_copy) {
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
        if (!(o instanceof Bone2)) return false;

        Bone2 b = (Bone2) o;

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
