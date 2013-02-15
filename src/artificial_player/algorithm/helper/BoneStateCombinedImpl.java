package artificial_player.algorithm.helper;

import java.util.*;


public class BoneStateCombinedImpl implements BoneState {
    private final Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone;
    private final Map<ImmutableBone, Double> opponentBoneProbs;
    private final int sizeOfBoneyard, sizeOfOpponentHand, layoutLeft, layoutRight;
    private final List<ImmutableBone> myBones, unknownBonesCache;

    public BoneStateCombinedImpl(List<ImmutableBone> myBones) {
        this.myBones = new ArrayList<ImmutableBone>(myBones);
        this.sizeOfOpponentHand = myBones.size();

        opponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();
        layoutLeft = -1;
        layoutRight = -1;

        List<ImmutableBone> possibleOpponentBones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);
        unknownBonesCache = Collections.unmodifiableList(possibleOpponentBones);

        sizeOfBoneyard = possibleOpponentBones.size() - sizeOfOpponentHand;

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        opponentChancesToHaveBone.put(sizeOfOpponentHand, possibleOpponentBones);

        opponentBoneProbs = calculateProbabilities();
    }

    private Map<ImmutableBone, Double> calculateProbabilities() {
        Map<ImmutableBone, Double> newOpponentBoneProbs = new HashMap<ImmutableBone, Double>();

        // Start from 0 probability (ie. the initial state)
        for (ImmutableBone bone : unknownBonesCache)
            newOpponentBoneProbs.put(bone, 0.0);

        int largestNumberOfChances = Collections.max(opponentChancesToHaveBone.keySet());
//        List<ImmutableBone> boneListNotAbleToBePickedUp = opponentChancesToHaveBone.get(0);
//        int thenAvailableBonesToPickup = 21;
//        if (boneListNotAbleToBePickedUp != null)
//            thenAvailableBonesToPickup =- boneListNotAbleToBePickedUp.size();
        int thenAvailableBonesToPickup = 0;

        List<ImmutableBone> possibleBonesToTake = new LinkedList<ImmutableBone>();

        for (int i = largestNumberOfChances; i > 0; --i) {
            List<ImmutableBone> bonesNowAbleToBePickedUp = opponentChancesToHaveBone.get(i);
            if (bonesNowAbleToBePickedUp != null) {
                possibleBonesToTake.addAll(bonesNowAbleToBePickedUp);
                thenAvailableBonesToPickup += bonesNowAbleToBePickedUp.size();
            }

            for (ImmutableBone bone : possibleBonesToTake) {
                double probOpponentHasBone = newOpponentBoneProbs.get(bone);
                double probBoneyardHasBone = 1 - probOpponentHasBone;

                double newProbOpponentHasBone = probOpponentHasBone + probBoneyardHasBone / thenAvailableBonesToPickup;

                newOpponentBoneProbs.put(bone, newProbOpponentHasBone);
            }

            --thenAvailableBonesToPickup;
        }
//
//          List<ImmutableBone> boneListNotAbleToBePickedUp = opponentChancesToHaveBone.get(0);
//          if (boneListNotAbleToBePickedUp != null)
//              for (ImmutableBone bone : boneListNotAbleToBePickedUp)
//                  newOpponentBoneProbs.put(bone, 0.0);

//
//        if (thenAvailableBonesToPickup != sizeOfBoneyard)
//            System.out.println("Real size of boneyard = " + sizeOfBoneyard + " , thenSizeOfBoneyard = " + thenAvailableBonesToPickup);
//        assert thenSizeOfBoneyard == sizeOfBoneyard;

//        assertEquals(new HashSet(unknownBonesCache), newOpponentBoneProbs.keySet());

        return newOpponentBoneProbs;
    }

    private static void setBoneAsKnown(ImmutableBone bone, Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone) {
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values()) {
            if (boneList.remove(bone))
                break;
        }
    }

    @Override
    public BoneState createNext(Choice choiceTaken, boolean isMyTurn) {
        int newSizeOfOpponentHand = sizeOfOpponentHand;
        int newSizeOfBoneyard = sizeOfBoneyard;
        int newLayoutLeft = layoutLeft;
        int newLayoutRight = layoutRight;

        List<ImmutableBone> newMyBones = new ArrayList<ImmutableBone>(myBones);
        Map<Integer, List<ImmutableBone>> newOpponentChancesToHaveBone = new HashMap<Integer, List<ImmutableBone>>();

        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet())
            newOpponentChancesToHaveBone.put(e.getKey(), new LinkedList<ImmutableBone>(e.getValue()));

        Choice.Action action = choiceTaken.getAction();
        ImmutableBone bone = choiceTaken.getBone();

        // Update layout end values
        if (action.isPlacement()) {
            boolean onRight = action == Choice.Action.PLACED_RIGHT;

            if (isLayoutEmpty()) {
                newLayoutLeft = bone.left();
                newLayoutRight = bone.right();
            } else {
                int oldValue = onRight ? layoutRight : layoutLeft;
                int newValue = (bone.left() == oldValue) ? bone.right() : bone.left();
                if (onRight)
                    newLayoutRight = newValue;
                else
                    newLayoutLeft = newValue;
            }
        }

        // Update other values
        if (isMyTurn) {     // If my turn...
            if (action.isPlacement()) {
                newMyBones.remove(bone);
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newMyBones.add(bone);
//                newBoneChances.remove(bone);
//                incrementBoneChance(bone, newOpponentChancesToHaveBone);
                setBoneAsKnown(bone, newOpponentChancesToHaveBone);
            } else if (action != Choice.Action.PASS)
                throw new RuntimeException("Unhandled action: " + action);

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
            } else
                throw new RuntimeException("Unhandled action: " + action);
        }

        return new BoneStateCombinedImpl(newOpponentChancesToHaveBone, newMyBones,
                newSizeOfBoneyard, newSizeOfOpponentHand, newLayoutLeft, newLayoutRight);
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

    private BoneStateCombinedImpl(Map<Integer, List<ImmutableBone>> opponentChancesToHaveBone, List<ImmutableBone> myBones,
                                  int sizeOfBoneyard, int sizeOfOpponentHand, int layoutLeft, int layoutRight) {
        this.opponentChancesToHaveBone = opponentChancesToHaveBone;
        this.myBones = Collections.unmodifiableList(myBones);
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.layoutLeft = layoutLeft;
        this.layoutRight = layoutRight;

        if (sizeOfBoneyard < 0)
            throw new RuntimeException("Size of boneyard < 0");

        if (sizeOfOpponentHand < 0)
            throw new RuntimeException("Size of opponent hand < 0");

        unknownBonesCache = new LinkedList<ImmutableBone>();
        for (List<ImmutableBone> boneList : opponentChancesToHaveBone.values())
            unknownBonesCache.addAll(boneList);

        opponentBoneProbs = calculateProbabilities();
    }

    @Override
    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    @Override
    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    @Override
    public List<ImmutableBone> getMyBones() {
        return Collections.unmodifiableList(myBones);
    }

    @Override
    public List<ImmutableBone> getUnknownBones() {
        return new ArrayList<ImmutableBone>(opponentBoneProbs.keySet());
    }

    @Override
    public double getProbThatOpponentHasBone(ImmutableBone bone) {
        return opponentBoneProbs.get(bone);
    }

    @Override
    public double getProbThatBoneyardHasBone(ImmutableBone bone) {
        return 1 - getProbThatOpponentHasBone(bone);
    }

    @Override
    public int getLayoutLeft() {
        return layoutLeft;
    }

    @Override
    public int getLayoutRight() {
        return layoutRight;
    }

    @Override
    public boolean isLayoutEmpty() {
        return layoutLeft == -1;
    }

    @Override
    public String toString() {
        Map<Integer, Integer> counter = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, List<ImmutableBone>> e : opponentChancesToHaveBone.entrySet()) {
            counter.put(e.getKey(), e.getValue().size());
        }

        return "Number of chances each bone had: " + counter +
                "\n probabilities = " + opponentBoneProbs;


    }

}
