package artificial_player.algorithm.helper;

import java.util.*;


public class BoneStateDoubleImpl implements BoneState {
    private final Map<ImmutableBone, Double> opponentProbToHaveBone;
    private final int sizeOfBoneyard, sizeOfOpponentHand, layoutLeft, layoutRight;
    private final List<ImmutableBone> myBones, unknownBonesCache;

    public BoneStateDoubleImpl(List<ImmutableBone> myBones) {
        this.myBones = Collections.unmodifiableList(myBones);
        this.sizeOfOpponentHand = myBones.size();

        opponentProbToHaveBone = new HashMap<ImmutableBone, Double>();
        layoutLeft = -1;
        layoutRight = -1;

        List<ImmutableBone> possibleOpponentBones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);
        unknownBonesCache = Collections.unmodifiableList(possibleOpponentBones);

        sizeOfBoneyard = possibleOpponentBones.size() - sizeOfOpponentHand;

        double probThatOpponentTookBone = 1.0 * sizeOfOpponentHand / (sizeOfBoneyard + sizeOfOpponentHand);

        for (ImmutableBone bone : possibleOpponentBones)
            opponentProbToHaveBone.put(bone, probThatOpponentTookBone);

    }

    @Override
    public BoneState createNext(Choice choiceTaken, boolean isMyTurn) {
        int newSizeOfOpponentHand = sizeOfOpponentHand;
        int newSizeOfBoneyard = sizeOfBoneyard;
        int newLayoutLeft = layoutLeft;
        int newLayoutRight = layoutRight;
        Map<ImmutableBone, Double> newOpponentProbToHaveBone = new HashMap<ImmutableBone, Double>(opponentProbToHaveBone);
        List<ImmutableBone> newMyBones = new ArrayList<ImmutableBone>(myBones);

        Choice.Action action = choiceTaken.getAction();
        ImmutableBone bone = choiceTaken.getBone();

        // Update layout end values
        if (action.isPlacement()) {
            boolean onRight = choiceTaken.getAction() == Choice.Action.PLACED_RIGHT;

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
                setBoneAsKnown(bone, newOpponentProbToHaveBone);
            } else if (action != Choice.Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        } else {            // If not my turn...
            if (action.isPlacement()) {
                setBoneAsKnown(bone, newOpponentProbToHaveBone);
                newSizeOfOpponentHand -= 1;
                setOpponentHandSize(sizeOfOpponentHand, newSizeOfOpponentHand, newOpponentProbToHaveBone);
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;

                // If opponent picked up, they can't have any bones containing layoutLeft or layoutRight,
                putBonesMatchingLayoutInBoneyard(layoutLeft, layoutRight, newOpponentProbToHaveBone);
                // but they immediately pick up so must add a chance to every unknown bone.
                newSizeOfOpponentHand += 1;
                setOpponentHandSize(sizeOfOpponentHand, newSizeOfOpponentHand, newOpponentProbToHaveBone);

            } else if (action == Choice.Action.PASS) {
                // If opponent passed, they can't have any bones containing layoutLeft or layoutRight
                putBonesMatchingLayoutInBoneyard(layoutLeft, layoutRight, newOpponentProbToHaveBone);
            } else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }

        return new BoneStateDoubleImpl(newOpponentProbToHaveBone, newMyBones,
                newSizeOfBoneyard, newSizeOfOpponentHand, newLayoutLeft, newLayoutRight);
    }

    private static void setOpponentHandSize(int oldSize, int newSize, Map<ImmutableBone, Double> opponentProbToHaveBone) {
        double correctionFactor = 1.0 * newSize / oldSize;
        double extraProbPerBone = correctionFactor - 1.0;

        for (Map.Entry<ImmutableBone, Double> e : opponentProbToHaveBone.entrySet())
            //e.setValue(e.getValue() * correctionFactor);
            e.setValue(e.getValue() * (1 - extraProbPerBone) + extraProbPerBone);
    }

    private static void putBonesMatchingLayoutInBoneyard(int layoutLeft, int layoutRight, Map<ImmutableBone, Double> opponentProbToHaveBone) {
        int sizeOfUnknownBones = opponentProbToHaveBone.size();
        double totalGainedProb = 0;

        for (Map.Entry<ImmutableBone, Double> e : opponentProbToHaveBone.entrySet()) {
            if (e.getKey().matches(layoutLeft) || e.getKey().matches(layoutRight)) {
                totalGainedProb += e.getValue();
                e.setValue(0.0);
            }
        }

        double extraProbPerBone = totalGainedProb / sizeOfUnknownBones;

        for (Map.Entry<ImmutableBone, Double> e : opponentProbToHaveBone.entrySet())
            if (!e.getKey().matches(layoutLeft) && !e.getKey().matches(layoutRight))
                // The bone will be in the opponent's hand if it was already, or it wasn't and was just picked
                //e.setValue(e.getValue() * (1 - extraProbPerBone) + extraProbPerBone);
                e.setValue(e.getValue() + extraProbPerBone);
    }



    private static void setBoneAsKnown(ImmutableBone bone, Map<ImmutableBone, Double> opponentProbToHaveBone) {
        int sizeOfUnknownBones = opponentProbToHaveBone.size();
        double gainedProbPerBone = opponentProbToHaveBone.remove(bone) / (sizeOfUnknownBones - 1);

        for (Map.Entry<ImmutableBone, Double> e : opponentProbToHaveBone.entrySet())
            // The bone will be in the opponent's hand if it was already, or it wasn't and was just picked
            e.setValue(e.getValue() * (1 - gainedProbPerBone) + gainedProbPerBone);
    }

    private void setBonesMatchingLayoutToBoneyard(Map<ImmutableBone, Integer> opponentChancesToHaveBone) {
        for (Map.Entry<ImmutableBone, Integer> e : opponentChancesToHaveBone.entrySet())
            if (e.getKey().matches(layoutLeft) || e.getKey().matches(layoutRight))
                e.setValue(0);
    }

    private BoneStateDoubleImpl(Map<ImmutableBone, Double> opponentProbToHaveBone, List<ImmutableBone> myBones,
                                int sizeOfBoneyard, int sizeOfOpponentHand, int layoutLeft, int layoutRight) {
        this.opponentProbToHaveBone = opponentProbToHaveBone;
        this.myBones = Collections.unmodifiableList(myBones);
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.layoutLeft = layoutLeft;
        this.layoutRight = layoutRight;

        if (sizeOfBoneyard < 0)
            throw new RuntimeException("Size of boneyard < 0");

        if (sizeOfOpponentHand < 0)
            throw new RuntimeException("Size of opponent hand < 0");

        unknownBonesCache = Collections.unmodifiableList(new ArrayList<ImmutableBone>(opponentProbToHaveBone.keySet()));
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
        return unknownBonesCache;
    }

    @Override
    public double getProbThatOpponentHasBone(ImmutableBone bone) {
        if (bone == null)
            throw new RuntimeException("Bone was null!");
        if (!opponentProbToHaveBone.containsKey(bone))
            throw new RuntimeException("Bone " + bone +" was not in unknown bones list: " + opponentProbToHaveBone +
                    "\n was it in myBones? " + myBones.contains(bone));
//        System.out.format("Bone %s had %d chances to be in opponent hand, and map size is %d%n",
//                bone.toString(), opponentProbToHaveBone.get(bone), opponentProbToHaveBone.size());
//        System.out.format("\tsizeOfOpponentHand = %d , totalChances = %d%n", sizeOfOpponentHand, totalChances);
        return opponentProbToHaveBone.get(bone);
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

}
