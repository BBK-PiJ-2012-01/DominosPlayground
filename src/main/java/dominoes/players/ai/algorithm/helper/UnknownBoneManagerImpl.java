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
    private final boolean isPickup;
    private final int sizeOfBoneyard;

    public UnknownBoneManagerImpl(List<ImmutableBone> unknownBones, int sizeOfOpponentHand) {
        this.sizeOfBoneyard = unknownBones.size() - sizeOfOpponentHand;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.unknownBones = unknownBones;
        isPickup = false;

        opponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        opponentChancesToHaveBone.put(sizeOfOpponentHand, unknownBones);

        opponentBoneProbs = calculateProbabilities();
    }

    private UnknownBoneManagerImpl(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone,
                                   int sizeOfOpponentHand, int sizeOfBoneyard, boolean isPickup) {
        this.opponentChancesToHaveBone = opponentChancesToHaveBone;
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.isPickup = isPickup;

        unknownBones = new ArrayList<ImmutableBone>(sizeOfBoneyard + sizeOfOpponentHand);
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values())
            unknownBones.addAll(boneList);

        opponentBoneProbs = calculateProbabilities();
    }

    private Map<ImmutableBone, Float> calculateProbabilities() {
        Map<ImmutableBone, Float> newOpponentBoneProbs = new HashMap<ImmutableBone, Float>();

        int largestNumberOfChances = Collections.max(opponentChancesToHaveBone.keySet());
        int thenAvailableBonesToPickup = 0;

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
            } else
                return this;

        } else {            // If not my turn...
            if (action.isPlacement()) {
                // If the opponent has just picked up:
                if (isPickup)
                    // Then we know that they picked up 'bone' (ie. the one they just placed).  We need to undo that pickup.
                    // All bones that matched the layout had 0 chance of being in opponent's hand.
                    // We want to decrement the other bones' chances.
                    incrementBoneChancesNotDefinitelyInBoneyard(newOpponentChancesToHaveBone, -1);

                setBoneAsKnown(bone, newOpponentChancesToHaveBone);
                newSizeOfOpponentHand -= 1;

            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newSizeOfOpponentHand += 1;

                // If opponent picked up, they can't have any bones containing layoutLeft or layoutRight,
                if (!isPickup)
                    // NB. I only need to do this for the first pickup, since subsequent pickups don't change the layout.
                    setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone, layoutLeft, layoutRight);

                // but they immediately pick up so must add a chance to every unknown bone that doesn't match the layout.
                if (!isPickup)
                    incrementBoneChancesNotMatchingLayout(newOpponentChancesToHaveBone, layoutLeft, layoutRight);
                else
                    // NB. if I've done this before, I now only need to increment bone chances that might be in the opponents hand.
                    incrementBoneChancesNotDefinitelyInBoneyard(newOpponentChancesToHaveBone, +1);

                // (NB. if it turns out that the picked-up bone DID match the layout, the opponent must immediately place
                // it, so we'll deal with that in the next UnknownBoneManager object).

            } else if (action == Choice.Action.PASS) {
                if (!isPickup)
                    // If opponent passed, they can't have any bones containing layoutLeft or layoutRight
                    setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone, layoutLeft, layoutRight);
            }
        }

        return new UnknownBoneManagerImpl(newOpponentChancesToHaveBone, newSizeOfOpponentHand, newSizeOfBoneyard,
                action == Choice.Action.PICKED_UP);
    }

    private static void setBoneAsKnown(ImmutableBone bone, Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone) {
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values()) {
            if (boneList.remove(bone))
                break;
        }
    }

    private static void incrementBoneChancesNotMatchingLayout(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone,
                                                              int layoutLeft, int layoutRight) {
        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet()) {
            int oldKey = e.getKey();
            for (ImmutableBone bone : e.getValue()) {
                int newKey;
                if (bone.matches(layoutLeft) || bone.matches(layoutRight))
                    newKey = oldKey;
                else
                    newKey = oldKey + 1;

                List<ImmutableBone> newBoneList = newOpponentChancesToHaveBone.get(newKey);
                if (newBoneList == null) {
                    newBoneList = new LinkedList<ImmutableBone>();
                    newOpponentChancesToHaveBone.put(newKey, newBoneList);
                }

                newBoneList.add(bone);
            }
        }

        opponentChancesToHaveBone.clear();
        opponentChancesToHaveBone.putAll(newOpponentChancesToHaveBone);
    }

    private static void incrementBoneChancesNotDefinitelyInBoneyard(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone, int value) {
        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet()) {
            int newKey;
            if (e.getKey() == 0)
                newKey = 0;
            else
                newKey = e.getKey() + value;

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
        sb.append(", isPickup=").append(isPickup);
        sb.append(", sizeOfOpponentHand=").append(sizeOfOpponentHand);
        sb.append(", opponentChancesToHaveBone=").append(opponentChancesToHaveBone);
        sb.append('}');
        return sb.toString();
    }
}
