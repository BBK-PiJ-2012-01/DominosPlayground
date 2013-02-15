package artificial_player.algorithm.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: Sam Wright
 * Date: 15/02/2013
 * Time: 08:01
 */
public class UnknownBoneManager {
    private static void incrementAllBoneChances(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone) {
        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet())
            newOpponentChancesToHaveBone.put(e.getKey() + 1, e.getValue());

        opponentChancesToHaveBone.clear();
        opponentChancesToHaveBone.putAll(newOpponentChancesToHaveBone);
    }

    private static void setBonesMatchingLayoutToBoneyard(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone, int layoutLeft, int layoutRight) {
        List<ImmutableBone> bonesToPutInBoneyard = new LinkedList<ImmutableBone>();
        List<ImmutableBone> matchingBonesInList = new LinkedList<ImmutableBone>();

        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values()) {
            matchingBonesInList.clear();

            for (ImmutableBone bone : boneList) {
                if (bone.matches(layoutLeft) || bone.matches(layoutRight))
                    matchingBonesInList.add(bone);
            }

            boneList.removeAll(matchingBonesInList);
            bonesToPutInBoneyard.addAll(matchingBonesInList);
        }

        List<ImmutableBone> bonesWithZeroChances = opponentChancesToHaveBone.get(0);
        if (bonesWithZeroChances == null) {
            bonesWithZeroChances = new LinkedList<ImmutableBone>();
            opponentChancesToHaveBone.put(0, bonesWithZeroChances);
        }

        bonesWithZeroChances.addAll(bonesToPutInBoneyard);

    }
}
