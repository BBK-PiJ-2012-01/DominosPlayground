package artificial_player.algorithm.helper;

import java.util.*;

/**
 * [0.5], 0.5, 0.5, 0.75, 0.75, 1.0, 1.0 (total = 5.0)
 * oldTotalExcludingFirst = 4.5
 * newTotalExcludingFirst = 5.0
 *
 * factor = 5 / 4.5 = 10 / 9 = 1.11111
 *
 * What is the probability of A being in hand given that bone B is not?
 *
 * I have:
 *  probability of A being in hand
 *  probability of B being in hand
 *
 * Maybe:
 *  (1 - prob(B)) * (1 - prob(A)) + prob(A)
 *
 * Which gives:
 *  [0.0], 0.75, 0.75, 0.875, 0.875, 1.0, 1.0 (total = 5.25)
 *
 * Transform to probabilities of bones being in BONEYARD (because I'm certain when
 * the bone is in the hand, but when in the boneyard the probability can change):
 * [0.5], 0.5, 0.5, 0.25, 0.25, 0.0, 0.0 (total = 2.0)
 *
 * What is the probability of A being in boneyard given that bone B definitely is?
 * Well, sizeOfBoneyard has decreases by prob(A), so all probs are multiplied by;
 *  (sizOfBoneyard - newProb(A)) / (sizeOfBoneyard - oldProb(A))
 *              = (2.0 - 1.0) / (2.0 - 0.5) = 2/3
 *
 * Which gives
 * [1.0], 0.3333, 0.3333, 0.1666, 0.1666, 0.0, 0.0 (total = [1] + 1)
 *
 * Convert back to probs of being in hand:
 * [0.0], 0.6666, 0.6666, 0.8333, 0.8333, 1.0, 1.0 (total = 5)
 *
 */
public class BoneManager {
    private final Map<ImmutableBone, Integer> opponentChancesToHaveBone;

    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    private final int sizeOfBoneyard, sizeOfOpponentHand, layoutLeft, layoutRight;
    private final List<ImmutableBone> myBones, layout;

    public BoneManager(List<ImmutableBone> myBones) {
        this.myBones = myBones;
        this.sizeOfOpponentHand = myBones.size();
        opponentChancesToHaveBone = new HashMap<ImmutableBone, Integer>();
        layout = Collections.emptyList();
        layoutLeft = -1;
        layoutRight = -1;

        List<ImmutableBone> possibleOpponentBones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);

        sizeOfBoneyard = possibleOpponentBones.size() - sizeOfOpponentHand;

        // The opponent takes a bone from the boneyard sizeOfOpponentHand times.
        for (ImmutableBone bone : possibleOpponentBones)
            opponentChancesToHaveBone.put(bone, sizeOfOpponentHand);
    }

    public BoneManager createNext(Choice choiceTaken, boolean isMyTurn) {
        int newSizeOfOpponentHand = sizeOfOpponentHand;
        int newSizeOfBoneyard = sizeOfBoneyard;
        int newLayoutLeft = layoutLeft;
        int newLayoutRight = layoutRight;
        Map<ImmutableBone, Integer> newOpponentChancesToHaveBone = new HashMap<ImmutableBone, Integer>(opponentChancesToHaveBone);
        List<ImmutableBone> newMyBones = new ArrayList<ImmutableBone>(myBones);
        List<ImmutableBone> newLayout = new ArrayList<ImmutableBone>(layout);

        Choice.Action action = choiceTaken.getAction();
        ImmutableBone bone = choiceTaken.getBone();

        // Update layout end values
        if (action.isPlacement()) {
            boolean onRight = choiceTaken.getAction() == Choice.Action.PLACED_RIGHT;

            if (layout.isEmpty()) {
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
                newLayout.add(bone);
                newMyBones.remove(bone);
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newMyBones.add(bone);
                newOpponentChancesToHaveBone.remove(bone);
            } else if (action != Choice.Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        } else {            // If not my turn...
            if (action.isPlacement()) {
                newLayout.add(bone);
                newOpponentChancesToHaveBone.remove(bone);
                newSizeOfOpponentHand -= 1;
            } else if (action == Choice.Action.PICKED_UP) {
                newSizeOfBoneyard -= 1;
                newSizeOfOpponentHand += 1;

                // If opponent picked up, they can't have any bones containing layoutLeft or layoutRight,
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone);
                // but they immediately pick up so must add a chance to every unknown bone.
                for (Map.Entry<ImmutableBone, Integer> e : opponentChancesToHaveBone.entrySet())
                    e.setValue(e.getValue() + 1);

            } else if (action == Choice.Action.PASS) {
                // If opponent passed, they can't have any bones containing layoutLeft or layoutRight
                setBonesMatchingLayoutToBoneyard(newOpponentChancesToHaveBone);
            } else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }

        return new BoneManager(newOpponentChancesToHaveBone, newMyBones, newLayout,
                newSizeOfBoneyard, newSizeOfOpponentHand, newLayoutLeft, newLayoutRight);
    }

    private void setBonesMatchingLayoutToBoneyard(Map<ImmutableBone, Integer> opponentChancesToHaveBone) {
        for (Map.Entry<ImmutableBone, Integer> e : opponentChancesToHaveBone.entrySet())
            if (e.getKey().matches(layoutLeft) || e.getKey().matches(layoutRight))
                e.setValue(0);
    }

    private void addChanceToAllUnknownBones(Map<ImmutableBone, Integer> opponentChancesToHaveBone) {
        for (Map.Entry<ImmutableBone, Integer> e : opponentChancesToHaveBone.entrySet())
            e.setValue( e.getValue() + 1 );
    }

    private BoneManager(Map<ImmutableBone, Integer> opponentChancesToHaveBone, List<ImmutableBone> myBones,
                        List<ImmutableBone> layout, int sizeOfBoneyard, int sizeOfOpponentHand, int layoutLeft, int layoutRight) {
        this.opponentChancesToHaveBone = opponentChancesToHaveBone;
        this.myBones = myBones;
        this.layout = layout;
        this.sizeOfBoneyard = sizeOfBoneyard;
        this.sizeOfOpponentHand = sizeOfOpponentHand;
        this.layoutLeft = layoutLeft;
        this.layoutRight = layoutRight;

        if (sizeOfBoneyard < 0)
            throw new RuntimeException("Size of boneyard < 0");

        if (sizeOfOpponentHand < 0)
            throw new RuntimeException("Size of opponent hand < 0");
    }

    public List<ImmutableBone> getMyBones() {
        return Collections.unmodifiableList(myBones);
    }

    public List<ImmutableBone> getLayout() {
        return Collections.unmodifiableList(layout);
    }

    public List<ImmutableBone> getUnknownBones() {
        return new ArrayList<ImmutableBone>(opponentChancesToHaveBone.keySet());
    }

    public double getProbThatOpponentHasBone(ImmutableBone bone) {
        return opponentChancesToHaveBone.get(bone) / opponentChancesToHaveBone.size();
    }

    public double getProbThatBoneyardHasBone(ImmutableBone bone) {
        return 1 - getProbThatOpponentHasBone(bone);
    }

    public int getLayoutLeft() {
        return layoutLeft;
    }

    public int getLayoutRight() {
        return layoutRight;
    }

}
