package artificial_player.algorithm.helper;

import java.util.*;


public class BoneStateIntegerImpl implements BoneState {
    private final Map<ImmutableBone, Integer> opponentChancesToHaveBone;
    private final int sizeOfBoneyard, sizeOfOpponentHand, layoutLeft, layoutRight, totalChances;
    private final List<ImmutableBone> myBones, unknownBonesCache;

    public BoneStateIntegerImpl(List<ImmutableBone> myBones) {
        this.myBones = Collections.unmodifiableList(myBones);
        this.sizeOfOpponentHand = myBones.size();

        opponentChancesToHaveBone = new HashMap<ImmutableBone, Integer>();
        layoutLeft = -1;
        layoutRight = -1;

        List<ImmutableBone> possibleOpponentBones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);
        unknownBonesCache = Collections.unmodifiableList(possibleOpponentBones);

        sizeOfBoneyard = possibleOpponentBones.size() - sizeOfOpponentHand;

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        for (ImmutableBone bone : possibleOpponentBones)
            opponentChancesToHaveBone.put(bone, sizeOfOpponentHand);

        totalChances = calculateTotalChances();
    }

    private int calculateTotalChances() {
        int totalChances = 0;
        for (Integer chancesForBone : opponentChancesToHaveBone.values())
            totalChances += chancesForBone;
        return totalChances;
    }

    @Override
    public BoneState createNext(Choice choiceTaken, boolean isMyTurn) {
        int newSizeOfOpponentHand = sizeOfOpponentHand;
        int newSizeOfBoneyard = sizeOfBoneyard;
        int newLayoutLeft = layoutLeft;
        int newLayoutRight = layoutRight;
        Map<ImmutableBone, Integer> newOpponentChancesToHaveBone = new HashMap<ImmutableBone, Integer>(opponentChancesToHaveBone);
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
                newOpponentChancesToHaveBone.remove(bone);
            } else if (action != Choice.Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        } else {            // If not my turn...
            if (action.isPlacement()) {
                newOpponentChancesToHaveBone.remove(bone);
                newSizeOfOpponentHand -= 1;
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newSizeOfOpponentHand += 1;

                // If opponent picked up, they can't have any bones containing layoutLeft or layoutRight,
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone);
                // but they immediately pick up so must add a chance to every unknown bone.
                for (Map.Entry<ImmutableBone, Integer> e : newOpponentChancesToHaveBone.entrySet())
                    e.setValue(e.getValue() + 1);

            } else if (action == Choice.Action.PASS) {
                // If opponent passed, they can't have any bones containing layoutLeft or layoutRight
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone);
            } else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }

        return new BoneStateIntegerImpl(newOpponentChancesToHaveBone, newMyBones,
                newSizeOfBoneyard, newSizeOfOpponentHand, newLayoutLeft, newLayoutRight);
    }

    private void setBonesMatchingLayoutToBoneyard(Map<ImmutableBone, Integer> opponentChancesToHaveBone) {
        for (Map.Entry<ImmutableBone, Integer> e : opponentChancesToHaveBone.entrySet())
            if (e.getKey().matches(layoutLeft) || e.getKey().matches(layoutRight))
                e.setValue(0);
    }

    private BoneStateIntegerImpl(Map<ImmutableBone, Integer> opponentChancesToHaveBone, List<ImmutableBone> myBones,
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

        totalChances = calculateTotalChances();
        unknownBonesCache = Collections.unmodifiableList(new ArrayList<ImmutableBone>(opponentChancesToHaveBone.keySet()));
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
        if (!opponentChancesToHaveBone.containsKey(bone))
            throw new RuntimeException("Bone " + bone +" was not in unknown bones list: " + opponentChancesToHaveBone +
                    "\n was it in myBones? " + myBones.contains(bone));
        System.out.format("Bone %s had %d chances to be in opponent hand, and map size is %d%n",
                bone.toString(), opponentChancesToHaveBone.get(bone), opponentChancesToHaveBone.size());
        System.out.format("\tsizeOfOpponentHand = %d , totalChances = %d%n", sizeOfOpponentHand, totalChances);
        return (double) opponentChancesToHaveBone.get(bone) * sizeOfOpponentHand / totalChances ;
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
