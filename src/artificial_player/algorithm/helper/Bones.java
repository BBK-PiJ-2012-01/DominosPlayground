package artificial_player.algorithm.helper;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:40
 */
public class Bones {

    public static Set<CopiedBone> getAllBones() {
        // Enumerate all bones
        Set<CopiedBone> allBones = new HashSet<CopiedBone>();
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                allBones.add(new CopiedBone(i, j));
            }
        }

        return allBones;
    }
}
