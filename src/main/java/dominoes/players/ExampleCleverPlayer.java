package dominoes.players;

import dominoes.Bone;
import dominoes.CantPlayException;
import dominoes.Play;
import dominoes.Table;
import dominoes.players.ai.CleverPlayer;
import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 26/03/2013
 * Time: 13:45
 */
public class ExampleCleverPlayer extends CleverPlayer {
    private int points = 0;
    private String name;
    private List<Bone> myBones = new ArrayList<Bone>();

    private Choice lastChoice;
    private List<Choice> opponentsLastChoices;

    @Override
    public Play makeCleverPlay(Table table, List<Choice> opponentsLastChoices, BoneState boneState) throws CantPlayException {
//    public Play makeObservantPlay(Table table, List<Choice> opponentsLastChoices) throws CantPlayException {
        System.out.println("Player " + name + " makeCleverPlay()");
        this.opponentsLastChoices = opponentsLastChoices;

        // Process opponent's last choices:
        System.out.println("\tOpponent choices were: " + opponentsLastChoices);

        // Check probabilities of unknown bones (ie. bones that are either in the opponent's hand OR in the boneyard)
        for (ImmutableBone bone : boneState.getUnknownBones()) {
            Bone originalBone = bone.cloneAsBone();     // This is how to convert back to the Bone class
            System.out.format("\tOpponent has %f chance of having bone %s%n", boneState.getProbThatOpponentHasBone(bone), bone);
        }

        // Make my play
        Play myPlay = null;
        for (Bone bone : myBones) {
            ImmutableBone myBone = new ImmutableBone(bone);

            if (myBone.matches(table.left())) {
                if (bone.left() == table.left())
                    bone.flip();
                assert bone.right() == table.left();
                myPlay = new Play(bone, Play.LEFT);
            } else if (myBone.matches(table.right())) {
                if (bone.right() == table.right())
                    bone.flip();
                assert bone.left() == table.right();
                myPlay = new Play(bone, Play.RIGHT);
            } else continue;

            break;
        }

        if (myPlay != null) {
            assert myBones.remove(myPlay.bone());
            lastChoice = new Choice(myPlay);
            return myPlay;
        } else {
            throw new CantPlayException();
        }
    }

    @Override
    public void takeBoneFromBoneyard(Bone bone) {
        myBones.add(bone);
        lastChoice = new Choice(Choice.Action.PICKED_UP, new ImmutableBone(bone));
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Shouldn't be called");
    }

    @Override
    public int numInHand() {
        return myBones.size();
    }

    @Override
    public Bone[] bonesInHand() {
        return myBones.toArray(new Bone[0]);
    }

    @Override
    public void newRound() {
        super.newRound();
        myBones.clear();
        opponentsLastChoices = Collections.emptyList();
    }

    @Override
    public void setPoints(int i) {
        points = i;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void setName(String s) {
        name = s;
    }

    @Override
    public String getName() {
        return name;
    }

    public Choice getLastChoice() {
        return lastChoice;
    }

    public List<Choice> getOpponentsLastChoices() {
        return opponentsLastChoices;
    }
}
