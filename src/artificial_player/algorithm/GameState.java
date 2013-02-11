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
    private final Set<ImmutableBone> possibleOpponentBones;
    private final Set<ImmutableBone> layout;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState previous;
    private final Choice choiceTaken;
    private final int layoutLeft, layoutRight;

    private List<GameState> childStates;
    private Status status = Status.NOT_YET_CALCULATED;
    private int ply;

    public GameState(StateEnumerator stateEnumerator, HandEvaluator handEvaluator,
                     int initialPly, Set<ImmutableBone> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;

        possibleOpponentBones = new HashSet<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);

        moveCounter = new MoveCounter();
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
        this.possibleOpponentBones = new HashSet<ImmutableBone>(previous.possibleOpponentBones);

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
            else if (choiceTaken.getAction() == Action.PICKED_UP) {
                myBones.add(choiceTaken.getBone());
                possibleOpponentBones.remove(choiceTaken.getBone());
            } else if (choiceTaken.getAction() != Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        } else {
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
                sizeOfOpponentHand = previous.getSizeOfOpponentHand() - 1;
                possibleOpponentBones.remove(choiceTaken.getBone());
            } else if (choiceTaken.getAction() == Action.PICKED_UP)
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

    private Set<Choice> getValidChoices() {
        if (isMyTurn)
            return stateEnumerator.getMyValidChoices(this);
        else
            return stateEnumerator.getOpponentValidChoices(this);
    }

    private void lazyChildrenInitialisation() {
        Status desired_status = getDesiredStatus();
        if (desired_status == status)
            return;

        if (desired_status == Status.HAS_CHILD_STATES) {
            Set<Choice> validChoicesList = getValidChoices();

            childStates = new ArrayList<GameState>(validChoicesList.size());

            for (Choice choice : validChoicesList)
                childStates.add( createNextState(choice) );

            // If this is the second pass in a row, it's game over
            if (choiceTaken != null && choiceTaken.getAction() == Action.PASS
                    && previous.getChoiceTaken() != null && previous.getChoiceTaken().getAction() == Action.PASS)
                childStates.clear();

            // If the opponent has placed all of their bones, it's game over
            if (sizeOfOpponentHand == 0)
                childStates.clear();

            // If I have placed all of my bones, it's game over
            if (myBones.isEmpty())
                childStates.clear();

            if (childStates.isEmpty())
                status = Status.IS_LEAF;
            else
                status = Status.HAS_CHILD_STATES;
        }
    }

    public List<GameState> getChildStates() {
        lazyChildrenInitialisation();
        return Collections.unmodifiableList(childStates);
    }

    public GameState choose(Choice choice) {
        GameState chosenState = null;

        if (status == Status.HAS_CHILD_STATES) {
            for (GameState childState : getChildStates()) {
                if (childState.getChoiceTaken().equals(choice)) {
                    chosenState = childState;
                    break;
                }
            }
        } else if (status == Status.NOT_YET_CALCULATED && getValidChoices().contains(choice)) {
            chosenState = createNextState(choice);
        }
//        } else if (status == Status.NOT_YET_CALCULATED) {
//            Set<Choice> validChoices = getValidChoices();
//            if (validChoices.contains(choice))
//                chosenState = createNextState(choice);
//            else
//                throw new RuntimeException("Could not create next state from choice: " + choice);
//        }

        if (chosenState == null) {
            Set<Choice> validChoices = new HashSet<Choice>();
            for (GameState childState : getChildStates()) {
                validChoices.add(childState.getChoiceTaken());
            }
            throw new RuntimeException("Choice was not valid: " + choice +
                    "\nValid choices were: " + validChoices + "\nStatus is " + status + "\n possibleOpponentBones = " + getPossibleOpponentBones());
        }

        moveCounter.incrementMovesPlayed();
        return chosenState;
    }

    public Set<ImmutableBone> getPossibleOpponentBones() {
//        Set<ImmutableBone> possible_opponent_bones = new HashSet<ImmutableBone>(Bones.getAllBones());
//        possible_opponent_bones.removeAll(myBones);
//        possible_opponent_bones.removeAll(layout);
//        return possible_opponent_bones;
        return Collections.unmodifiableSet(possibleOpponentBones);
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