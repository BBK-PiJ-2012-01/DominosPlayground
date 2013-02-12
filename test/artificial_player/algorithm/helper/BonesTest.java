package artificial_player.algorithm.helper;

import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 17:32
 */
public class BonesTest {
    @Test
    public void testGetAllBones() throws Exception {
        Set<ImmutableBone> allBones = Bones.getAllBones();

        assertEquals(28, allBones.size());

        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                assertTrue(allBones.contains(new ImmutableBone(i, j)));
            }
        }
    }
}
