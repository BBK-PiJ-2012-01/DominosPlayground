package dominoes.players.ai;

import dominoes.*;
import dominoes.players.ai.algorithm.helper.*;

import java.util.List;


/**
 * A CleverPlayer is an ObservantPlayer who uses his observations to work out probabilistically
 * what bones are in the opponent's hand, and which are in the boneyard.
 *
 * @author Sam Wright
 */
public abstract class CleverPlayer extends ObservantPlayer {
    private BoneState boneState;
    private BoneState beforeMyTurn, afterMyTurn;


    public BoneState getBeforeMyTurn() {
        return beforeMyTurn;
    }

    public BoneState getAfterMyTurn() {
        return afterMyTurn;
    }

    @Override
    public Play makeObservantPlay(Table table, List<Choice> opponentsLastChoices) throws CantPlayException {
        if (isFirstMove()) {
            List<ImmutableBone> initialHand = Bones.convertToImmutableBoneList(bonesInHand());
            List<ImmutableBone> var = Bones.convertToImmutableBoneList(getInitialLayout());
            ImmutableBone[] initialLayout = var.toArray(new ImmutableBone[var.size()]);
            boneState = new BoneStateImpl(initialHand, getBoneYard().size(), initialLayout);
        }

        for (Choice choice : opponentsLastChoices)
            boneState = boneState.createNext(choice, false);

        if (!isPickingUp())
            beforeMyTurn = boneState;
        Play play;

        try {
            play = makeCleverPlay(table, opponentsLastChoices, boneState);
            boneState = boneState.createNext(new Choice(play), true);
        } catch (CantPlayException e) {
            if (boneState.getSizeOfBoneyard() == 0) {
                boneState = boneState.createNext(new Choice(Choice.Action.PASS, null), true);
                afterMyTurn = boneState;
            }
            throw e;
        }

        afterMyTurn = boneState;

        return play;
    }

    public abstract Play makeCleverPlay(Table table, List<Choice> opponentsLastChoices, BoneState boneState) throws CantPlayException;

    public abstract void takeBoneFromBoneyard(Bone bone);

    @Override
    public final void draw(BoneYard boneYard) {
        super.draw(boneYard);

        Bone bone = boneYard.draw();
        if (!isFirstMove()) {
            boneState = boneState.createNext(new Choice(Choice.Action.PICKED_UP, new ImmutableBone(bone)), true);
            afterMyTurn = boneState;
        }

        takeBoneFromBoneyard(bone);
    }
}


