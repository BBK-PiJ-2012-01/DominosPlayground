package dominoes.players.ai.algorithm.helper;

import java.util.*;

/**
 * User: Sam Wright
 * Date: 15/02/2013
 * Time: 08:01
 */
public class UnknownBoneManagerImpl implements UnknownBoneManager {
    private final Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone;
    private final Map<ImmutableBone, Float> opponentBoneProbs;
    private final List<ImmutableBone> unknownBones;
    private final int sizeOfOpponentHand;
    private final boolean isOpponentPickup;
    private final int sizeOfBoneyard;

    public UnknownBoneManagerImpl(List<ImmutableBone> unknownBones, int sizeOfBoneyard) {
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = unknownBones.size() - sizeOfBoneyard;
        this.unknownBones = unknownBones;
        isOpponentPickup = false;

        opponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        opponentChancesToHaveBone.put(sizeOfOpponentHand, unknownBones);

        opponentBoneProbs = calculateProbabilities();

    }

    private UnknownBoneManagerImpl(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone,
                                   int sizeOfOpponentHand, int sizeOfBoneyard, boolean isOpponentPickup) {
        this.opponentChancesToHaveBone = opponentChancesToHaveBone;
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.isOpponentPickup = isOpponentPickup;

        unknownBones = new ArrayList<ImmutableBone>(sizeOfBoneyard + sizeOfOpponentHand);
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values())
            unknownBones.addAll(boneList);

        opponentBoneProbs = calculateProbabilities();
    }

    private Map<ImmutableBone, Float> calculateProbabilities() {
        Map<ImmutableBone, Float> newOpponentBoneProbs = new HashMap<ImmutableBone, Float>();

        if (opponentChancesToHaveBone.isEmpty())
            return Collections.emptyMap();

        int largestNumberOfChances = Collections.max(opponentChancesToHaveBone.keySet());

        int thenAvailableBonesToPickup = 0;

        List<ImmutableBone> possibleBonesToTake = new ArrayList<ImmutableBone>(sizeOfBoneyard + sizeOfOpponentHand);
        float[] thenBoneProb = new float[sizeOfBoneyard + sizeOfOpponentHand];


        // If this is in a long line of pickups, we know that all previous pickups can only have picked up
        // a bone not matching the layout, or else we'd have stopped picking up.

        // So if this is a pickup, then we don't need to worry about previous pickups.

        // The underlying structure (opponentChancesToHaveBone) works on the basis that NO pickups involve
        // bones that match the layout, whereas we know that the last one involves all bones.

        // So we pretend that all bones with zero chances have exactly one chance, by merging the lists
        // from 'opponentChancesToHaveBone.get(0)' and 'opponentChancesToHaveBone.get(1)' if 'isOpponentPickup'.

        for (int i = largestNumberOfChances; i > 0; --i) {
            List<ImmutableBone> bonesNowAbleToBePickedUp = opponentChancesToHaveBone.get(i);
            if (bonesNowAbleToBePickedUp != null) {
                possibleBonesToTake.addAll(bonesNowAbleToBePickedUp);
                thenAvailableBonesToPickup += bonesNowAbleToBePickedUp.size();
            }

            // As per the comments above, if this is a pickup, we include the bones with zero chance
            // with the bones that have exactly one chance:
            if (isOpponentPickup && i == 1) {
                List<ImmutableBone> quasiZeroChanceBones = opponentChancesToHaveBone.get(0);
                if (quasiZeroChanceBones != null) {
                    possibleBonesToTake.addAll(quasiZeroChanceBones);
                    thenAvailableBonesToPickup += quasiZeroChanceBones.size();
                }
            }

            /**
             * No bone can be picked more than once! If this is tried, then
             * the number of available bones is zero (and the exception is thrown).
             */
            if (thenAvailableBonesToPickup == 0)
                throw new IllegalStateException("Invalid choice");

            for (int boneId = 0; boneId < possibleBonesToTake.size(); ++boneId) {
                float probOpponentHasBone = thenBoneProb[boneId];
                float probBoneyardHasBone = 1 - probOpponentHasBone;
                float newProbOpponentHasBone = probOpponentHasBone;
                if (Math.abs(probBoneyardHasBone) > 0.001)
                    newProbOpponentHasBone += probBoneyardHasBone / thenAvailableBonesToPickup;

                thenBoneProb[boneId] = newProbOpponentHasBone;
            }

            --thenAvailableBonesToPickup;
        }

        // Bones that have had zero chances to be picked up will have zero probability of being in opponent's hand.
        // I've only dealt with those with greater than zero chances so far, but I'll add these now.  They'll relate
        // to elements in thenBoneProb elements that haven't been set, ie. will be zero.
        // (NB. this is assuming that 'isOpponentPickup == false', otherwise these are dealt with above.  See comments above.)
        if (!isOpponentPickup) {
            List<ImmutableBone> bonesWithZeroProb = opponentChancesToHaveBone.get(0);
            if (bonesWithZeroProb != null)
                possibleBonesToTake.addAll(bonesWithZeroProb);
        }

        // Now persist these probabilities in a map:
        for (int boneId = 0; boneId < possibleBonesToTake.size(); ++boneId)
            newOpponentBoneProbs.put(possibleBonesToTake.get(boneId), thenBoneProb[boneId]);

        return newOpponentBoneProbs;
    }

    @Override
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
            }

        } else {            // If not my turn...
            if (action.isPlacement()) {
                // If the opponent has placed, then each pickup where 'bone' was a contender has had one chance
                // removed from their number of chances of being picked up.

                // These aforementioned bones can be identified by having at least the same number of chances as 'bone'.
                // NB. this works because we can assume that the first pickup was 'bone', and subsequent bones are
                // unaffected.  Only those which were in contention for that first pick (ie. those with at least the same
                // number of chances as 'bone') need to have a chance removed.
                incrementBoneChancesAboveThreshold(newOpponentChancesToHaveBone, getBoneChances(bone), -1);

                setBoneAsKnown(bone, newOpponentChancesToHaveBone);
                newSizeOfOpponentHand -= 1;

            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newSizeOfOpponentHand += 1;

                if (!isOpponentPickup) {
                    // If first pickup: increment all bone chances, due to pickup:
                    incrementBoneChancesAboveThreshold(newOpponentChancesToHaveBone, 0, +1);

                    // but also, the opponent couldn't have had any bones matching the layout.  To make this
                    // easier we will assume that IN ADDITION, this pickup will only involve a bone that does
                    // not match the layout.  If the player places a bone, then we'll know they picked up a
                    // bone that DID match the layout, and can deal with that situation when it arises.
                    setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone, layoutLeft, layoutRight);
                } else {
                    // If not the first pickup, then only bones that don't match the layout have a greater-than-zero
                    // chance.  As such, to increment the chances of bones that don't match the layout we need only
                    // increase the chances of bones which already have a greater-than-zero chance:
                    incrementBoneChancesAboveThreshold(newOpponentChancesToHaveBone, 1, +1);
                }

            }
        }

        // Clean up 'newOpponentChancesToHaveBone' by removing empty lists
        List<Integer> keysToRemove = new LinkedList<Integer>();
        for (Map.Entry<Integer,List<ImmutableBone>> e : newOpponentChancesToHaveBone.entrySet())
            if (e.getValue().isEmpty())
                keysToRemove.add(e.getKey());

        for (Integer keyToRemove : keysToRemove)
            newOpponentChancesToHaveBone.remove(keyToRemove);

        return new UnknownBoneManagerImpl(newOpponentChancesToHaveBone, newSizeOfOpponentHand, newSizeOfBoneyard,
                action == Choice.Action.PICKED_UP && !isMyTurn);
    }

    private int getBoneChances(ImmutableBone bone) {
        for (Map.Entry<Integer,List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet())
            if (e.getValue().contains(bone))
                return e.getKey();

        throw new IllegalStateException("Not an unknown bone");
    }

    private static void setBoneAsKnown(ImmutableBone bone, Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone) {
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values()) {
            if (boneList.remove(bone))
                break;
        }
    }

    /**
     * Increases the chances of all bones that have had at least 'threshold' chances to be chosen, by 'value'.
     *
     * @param opponentChancesToHaveBone the map of bones to chances the opponent has had to pick them up.
     * @param threshold the number of chances required of each bone to qualify for increasing.  Must be at least 0.
     * @param value the value to add to the number of chances for each qualifying bone (can be negative, but the number
     *              of chances for any bone is at least zero).
     */
    private static void incrementBoneChancesAboveThreshold(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone, int threshold, int value) {
        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();
        if (threshold < 0)
            throw new IllegalArgumentException("Threshold must be at least 0");

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet()) {
            int newKey = e.getKey();
            if (e.getKey() >= threshold)
                newKey += value;
            if (newKey < 0)
                newKey = 0;

            // If the newKey is already in use, merge the bone lists.
            List<ImmutableBone> newBoneList = newOpponentChancesToHaveBone.get(newKey);
            if (newBoneList == null)
                newOpponentChancesToHaveBone.put(newKey, e.getValue());
            else
                newBoneList.addAll(e.getValue());
        }

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

    @Override
    public Map<ImmutableBone, Float> getOpponentBoneProbs() {
        return opponentBoneProbs;
    }
// {layoutLeft=3, layoutRight=6, myBones=[[2,1], [1,1], [4,1], [1,0], [2,4], [4,4], [0,0]],
    @Override
    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    @Override
    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    @Override
    public List<ImmutableBone> getUnknownBones() {
        return Collections.unmodifiableList(unknownBones);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("UnknownBoneManagerImpl");
        sb.append("{sizeOfBoneyard=").append(sizeOfBoneyard);
        sb.append(", isOpponentPickup=").append(isOpponentPickup);



        float total = 0;
        for (ImmutableBone bone : unknownBones) {
            float prob = opponentBoneProbs.get(bone);
            total += prob;
            sb.append("\n\t\t opponent has bone ").append(bone).append(" with prob = ").append(prob);
        }
        sb.append(", sizeOfOpponentHand=").append(sizeOfOpponentHand);
        sb.append(", sumOfProbs=").append(total);
        sb.append(", opponentChancesToHaveBone=").append(opponentChancesToHaveBone);
        sb.append('}');
        return sb.toString();
    }
}
