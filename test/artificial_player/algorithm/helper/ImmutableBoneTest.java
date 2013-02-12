package artificial_player.algorithm.helper;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 17:54
 */
public class ImmutableBoneTest {
    ImmutableBone bone;
    int left = 2, right = 3;

    @Before
    public void setUp() throws Exception {
        bone = new ImmutableBone(left, right);
    }

    @Test
    public void testLeft() throws Exception {
        assertEquals(left, bone.left());
    }

    @Test
    public void testRight() throws Exception {
        assertEquals(right, bone.right());
    }

    @Test
    public void testWeight() throws Exception {
        assertEquals(left + right, bone.weight());
    }

    @Test
    public void testEquivalence() throws Exception {
        ImmutableBone duplicateBone = new ImmutableBone(left, right);
        assertEquals(bone, duplicateBone);
    }

    @Test
    public void testEquivalenceAfterFlipping() throws Exception {
        ImmutableBone flippedBone = new ImmutableBone(right, left);
        assertEquals(bone, flippedBone);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("[" + left + "," + right + "]", bone.toString());
    }
}
