package artificial_player;

/**
 * User: Sam Wright
 * Date: 05/02/2013
 * Time: 01:02
 */
public class Bone2Immutable extends Bone2 {
    public Bone2Immutable(int left, int right) {
        super(left, right);
    }

    @Override
    public void flip() {
        throw new RuntimeException("Cannot mutate immutable bone");
    }

}
