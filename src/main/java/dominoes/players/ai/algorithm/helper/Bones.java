package dominoes.players.ai.algorithm.helper;

import dominoes.Bone;

import java.util.*;

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

    /**
     * Given a list of ImmutableBones, this will return an array of equivalent
     * Bones.
     *
     * @param list a list of ImmutableBones.
     * @return an array of equivalent Bones.
     */
    public static Bone[] convertToBoneArray(List<ImmutableBone> list) {
        Bone[] array = new Bone[list.size()];
        int i = 0;

        for (ImmutableBone bone : list)
            array[i++] = bone.cloneAsBone();

        return array;
    }

    public static List<ImmutableBone> convertToImmutableBoneList(Bone[] array) {
        List<ImmutableBone> list = new ArrayList<ImmutableBone>();
        for (Bone bone : array)
            list.add(new ImmutableBone(bone));
        return list;
    }
}
