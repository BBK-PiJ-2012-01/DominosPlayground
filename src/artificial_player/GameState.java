package artificial_player;

import java.util.*;


public class GameState {
    public static enum Action { PLACED_RIGHT, PLACED_LEFT, PICKED_UP, PASS }
    public static enum Status { NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF }
    private static final Set<Bone2> allBones;

    static {
        // Enumerate all bones
        Set<Bone2> tempAllBones = new HashSet<Bone2>();
        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                tempAllBones.add(new Bone2(i, j));
            }
        }
        allBones = Collections.unmodifiableSet(tempAllBones);
    }

    public static Set<Bone2> getAllBones() {
        Set<Bone2> tempAllBones = new HashSet<Bone2>();

        for (Bone2 bone : allBones) {
            tempAllBones.add(new Bone2(bone));
        }

        return tempAllBones;
    }

    private final StateEnumerator stateEnumerator;

    private final HandEvaluator handEvaluator;
    private final int sizeOfOpponentHand;
    private final int sizeOfBoneyard;
    private final double value;
    private final Set<Bone2> myBones;
    private final LinkedList<Bone2> layout;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final MoveCounter moveCounter;
    private final GameState previous;
    private final Choice choiceTaken;
    private final Map<Choice,GameState> validChoices = new HashMap<Choice, GameState>();
    private Status status = Status.NOT_YET_CALCULATED;

    private int ply;
    public GameState(StateEnumerator stateEnumerator, HandEvaluator handEvaluator, MoveCounter moveCounter,
                     int initialPly, Set<Bone2> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.stateEnumerator = stateEnumerator;
        this.handEvaluator = handEvaluator;
        this.moveCounter = moveCounter;

        layout = new LinkedList<Bone2>();
        sizeOfOpponentHand = myBones.size();
        sizeOfBoneyard = allBones.size() - 2 * sizeOfOpponentHand;
        previous = null;
        moveNumber = 0;
        choiceTaken = null;

        value = handEvaluator.evaluateInitialValue(this);
        ply = initialPly;
    }

    public Set<Bone2> getMyBones() {
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

    public Map<Choice,GameState> getValidChoices() {
        lazyChoicesInitialisation();
        return Collections.unmodifiableMap(validChoices);
    }

    public int getPly() {
        return ply;
    }

    public void setPly(int ply) {
        this.ply = ply;
    }

    private GameState(GameState previous, Choice choiceTaken) {
        this.myBones = new HashSet<Bone2>(previous.myBones);
        this.layout = new LinkedList<Bone2>(previous.layout);
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

    public Set<Bone2> getPossibleOpponentBones() {
        Set<Bone2> possible_opponent_bones = new HashSet<Bone2>(getAllBones());
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