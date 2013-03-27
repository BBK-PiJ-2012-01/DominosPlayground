package dominoes.players;

import dominoes.Bone;
import dominoes.CantPlayException;
import dominoes.Play;
import dominoes.Table;
import dominoes.players.ai.CleverPlayer;
import dominoes.players.ai.algorithm.components.StateEnumeratorImpl;
import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Bones;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 26/03/2013
 * Time: 13:45
 */
public class ExampleCleverPlayer extends CleverPlayer {
    private static final Comparator<ImmutableBone> boneWeightComparator = new Comparator<ImmutableBone>(){
        @Override
        public int compare(ImmutableBone o1, ImmutableBone o2) {
            return Integer.compare(o2.weight(), o1.weight());
        }
    };

    private int points = 0;
    private String name;
    private List<ImmutableBone> myBones = new LinkedList<ImmutableBone>();

    private Choice lastChoice;
    private List<Choice> opponentsLastChoices;
    private final boolean verbose;

    public ExampleCleverPlayer() {
        this(true);
    }

    public ExampleCleverPlayer(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public Play makeCleverPlay(Table table, List<Choice> opponentsLastChoices, BoneState boneState) throws CantPlayException {
        if (isFirstMove())
            Collections.sort(myBones, boneWeightComparator);

        if (verbose) System.out.println("Player " + name + " makeCleverPlay()");
        this.opponentsLastChoices = opponentsLastChoices;

        // Process opponent's last choices:
        if (verbose) System.out.println("\tOpponent choices were: " + opponentsLastChoices);

        // Check probabilities of unknown bones (ie. bones that are either in the opponent's hand OR in the boneyard)
        for (ImmutableBone bone : boneState.getUnknownBones()) {
            Bone originalBone = bone.cloneAsBone();     // This is how to convert back to the Bone class
            if (verbose) System.out.format("\tOpponent has %f chance of having bone %s%n", boneState.getProbThatOpponentHasBone(bone), bone);
        }

        // Make my play
        Play myPlay = null;
        ImmutableBone placedBone = null;
        for (ImmutableBone myBone : myBones) {
            placedBone = myBone;
            Bone bone = myBone.cloneAsBone();

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
            myBones.remove(placedBone);
            lastChoice = new Choice(myPlay);

            // What is the boneState after I make this placement?
            BoneState nextState = boneState.createNext(lastChoice, true);

            // What can the opponent do next?
            if (verbose) {
                System.out.println("\tThe opponent can then do:");
                for (Choice choice : new StateEnumeratorImpl().getOpponentValidChoices(nextState))
                    System.out.println("\t\t" + choice);
            }

            return myPlay;
        } else {
            throw new CantPlayException();
        }
    }

    @Override
    public void takeBoneFromBoneyard(Bone bone) {
        myBones.add(new ImmutableBone(bone));
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
        return Bones.convertToBoneArray(myBones);
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
