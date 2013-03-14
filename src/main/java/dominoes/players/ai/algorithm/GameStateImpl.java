package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.*;
import dominoes.players.ai.algorithm.components.HandEvaluator;
import dominoes.players.ai.algorithm.components.StateEnumerator;

import java.util.*;
import static dominoes.players.ai.algorithm.helper.Choice.Action;

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
     * @param initialLayout the bones which start in the layout.
     */
    public GameStateImpl(StateEnumerator stateEnumerator, HandEvaluator handEvaluator,
                         int minPly, List<ImmutableBone> myBones, boolean isMyTurn, ImmutableBone... initialLayout) {
        this.isMyTurn = isMyTurn;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;

        moveCounter = new MoveCounter(minPly);
        parent = null;
        moveNumber = 0;
        choiceTaken = null;
        boneState = new BoneStateImpl(myBones, initialLayout);

        value = handEvaluator.evaluateInitialValue(boneState);
        extraPly = 0;
    }

    /**
     * A helper function to clarify calls to the GameState(GameState previous, Choice choiceTaken) constructor.
     *
     * @param choice the choice taken.
     * @return the resulting GameState after applying the given choice to this state.
     */
    private GameStateImpl createNextState(Choice choice) {
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

            List<GameState> childStates;

            // If this is the second pass in a row, it's game over
            if (choiceTaken != null && choiceTaken.getAction() == Action.PASS
                    && parent.getChoiceTaken() != null && parent.getChoiceTaken().getAction() == Action.PASS)
                childStates = Collections.emptyList();

            // If the opponent has placed all of their bones, it's game over
            else if (boneState.getSizeOfOpponentHand() == 0)
                childStates = Collections.emptyList();

            // If I have placed all of my bones, it's game over
            else if (boneState.getMyBones().isEmpty())
                childStates = Collections.emptyList();

            else {
                childStates = new ArrayList<GameState>(validChoicesList.size());
                for (Choice choice : validChoicesList)
                    childStates.add(createNextState(choice));
            }

            if (childStates.isEmpty())
                status = Status.GAME_OVER;
            else
                status = Status.HAS_CHILD_STATES;

            this.childStates = Collections.unmodifiableList(childStates);
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
        return childStates;
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
            Route r = new Route(this);
            throw new RuntimeException("Choice was not valid: " + choice +
                    "\nValid choices were: " + validChoices +
                    "\nMy bones were: " + boneState.getMyBones() +
                    "\nStatus is " + status +
                    "\n possibleOpponentBones = " + boneState.getUnknownBones() +
                    "\n childStates.size() = " + getChildStates().size() +
                    "\n choice stack: " + r) ;
        }

        moveCounter.incrementMovesPlayed();
        return chosenState;
    }

    @Override
    public GameState skipFirstChoices(List<ImmutableBone> bonesOpponentPlaced, List<ImmutableBone> bonesIPickedUp) {
        assert moveNumber == 0;

        // Place the opponent's initial bone
        GameStateImpl afterChoicesState = this;

        int movesToSkip = bonesIPickedUp.size() * 2 + (isMyTurn() ? 0 : 1);

        // for each bone I've picked up, the opponent has either placed or picked up.
        for (int i = 0; i < movesToSkip; ++i) {
            if (afterChoicesState.isMyTurn()) {
                // My pickup:
                afterChoicesState = afterChoicesState.createNextState(new Choice(Action.PICKED_UP, bonesIPickedUp.get(i)));
            } else {
                // Opponent's move:
                if (bonesOpponentPlaced.isEmpty())
                    // Opponent has placed all his bones, so he picked up:
                    afterChoicesState = afterChoicesState.createNextState(new Choice(Action.PICKED_UP, null));
                else
                    // Opponent still has bones to place
                    afterChoicesState = afterChoicesState.createNextState(new Choice(Action.PLACED_RIGHT, bonesOpponentPlaced.remove(0)));
            }
        }

        assert afterChoicesState.isMyTurn();

        return afterChoicesState;
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