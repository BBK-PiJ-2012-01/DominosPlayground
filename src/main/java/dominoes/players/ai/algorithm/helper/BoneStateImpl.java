package dominoes.players.ai.algorithm.helper;

import java.util.*;


public class BoneStateImpl implements BoneState {
    private static final int INITIAL_LAYOUT_SIZE = 1;
    private static final int INITIAL_HAND_SIZE = 7;

    private final int layoutLeft, layoutRight;
    private final List<ImmutableBone> myBones;
    private final UnknownBoneManager unknownBoneManager;

    public BoneStateImpl(List<ImmutableBone> myBones, int sizeOfBoneyard, ImmutableBone... initialLayout) {
        this.myBones = new ArrayList<ImmutableBone>(myBones);

        if (initialLayout.length == 0) {
            layoutLeft = -1;
            layoutRight = -1;
        } else {
            layoutLeft = initialLayout[0].left();
            layoutRight = initialLayout[initialLayout.length-1].right();
        }

        List<ImmutableBone> unknownBones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        unknownBones.removeAll(myBones);
        unknownBones.removeAll(Arrays.asList(initialLayout));

        unknownBoneManager = new UnknownBoneManagerImpl(unknownBones, sizeOfBoneyard);
    }

    public BoneStateImpl(List<ImmutableBone> myBones, UnknownBoneManager unknownBoneManager, int layoutLeft, int layoutRight) {
        this.myBones = myBones;
        this.unknownBoneManager = unknownBoneManager;
        this.layoutLeft = layoutLeft;
        this.layoutRight = layoutRight;
    }

    @Override
    public BoneState createNext(Choice choiceTaken, boolean isMyTurn) {
        int newLayoutLeft = layoutLeft;
        int newLayoutRight = layoutRight;

        List<ImmutableBone> newMyBones = new ArrayList<ImmutableBone>(myBones);
        UnknownBoneManager nextUnknownBoneManager = unknownBoneManager.createNext(choiceTaken, isMyTurn, layoutLeft, layoutRight);

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

        // Update my bones
        if (isMyTurn) {
            if (action.isPlacement())
                newMyBones.remove(bone);
            else if (action == Choice.Action.PICKED_UP)
                newMyBones.add(bone);
        }

        return new BoneStateImpl(newMyBones, nextUnknownBoneManager, newLayoutLeft, newLayoutRight);
    }

    @Override
    public int getSizeOfBoneyard() {
        return unknownBoneManager.getSizeOfBoneyard();
    }

    @Override
    public int getSizeOfOpponentHand() {
        return unknownBoneManager.getSizeOfOpponentHand();
    }

    @Override
    public List<ImmutableBone> getMyBones() {
        return Collections.unmodifiableList(myBones);
    }

    @Override
    public List<ImmutableBone> getUnknownBones() {
        return unknownBoneManager.getUnknownBones();
    }

    @Override
    public float getProbThatOpponentHasBone(ImmutableBone bone) {
        return unknownBoneManager.getOpponentBoneProbs().get(bone);
    }

    @Override
    public float getProbThatBoneyardHasBone(ImmutableBone bone) {
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
        final StringBuilder sb = new StringBuilder();
        sb.append("BoneStateImpl");
        sb.append("{layoutLeft=").append(layoutLeft);
        sb.append(", layoutRight=").append(layoutRight);
        sb.append(", myBones=").append(myBones);
        sb.append(", unknownBoneManager=").append(unknownBoneManager);
        sb.append('}');
        return sb.toString();
    }
}
