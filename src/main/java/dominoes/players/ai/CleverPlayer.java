package dominoes.players.ai;

import dominoes.*;
import dominoes.players.ai.algorithm.helper.*;

import java.util.List;


/**
 * User: Sam Wright
 * Date: 26/03/2013
 * Time: 12:55
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
            ImmutableBone[] initialLayout = Bones.convertToImmutableBoneList(getInitialLayout()).toArray(new ImmutableBone[0]);
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


