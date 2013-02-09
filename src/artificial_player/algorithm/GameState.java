package artificial_player.algorithm;

import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.MoveCounter;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.StateEnumerator;

import java.util.*;
import static artificial_player.algorithm.helper.Choice.Action;


public class GameState {
    public Choice getChoiceTaken() {
        return choiceTaken;
    }

    public static enum Status { NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF }

    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;
    private final int sizeOfOpponentHand;
    private final int sizeOfBoneyard;
    private final double value;
    private final Set<ImmutableBone> myBones;
    private final Set<ImmutableBone> layout;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState previous;
    private final Choice choiceTaken;
    private final Map<Choice,GameState> validChoices = new HashMap<Choice, GameState>();
    private final int layoutLeft, layoutRight;

    private Status status = Status.NOT_YET_CALCULATED;
    private int ply;

    public GameState(StateEnumerator stateEnumerator, HandEvaluator handEvaluator, MoveCounter moveCounter,
                     int initialPly, Set<ImmutableBone> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
        this.moveCounter = moveCounter;

        layout = new HashSet<ImmutableBone>();
        layoutLeft = -1;
        layoutRight = -1;
        sizeOfOpponentHand = myBones.size();
        sizeOfBoneyard = Bones.getAllBones().size() - 2 * sizeOfOpponentHand;
        previous = null;
        moveNumber = 0;
        choiceTaken = null;

        value = handEvaluator.evaluateInitialValue(this);
        ply = initialPly;
    }

    private GameState(GameState previous, Choice choiceTaken) {
        this.myBones = new HashSet<ImmutableBone>(previous.myBones);
        this.layout = new HashSet<ImmutableBone>(previous.layout);
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

            // Update layout end values.

            boolean onRight = choiceTaken.getAction() == Action.PLACED_RIGHT;
            ImmutableBone bone = choiceTaken.getBone();

            if (layout.isEmpty()) {
                layoutLeft = bone.left();
                layoutRight = bone.right();
            } else {
                int oldValue = onRight ? previous.getLayoutRight() : previous.getLayoutLeft();
                int newValue = (bone.left() == oldValue) ? bone.right() : bone.left();
                if (onRight) {
                    layoutLeft = previous.getLayoutLeft();
                    layoutRight = newValue;
                } else {
                    layoutLeft = newValue;
                    layoutRight = previous.getLayoutRight();
                }
            }

            layout.add(bone);

        } else if (choiceTaken.getAction() == Action.PICKED_UP) {
            layoutLeft = previous.getLayoutLeft();
            layoutRight = previous.getLayoutRight();
            sizeOfBoneyard = previous.getSizeOfBoneyard() - 1;
        } else if (choiceTaken.getAction() == Action.PASS) {
            layoutLeft = previous.getLayoutLeft();
            layoutRight = previous.getLayoutRight();
            sizeOfBoneyard = previous.sizeOfBoneyard;
        } else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

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

        if (sizeOfBoneyard < 0) {
            throw new RuntimeException("Size of boneyard < 0 (inside GameState initialiser) because of " +
                    (previous.isMyTurn() ? "my" : "else") + " choice: " + choiceTaken);
        }

        if (sizeOfOpponentHand < 0) {
            throw new RuntimeException("Size of opponent hand < 0 (inside GameState initialiser) because of " +
                    (previous.isMyTurn() ? "my" : "opponent's") + " choice: " + choiceTaken);
        }
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

    private void lazyChoicesInitialisation() {
        Status desired_status = getDesiredStatus();
        if (desired_status == status)
            return;

        if (desired_status == Status.HAS_CHILD_STATES) {
            Set<Choice> validChoicesList;

            if (isMyTurn)
                validChoicesList = stateEnumerator.getMyValidChoices(this);
            else
                validChoicesList = stateEnumerator.getOpponentValidChoices(this);

            for (Choice choice : validChoicesList)
                validChoices.put(choice, createNextState(choice));

            // If this is the second pass in a row, it's game over
            if (choiceTaken != null                                        // Not the first move
                    && validChoices.size() == 1                                 // There is only one possible choice
                    && validChoices.containsKey(new Choice(Action.PASS, null))  // and that choice is PASS
                    && choiceTaken.getAction() == Action.PASS)                  // and the previous move was also a PASS
                validChoices.clear();

            // If the opponent has placed all of their bones, it's game over
            if (sizeOfOpponentHand == 0)
                validChoices.clear();

            // If I have placed all of my bones, it's game over
            if (myBones.isEmpty())
                validChoices.clear();

            if (validChoices.isEmpty())
                status = Status.IS_LEAF;
            else
                status = Status.HAS_CHILD_STATES;
        }
    }

    public Map<Choice,GameState> getValidChoices() {
        lazyChoicesInitialisation();
        return Collections.unmodifiableMap(validChoices);
    }

    public Set<ImmutableBone> getPossibleOpponentBones() {
        Set<ImmutableBone> possible_opponent_bones = new HashSet<ImmutableBone>(Bones.getAllBones());
        possible_opponent_bones.removeAll(myBones);
        possible_opponent_bones.removeAll(layout);
        return possible_opponent_bones;
    }

    public double probThatOpponentHasBone() {
        // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone is low
        int total_possible_opponent_bones = sizeOfBoneyard + sizeOfOpponentHand;

        if (total_possible_opponent_bones == 0)
            return 0;
        else
            return ( (double) sizeOfOpponentHand) / total_possible_opponent_bones;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public Set<ImmutableBone> getMyBones() {
        return Collections.unmodifiableSet(myBones);
    }

    private GameState createNextState(Choice choice) {
        return new GameState(this, choice);
    }

    public GameState getPrevious() {
        return previous;
    }

    public int getPly() {
        return ply;
    }

    public void increasePly(int plyIncrease) {
        ply += plyIncrease;
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

    public Set<ImmutableBone> getLayout() {
        return Collections.unmodifiableSet(layout);
    }

    public int getLayoutRight() {
        return layoutRight;
    }

    public int getLayoutLeft() {
        return layoutLeft;
    }

    @Override
    public String toString() {
        return String.format("%s %s , now value = %.1f , i have %d, opponent has %d, boneyard has %d%n",
                (isMyTurn ? "opponent" : "I"), choiceTaken, getValue(), myBones.size(),
                sizeOfOpponentHand, sizeOfBoneyard);
    }
}