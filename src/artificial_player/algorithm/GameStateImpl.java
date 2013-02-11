package artificial_player.algorithm;

import artificial_player.algorithm.helper.Bones;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;
import artificial_player.algorithm.helper.MoveCounter;
import artificial_player.algorithm.virtual.HandEvaluator;
import artificial_player.algorithm.virtual.StateEnumerator;

import java.util.*;
import static artificial_player.algorithm.helper.Choice.Action;

/**
 * Implementation of GameState, which uses lazy initialisation of child states.
 */
public class GameStateImpl implements GameState {

    private final StateEnumerator stateEnumerator;
    private final HandEvaluator handEvaluator;
    private final int sizeOfOpponentHand;
    private final int sizeOfBoneyard;
    private final double value;
    private final List<ImmutableBone> myBones;
    private final List<ImmutableBone> possibleOpponentBones;
    private final List<ImmutableBone> layout;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState parent;
    private final Choice choiceTaken;
    private final int layoutLeft, layoutRight;

    private List<GameState> childStates = Collections.emptyList();
    private Status status = Status.NOT_YET_CALCULATED;
    private int extraPly;

    /**
     * Creates an initial GameState (ie. at the beginning of the game, with an empty layout).
     *
     * @param stateEnumerator the StateEnumerator object to use to enumerate child states.
     * @param handEvaluator the HandEvaluator object to use to evaluate this and future hands.
     * @param minPly the initial extraPly to give to this and all child states.
     * @param myBones the bones I have been dealt.
     * @param isMyTurn true iff the first turn is mine.
     */
    public GameStateImpl(StateEnumerator stateEnumerator, HandEvaluator handEvaluator,
                         int minPly, List<ImmutableBone> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;

        possibleOpponentBones = new ArrayList<ImmutableBone>(Bones.getAllBones());
        possibleOpponentBones.removeAll(myBones);

        moveCounter = new MoveCounter(minPly);
        layout = Collections.emptyList();
        layoutLeft = -1;
        layoutRight = -1;
        sizeOfOpponentHand = myBones.size();
        sizeOfBoneyard = possibleOpponentBones.size() - sizeOfOpponentHand;
        parent = null;
        moveNumber = 0;
        choiceTaken = null;

        value = handEvaluator.evaluateInitialValue(this);
        extraPly = 0;
    }

    /**
     * A helper function to clarify calls to the GameState(GameState previous, Choice choiceTaken) constructor.
     *
     * @param choice the choice taken.
     * @return the resulting GameState after applying the given choice to this state.
     */
    private GameState createNextState(Choice choice) {
        return new GameStateImpl(this, choice);
    }

    /**
     * Creates a new GameState from applying a choice to a parent state.
     *
     * @param parent the state to base this new one off of.
     * @param choiceTaken the choice taken in going from 'parent' to this.
     */
    private GameStateImpl(GameStateImpl parent, Choice choiceTaken) {
        this.myBones = new ArrayList<ImmutableBone>(parent.myBones);
        this.layout = new ArrayList<ImmutableBone>(parent.layout);
        this.choiceTaken = choiceTaken;
        this.isMyTurn = !parent.isMyTurn;
        this.moveNumber = parent.moveNumber + 1;
        this.moveCounter = parent.moveCounter;
        this.parent = parent;
        this.extraPly = parent.extraPly;
        this.handEvaluator = parent.handEvaluator;
        this.stateEnumerator = parent.stateEnumerator;
        this.possibleOpponentBones = new ArrayList<ImmutableBone>(parent.possibleOpponentBones);

        this.value = handEvaluator.addedValueFromChoice(choiceTaken, parent);
        extraPly = Math.max(parent.extraPly - 1, 0);

        // Set sizeOfOpponentHand, sizeOfBoneyard, myBones, and layout

        if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
            sizeOfBoneyard = parent.getSizeOfBoneyard();

            // Update layout end values.

            boolean onRight = choiceTaken.getAction() == Action.PLACED_RIGHT;
            ImmutableBone bone = choiceTaken.getBone();

            if (layout.isEmpty()) {
                layoutLeft = bone.left();
                layoutRight = bone.right();
            } else {
                int oldValue = onRight ? parent.getLayoutRight() : parent.getLayoutLeft();
                int newValue = (bone.left() == oldValue) ? bone.right() : bone.left();
                if (onRight) {
                    layoutLeft = parent.getLayoutLeft();
                    layoutRight = newValue;
                } else {
                    layoutLeft = newValue;
                    layoutRight = parent.getLayoutRight();
                }
            }

            layout.add(bone);

        } else if (choiceTaken.getAction() == Action.PICKED_UP) {
            layoutLeft = parent.getLayoutLeft();
            layoutRight = parent.getLayoutRight();
            sizeOfBoneyard = parent.getSizeOfBoneyard() - 1;
        } else if (choiceTaken.getAction() == Action.PASS) {
            layoutLeft = parent.getLayoutLeft();
            layoutRight = parent.getLayoutRight();
            sizeOfBoneyard = parent.sizeOfBoneyard;
        } else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        if (parent.isMyTurn()) {
            sizeOfOpponentHand = parent.getSizeOfOpponentHand();
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                myBones.remove(choiceTaken.getBone());
            else if (choiceTaken.getAction() == Action.PICKED_UP) {
                myBones.add(choiceTaken.getBone());
                possibleOpponentBones.remove(choiceTaken.getBone());
            } else if (choiceTaken.getAction() != Action.PASS)
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        } else {
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
                sizeOfOpponentHand = parent.getSizeOfOpponentHand() - 1;
                possibleOpponentBones.remove(choiceTaken.getBone());
            } else if (choiceTaken.getAction() == Action.PICKED_UP)
                sizeOfOpponentHand = parent.getSizeOfOpponentHand() + 1;
            else if (choiceTaken.getAction() == Action.PASS)
                sizeOfOpponentHand = parent.getSizeOfOpponentHand();
            else
                throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }

        if (sizeOfBoneyard < 0) {
            throw new RuntimeException("Size of boneyard < 0 (inside GameState initialiser) because of " +
                    (parent.isMyTurn() ? "my" : "else") + " choice: " + choiceTaken);
        }

        if (sizeOfOpponentHand < 0) {
            throw new RuntimeException("Size of opponent hand < 0 (inside GameState initialiser) because of " +
                    (parent.isMyTurn() ? "my" : "opponent's") + " choice: " + choiceTaken);
        }
    }

    /**
     * Gets all valid choices from this GameState.
     *
     * @return all valid choices from this GameState.
     */
    private List<Choice> getValidChoices() {
        if (isMyTurn)
            return stateEnumerator.getMyValidChoices(this);
        else
            return stateEnumerator.getOpponentValidChoices(this);
    }

    /**
     * Lazily initialises childStates and updates the status.
     */
    private void lazyChildrenInitialisation() {
        Status desired_status = getStatus();
        if (desired_status == status)
            return;

        if (desired_status == Status.HAS_CHILD_STATES) {
            List<Choice> validChoicesList = getValidChoices();

            childStates = new ArrayList<GameState>(validChoicesList.size());

            for (Choice choice : validChoicesList)
                childStates.add( createNextState(choice) );

            // If this is the second pass in a row, it's game over
            if (choiceTaken != null && choiceTaken.getAction() == Action.PASS
                    && parent.getChoiceTaken() != null && parent.getChoiceTaken().getAction() == Action.PASS)
                childStates.clear();

            // If the opponent has placed all of their bones, it's game over
            if (sizeOfOpponentHand == 0)
                childStates.clear();

            // If I have placed all of my bones, it's game over
            if (myBones.isEmpty())
                childStates.clear();

            if (childStates.isEmpty())
                status = Status.GAME_OVER;
            else
                status = Status.HAS_CHILD_STATES;
        }
    }

    @Override
    public Status getStatus() {
        if (status == Status.GAME_OVER)
            return Status.GAME_OVER;
        if (moveCounter.getMovesPlayed() + moveCounter.getMinPly() + extraPly > moveNumber)
            return Status.HAS_CHILD_STATES;
        if (moveCounter.getMovesPlayed() + moveCounter.getMinPly() + extraPly <= moveNumber)
            return Status.NOT_YET_CALCULATED;

        throw new RuntimeException("getStatus broke");
    }

    @Override
    public List<GameState> getChildStates() {
        lazyChildrenInitialisation();
        return Collections.unmodifiableList(childStates);
    }

    @Override
    public GameState choose(Choice choice) {
        GameState chosenState = null;

        if (status == Status.HAS_CHILD_STATES) {
            for (GameState childState : getChildStates()) {
                if (childState.getChoiceTaken().equals(choice)) {
                    chosenState = childState;
                    break;
                }
            }
            //childStates.clear(); // To help GC  - TODO: does it work???

        } else if (status == Status.NOT_YET_CALCULATED && getValidChoices().contains(choice)) {
            chosenState = createNextState(choice);
        }

        if (chosenState == null) {
            Set<Choice> validChoices = new HashSet<Choice>();
            for (GameState childState : getChildStates()) {
                validChoices.add(childState.getChoiceTaken());
            }
            throw new RuntimeException("Choice was not valid: " + choice +
                    "\nValid choices were: " + validChoices + " (should be " + getValidChoices() + ")" +
                    "\nStatus is " + status +
                    "\n possibleOpponentBones = " + getPossibleOpponentBones() +
                    "\n childStates.size() = " + getChildStates().size()) ;
        }

        moveCounter.incrementMovesPlayed();
        return chosenState;
    }

    @Override
    public List<ImmutableBone> getPossibleOpponentBones() {
        return Collections.unmodifiableList(possibleOpponentBones);
    }

    @Override
    public Choice getChoiceTaken() {
        return choiceTaken;
    }

    @Override
    public boolean isMyTurn() {
        return isMyTurn;
    }

    @Override
    public List<ImmutableBone> getMyBones() {
        return Collections.unmodifiableList(myBones);
    }

    @Override
    public GameState getParent() {
        return parent;
    }

    @Override
    public void increasePly(int plyIncrease) {
        extraPly += plyIncrease;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public int getSizeOfOpponentHand() {
        return sizeOfOpponentHand;
    }

    @Override
    public int getSizeOfBoneyard() {
        return sizeOfBoneyard;
    }

    @Override
    public int getLayoutRight() {
        return layoutRight;
    }

    @Override
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