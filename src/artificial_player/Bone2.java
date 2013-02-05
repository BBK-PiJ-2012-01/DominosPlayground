package artificial_player;


/**
 * User: Sam Wright
 * Date: 31/01/2013
 * Time: 12:12
 */
class Bone2 {
    private int left, right;
    private int weight;
    private boolean is_mine;

    public Bone2(int left, int right, boolean is_mine) {
        setValues(left, right, is_mine);
    }

    private void setValues(int left, int right, boolean is_mine) {
        this.weight = left + right;
        this.right = right;
        this.left = left;
        setMine(is_mine);
    }

    public Bone2(Bone2 to_copy) {
        setValues(to_copy.left(), to_copy.right(), to_copy.is_mine);
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
        if (o.getClass() != getClass()) return false;

        Bone2 b = (Bone2) o;

        if (left() == b.left() && right() == b.right()) return true;
        if (left() == b.right() && right() == b.left()) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return weight();
    }

    public boolean matchesNumber(int number) {
        return left() == number || right() == number;
    }

    public void setMine(boolean is_mine) {
        this.is_mine = is_mine;
    }

    public boolean isMine() {
        return is_mine;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", left(), right());
    }
}
