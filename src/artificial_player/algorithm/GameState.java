package artificial_player.algorithm;

import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.CopiedBone;
import artificial_player.algorithm.helper.MoveCounter;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.StateEnumerator;

import java.util.*;
import static artificial_player.algorithm.helper.Choice.Action;


public class GameState {
    public static enum Status { NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF }

    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;
    private final int sizeOfOpponentHand;
    private final int sizeOfBoneyard;
    private final double value;
    private final Set<CopiedBone> myBones;
    private final LinkedList<CopiedBone> layout;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState previous;
    private final Choice choiceTaken;
    private final Map<Choice,GameState> validChoices = new HashMap<Choice, GameState>();
    private Status status = Status.NOT_YET_CALCULATED;

    private int ply;

    public GameState(StateEnumerator stateEnumerator, HandEvaluator handEvaluator, MoveCounter moveCounter,
                     int initialPly, Set<CopiedBone> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
        this.moveCounter = moveCounter;

        layout = new LinkedList<CopiedBone>();
        sizeOfOpponentHand = myBones.size();
        sizeOfBoneyard = Bones.getAllBones().size() - 2 * sizeOfOpponentHand;
        previous = null;
        moveNumber = 0;
        choiceTaken = null;

        value = handEvaluator.evaluateInitialValue(this);
        ply = initialPly;
    }

    public Map<Choice,GameState> getValidChoices() {
        lazyChoicesInitialisation();
        return Collections.unmodifiableMap(validChoices);
    }

    public Set<CopiedBone> getMyBones() {
        return Collections.unmodifiableSet(myBones);
    }

    private GameState createNextState(Choice choice) {
        return new GameState(this, choice);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public GameState getPrevious() {
        return previous;
    }

    public int getPly() {
        return ply;
    }

    public void setPly(int ply) {
        this.ply = ply;
    }

    private GameState(GameState previous, Choice choiceTaken) {
        this.myBones = new HashSet<CopiedBone>(previous.myBones);
        this.layout = new LinkedList<CopiedBone>(previous.layout);
        this.choiceTaken = choiceTaken;
        this.isMyTurn = !previous.isMyTurn;
        this.moveNumber = previous.moveNumber + 1;
        this.moveCounter = previous.moveCounter;
        this.previous = previous;
        this.ply = previous.ply;
        this.handEvaluator = previous.handEvaluator;
        this.stateEnumerator = previous.stateEnumerator;

        this.value = handEvaluator.addedValueFromChoice(choiceTaken, previous);
        ply = previous.getPly();

        // Set sizeOfOpponentHand, sizeOfBoneyard, myBones, and layout

        if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
            sizeOfBoneyard = previous.getSizeOfBoneyard();
            if (choiceTaken.getAction() == Action.PLACED_RIGHT)
                layout.addLast(choiceTaken.getBone());
            else
                layout.addFirst(choiceTaken.getBone());
        } else if (choiceTaken.getAction() == Action.PICKED_UP) {
            sizeOfBoneyard = previous.getSizeOfBoneyard() - 1;
        } else if (choiceTaken.getAction() == Action.PASS)
            sizeOfBoneyard = previous.sizeOfBoneyard;
        else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        if (previous.isMyTurn()) {
            sizeOfOpponentHand = previous.getSizeOfOpponentHand();
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                myBones.remove(choiceTaken.getBone());
            else if (choiceTaken.getAction() == Action.PICKED_UP)
                myBones.add(choiceTaken.getBone());
            else if (choiceTaken.getAction() != Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        } else {
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand() - 1;
            else if (choiceTaken.getAction() == Action.PICKED_UP)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand() + 1;
            else if (choiceTaken.getAction() == Action.PASS)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand();
            else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public Status getDesiredStatus() {
        if (status == Status.IS_LEAF)
            return Status.IS_LEAF;
        if (moveCounter.getMovesPlayed() + ply > moveNumber)
            return Status.HAS_CHILD_STATES;
        if (moveCounter.getMovesPlayed() + ply <= moveNumber)
            return Status.NOT_YET_CALCULATED;

        throw new RuntimeException("getDesiredStatus broke");
    }

    public void lazyChoicesInitialisation() {
        Status desired_status = getDesiredStatus();
        if (desired_status == status)
            return;

        if (desired_status == Status.HAS_CHILD_STATES) {
            Set<Choice> validChoicesList = new HashSet<Choice>();

            if (isMyTurn) {
                if (!myBones.isEmpty())
                    validChoicesList = stateEnumerator.getMyValidChoices(layout, myBones, getPossibleOpponentBones());
            } else {
                if (sizeOfOpponentHand != 0)
                    validChoicesList = stateEnumerator.getOpponentValidChoices(layout, getPossibleOpponentBones(), sizeOfBoneyard);
            }

            for (Choice choice : validChoicesList)
                validChoices.put(choice, createNextState(choice));

            if (validChoices.isEmpty())
                setStatus(Status.IS_LEAF);
            else if (choiceTaken != null
                    && choiceTaken.getAction() == Action.PASS
                    && validChoices.size() == 1
                    && validChoices.containsKey(new Choice(Action.PASS, null)))
                setStatus(Status.IS_LEAF);
            else
                setStatus(Status.HAS_CHILD_STATES);
        }
    }

    public double getValue() {
        return value;
    }

    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    public double probThatOpponentHasBone() {
        // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone is low
        int total_possible_opponent_bones = sizeOfBoneyard + sizeOfOpponentHand;

        if (total_possible_opponent_bones == 0)
            return 0;
        else
            return ( (double) sizeOfOpponentHand) / total_possible_opponent_bones;
    }

    public Set<CopiedBone> getPossibleOpponentBones() {
        Set<CopiedBone> possible_opponent_bones = new HashSet<CopiedBone>(Bones.getAllBones());
        possible_opponent_bones.removeAll(myBones);
        possible_opponent_bones.removeAll(layout);
        return possible_opponent_bones;
    }

    @Override
    public String toString() {
        return String.format("%s %s , now value = %.1f , i have %d, opponent has %d, boneyard has %d%n\t%s",
                (isMyTurn ? "opponent" : "I"), getChoiceTaken(), getValue(), myBones.size(),
                sizeOfOpponentHand, sizeOfBoneyard, layout.toString());
    }

    public Choice getChoiceTaken() {
        return choiceTaken;
    }

    public MoveCounter getMoveCounter() {
        return moveCounter;
    }
}