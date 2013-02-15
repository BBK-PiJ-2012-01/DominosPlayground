package artificial_player.algorithm;

import artificial_player.algorithm.helper.*;
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
    private final double value;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState parent;
    private final Choice choiceTaken;
    private final BoneState boneState;

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
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;

        moveCounter = new MoveCounter(minPly);
        parent = null;
        moveNumber = 0;
        choiceTaken = null;
        boneState = new BoneStateIntegerImpl(myBones);

        value = handEvaluator.evaluateInitialValue(boneState);
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
        this.choiceTaken = choiceTaken;
        this.isMyTurn = !parent.isMyTurn;
        this.moveNumber = parent.moveNumber + 1;
        this.moveCounter = parent.moveCounter;
        this.parent = parent;
        this.extraPly = parent.extraPly;
        this.handEvaluator = parent.handEvaluator;
        this.stateEnumerator = parent.stateEnumerator;

        this.boneState = parent.boneState.createNext(choiceTaken, parent.isMyTurn());

        Choice previousChoice = parent.getChoiceTaken();
        boolean lastChoiceWasPass = previousChoice == null ? false : previousChoice.getAction() == Action.PASS;
        this.value = parent.getValue() + handEvaluator.addedValueFromChoice(parent.getBoneState(), parent.isMyTurn(),
                lastChoiceWasPass, choiceTaken);
        extraPly = Math.max(parent.extraPly - 1, 0);
    }

    /**
     * Gets all valid choices from this GameState.
     *
     * @return all valid choices from this GameState.
     */
    private List<Choice> getValidChoices() {
        if (isMyTurn)
            return stateEnumerator.getMyValidChoices(boneState);
        else
            return stateEnumerator.getOpponentValidChoices(boneState);
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
            if (boneState.getSizeOfOpponentHand() == 0)
                childStates.clear();

            // If I have placed all of my bones, it's game over
            if (boneState.getMyBones().isEmpty())
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
                    "\n possibleOpponentBones = " + boneState.getUnknownBones() +
                    "\n childStates.size() = " + getChildStates().size()) ;
        }

        moveCounter.incrementMovesPlayed();
        return chosenState;
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
    public BoneState getBoneState() {
        return boneState;
    }

    @Override
    public String toString() {
        return String.format("%s %s , now value = %.1f , i have %d, opponent has %d, boneyard has %d%n",
                (isMyTurn ? "opponent" : "I"), choiceTaken, getValue(), boneState.getMyBones().size(),
                boneState.getSizeOfOpponentHand(), boneState.getSizeOfBoneyard());
    }
}