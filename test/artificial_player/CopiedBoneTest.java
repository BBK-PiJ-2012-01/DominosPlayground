package artificial_player;

import artificial_player.algorithm.helper.CopiedBone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * User: Sam Wright
 * Date: 02/02/2013
 * Time: 13:55
 */
public class CopiedBoneTest {
    @Test
    public void testEquals() throws Exception {
        assertEquals(new CopiedBone(1,2), new CopiedBone(2,1));
    }

    @Test
    public void testHashCode() throws Exception {
        assertEquals(new CopiedBone(1, 2).hashCode(), new CopiedBone(2, 1).hashCode());
    }
}
