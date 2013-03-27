package dominoes.players;


import dominoes.*;
import dominoes.players.ai.ObservantPlayer;
import dominoes.players.ai.algorithm.AIBuilder;
import dominoes.players.ai.algorithm.AIController;
import dominoes.players.ai.algorithm.GameOverException;
import dominoes.players.ai.algorithm.helper.Bones;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:50
 */
public class AIPlayer extends ObservantPlayer {
    private final AIController ai = AIBuilder.createAI("ProbabilisticAI");
    private final List<ImmutableBone> initialHand = new ArrayList<ImmutableBone>();

    private int points = 0;
    private String name;

    public AIPlayer() {
        newRound();
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Wasn't expecting to takeBack a bone!");
    }

    @Override
    public Play makeObservantPlay(Table table, List<Choice> opponentsLastChoices) throws CantPlayException {
        if (isFirstMove()) {
            ImmutableBone[] initialLayout = Bones.convertToImmutableBoneList(getInitialLayout()).toArray(new ImmutableBone[0]);
            ai.setInitialState(initialHand, true, getBoneYard().size(), initialLayout);
        }


        System.out.println("Player " + getName());
        for (Choice choice : opponentsLastChoices) {
            System.out.println("\tbefore opponent choices: " + ai.getGameState().getBoneState());
            System.out.println("\tOpponent choice: " + choice);
            assert !ai.getGameState().isMyTurn();
            ai.choose(choice);
        }

        assert ai.getGameState().isMyTurn();

        // Find my best choice:
        Choice myChoice;
        try {
            myChoice = ai.getBestChoice();
        } catch (GameOverException e) {
            throw new CantPlayException();
        }
        System.out.println("\tchose to: " + myChoice);


        // But if I can't place, throw a CantPlayException
        if (!myChoice.getAction().isPlacement()) {
            // If I have to pass, do so.
            if (myChoice.getAction() == Choice.Action.PASS) {
                ai.choose(myChoice);
                System.out.println("\tafter my pass: " + ai.getGameState().getBoneState());
            }

            throw new CantPlayException();
        }

        System.out.println("\tafter my placement: " + ai.getGameState().getBoneState());

        // So now, the choice must be a placement
        ai.choose(myChoice);

        // and finally, convert to a Play object
        int matchingValue = (myChoice.getAction() == Choice.Action.PLACED_RIGHT)? table.right() : table.left();
        return myChoice.convertToPlay(matchingValue);
    }

    @Override
    public void draw(BoneYard boneYard) {
        super.draw(boneYard);
        ImmutableBone pickedUpBone = new ImmutableBone(boneYard.draw());

        if (isFirstMove())
            // Just add to initialHand (they'll be given to the AI when 'makePlay' is first called.
            initialHand.add(pickedUpBone);
        else {
            // Pick up
            assert ai.getGameState().isMyTurn();
            ai.choose(new Choice(Choice.Action.PICKED_UP, pickedUpBone));
            System.out.println("\tafter my pickup of "+pickedUpBone+": " + ai.getGameState().getBoneState());
        }
    }

    @Override
    public int numInHand() {
        return ai.getGameState().getBoneState().getMyBones().size();
    }

    @Override
    public Bone[] bonesInHand() {
        List<ImmutableBone> internalBones;

        try {
            internalBones = ai.getGameState().getBoneState().getMyBones();
        } catch (NullPointerException e) {
            internalBones = initialHand;
        }

        return Bones.convertToBoneArray(internalBones);
    }

    @Override
    public void newRound() {
        super.newRound();
        initialHand.clear();
    }

    @Override
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
