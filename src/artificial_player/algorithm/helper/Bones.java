package artificial_player.algorithm.helper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:40
 */
public class Bones {
    private static Set<ImmutableBone> allBones;

    static {
        // Enumerate all bones
        Set<ImmutableBone> tempAllBones = new HashSet<ImmutableBone>();
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                tempAllBones.add(new ImmutableBone(i, j));
            }
        }
        allBones = Collections.unmodifiableSet(tempAllBones);
    }

    public static Set<ImmutableBone> getAllBones() {
        return allBones;
    }
}
