package artificial_player.algorithm.helper;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 15/02/2013
 * Time: 08:01
 */
public class UnknownBoneManager {
    private final Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone;
    private final Map<ImmutableBone, Float> opponentBoneProbs;
    private final List<ImmutableBone> unknownBones;
    private final int sizeOfOpponentHand, sizeOfBoneyard;

    public UnknownBoneManager(List<ImmutableBone> unknownBones, int sizeOfOpponentHand) {
        this.sizeOfBoneyard = unknownBones.size() - sizeOfOpponentHand;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.unknownBones = unknownBones;

        opponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        opponentChancesToHaveBone.put(sizeOfOpponentHand, unknownBones);

        opponentBoneProbs = calculateProbabilities();
    }

    private UnknownBoneManager(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone,
                               int sizeOfOpponentHand, int sizeOfBoneyard) {
        this.opponentChancesToHaveBone = opponentChancesToHaveBone;
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;

        unknownBones = new ArrayList<ImmutableBone>(sizeOfBoneyard + sizeOfOpponentHand);
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values())
            unknownBones.addAll(boneList);

        opponentBoneProbs = calculateProbabilities();
    }

    private Map<ImmutableBone, Float> calculateProbabilities() {
        Map<ImmutableBone, Float> newOpponentBoneProbs = new HashMap<ImmutableBone, Float>();

        // Start from 0 probability (ie. the initial state)
//        for (ImmutableBone bone : unknownBones)
//            newOpponentBoneProbs.put(bone, 0.0);

        int largestNumberOfChances = Collections.max(opponentChancesToHaveBone.keySet());
        int thenAvailableBonesToPickup = 0;

//        List<ImmutableBone> possibleBonesToTake = new LinkedList<ImmutableBone>();
        List<ImmutableBone> possibleBonesToTake = new ArrayList<ImmutableBone>(sizeOfBoneyard + sizeOfOpponentHand);
        float[] thenBoneProb = new float[sizeOfBoneyard + sizeOfOpponentHand];

        for (int i = largestNumberOfChances; i > 0; --i) {
            List<ImmutableBone> bonesNowAbleToBePickedUp = opponentChancesToHaveBone.get(i);
            if (bonesNowAbleToBePickedUp != null) {
                possibleBonesToTake.addAll(bonesNowAbleToBePickedUp);
                thenAvailableBonesToPickup += bonesNowAbleToBePickedUp.size();
            }

            for (int boneId = 0; boneId < possibleBonesToTake.size(); ++boneId) {
                float probOpponentHasBone = thenBoneProb[boneId];
                float probBoneyardHasBone = 1 - probOpponentHasBone;
                float newProbOpponentHasBone = probOpponentHasBone + probBoneyardHasBone / thenAvailableBonesToPickup;

                thenBoneProb[boneId] = newProbOpponentHasBone;
            }

//            for (ImmutableBone bone : possibleBonesToTake) {
//                double probOpponentHasBone = newOpponentBoneProbs.get(bone);
//                double probBoneyardHasBone = 1 - probOpponentHasBone;
//
//                double newProbOpponentHasBone = probOpponentHasBone + probBoneyardHasBone / thenAvailableBonesToPickup;
//
//                newOpponentBoneProbs.put(bone, newProbOpponentHasBone);
//            }

            --thenAvailableBonesToPickup;
        }

        // Bones that have had zero chances to be picked up will have zero probability of being in opponent's hand.
        // I've only dealt with those with greater than zero chances so far, but I'll add these now.  They'll relate
        // to elements in thenBoneProb elements that haven't been set, ie. will be zero.
        List<ImmutableBone> bonesWithZeroProb = opponentChancesToHaveBone.get(0);
        if (bonesWithZeroProb != null)
            possibleBonesToTake.addAll(bonesWithZeroProb);

        // Now persist these probabilities in a map:
        for (int boneId = 0; boneId < possibleBonesToTake.size(); ++boneId)
            newOpponentBoneProbs.put(possibleBonesToTake.get(boneId), thenBoneProb[boneId]);

        return newOpponentBoneProbs;
    }

    public UnknownBoneManager createNext(Choice choiceTaken, boolean isMyTurn, int layoutLeft, int layoutRight) {
        int newSizeOfOpponentHand = sizeOfOpponentHand;
        int newSizeOfBoneyard = sizeOfBoneyard;

        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet())
            newOpponentChancesToHaveBone.put(e.getKey(), new LinkedList<ImmutableBone>(e.getValue()));

        Choice.Action action = choiceTaken.getAction();
        ImmutableBone bone = choiceTaken.getBone();


        if (isMyTurn) {     // If my turn...
            if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                setBoneAsKnown(bone, newOpponentChancesToHaveBone);
            } else
                return this;

        } else {            // If not my turn...
            if (action.isPlacement()) {
                setBoneAsKnown(bone, newOpponentChancesToHaveBone);
                newSizeOfOpponentHand -= 1;
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newSizeOfOpponentHand += 1;

                // If opponent picked up, they can't have any bones containing layoutLeft or layoutRight,
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone, layoutLeft, layoutRight);
                // but they immediately pick up so must add a chance to every unknown bone.
                incrementAllBoneChances(newOpponentChancesToHaveBone);

            } else if (action == Choice.Action.PASS) {
                // If opponent passed, they can't have any bones containing layoutLeft or layoutRight
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone, layoutLeft, layoutRight);
            }
        }

        return new UnknownBoneManager(newOpponentChancesToHaveBone, newSizeOfOpponentHand, newSizeOfBoneyard);
    }

    private static void setBoneAsKnown(ImmutableBone bone, Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone) {
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values()) {
            if (boneList.remove(bone))
                break;
        }
    }

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

            for (ImmutableBone bone : boneList)
                if (bone.matches(layoutLeft) || bone.matches(layoutRight))
                    matchingBonesInList.add(bone);

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

    public Map<ImmutableBone, Float> getOpponentBoneProbs() {
        return opponentBoneProbs;
    }

    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    public List<ImmutableBone> getUnknownBones() {
        return Collections.unmodifiableList(unknownBones);
    }
}
