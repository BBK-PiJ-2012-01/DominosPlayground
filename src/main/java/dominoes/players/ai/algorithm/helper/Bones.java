package dominoes.players.ai.algorithm.helper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Helper class for Bones
 */
public class Bones {
    private static final Set<ImmutableBone> allBones;

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

    /**
     * Returns an immutable set of all possible immutable bones.
     *
     * @return an immutable set of all possible immutable bones.
     */
    public static Set<ImmutableBone> getAllBones() {
        return allBones;
    }
}
