package artificial_player;

import java.util.*;


public class GameState {
    public static final Set<Bone2> allBones;
    private static final int PLY = 4;
    private static final int COST_OF_MY_PICKUP = 20;
    private static final int VALUE_OF_OPPONENT_PICKUP = 5;
    private static final Comparator<Map.Entry<Choice, GameState>> comparator;
    private static final Comparator<Map.Entry<GameState, Choice>> inverseComparator;

    public static enum Action {
        PLACED_RIGHT, PLACED_LEFT, PICKED_UP, PASS
    }

    public static enum Status {
        NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF
    }

    static {
        // Enumerate all bones

        Set<Bone2> tempAllBones = new HashSet<Bone2>();

        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                tempAllBones.add(new Bone2(i, j));
            }
        }
        assert tempAllBones.size() == 28;
        allBones = Collections.unmodifiableSet(tempAllBones);

        // Setup the comparators
        comparator = new Comparator<Map.Entry<Choice, GameState>>() {
            @Override
            public int compare(Map.Entry<Choice, GameState> o1, Map.Entry<Choice, GameState> o2) {
                return compareStates(o1.getValue(), o2.getValue());
            }
        };

        inverseComparator = new Comparator<Map.Entry<GameState, Choice>>() {

            @Override
            public int compare(Map.Entry<GameState, Choice> o1, Map.Entry<GameState, Choice> o2) {
                return compareStates(o1.getKey(), o2.getKey());
            }
        };
    }

    private static int compareStates(GameState s1, GameState s2) {
        return Double.compare(s1.getValue(), s2.getValue());
    }

    public static Set<Bone2> getAllBones() {
        Set<Bone2> tempAllBones = new HashSet<Bone2>();

        for (Bone2 bone : allBones) {
            tempAllBones.add(new Bone2(bone));
        }

        return tempAllBones;
    }

    private final AIContainer aiContainer;
    private final int sizeOfOpponentHand;
    private final int sizeOfBoneyard;
    private final double value;
    private final Set<Bone2> myBones;
    private final LinkedList<Bone2> placedBones;
    private final boolean isMyTurn;
    private final int moveNumber;
    private final Memo memo;
    private final GameState previous;
    private final Choice choiceTaken;
    private final Map<Choice,GameState> choices = new HashMap<Choice, GameState>();

    private Status status = Status.NOT_YET_CALCULATED;
    private int extraPly;

    public GameState(AIContainer aiContainer, Set<Bone2> myBones, boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
        this.myBones = myBones;
        this.aiContainer = aiContainer;
        placedBones = new LinkedList<Bone2>();
        sizeOfOpponentHand = myBones.size();
        sizeOfBoneyard = allBones.size() - 2 * sizeOfOpponentHand;
        previous = null;
        moveNumber = 0;
        memo = new Memo();
        choiceTaken = null;

        value = aiContainer.getHandEvaluator().evaluateInitialValue(this);
    }

    public Set<Bone2> getMyBones() {
        return Collections.unmodifiableSet(myBones);
    }

    private GameState createNextState(Choice choice) {
        return new GameState(this, choice);
    }

    private GameState createNextState(Action action, Bone2 bone) {
        return createNextState(new Choice(action, bone));
    }

    private GameState(GameState previous, Choice choiceTaken) {
        this.myBones = new HashSet<Bone2>(previous.myBones);
        this.placedBones = new LinkedList<Bone2>(previous.placedBones);
        this.choiceTaken = choiceTaken;
        this.isMyTurn = !previous.isMyTurn;
        this.moveNumber = previous.moveNumber + 1;
        this.memo = previous.memo;
        this.previous = previous;
        this.extraPly = previous.extraPly;
        this.aiContainer = previous.aiContainer;
        this.value = aiContainer.getHandEvaluator().addedValueFromChoice(choiceTaken, previous);

        // Set sizeOfOpponentHand, sizeOfBoneyard, myBones, and placedBones

        if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT) {
            sizeOfBoneyard = previous.getSizeOfBoneyard();
            if (choiceTaken.getAction() == Action.PLACED_RIGHT)
                placedBones.addLast(choiceTaken.getBone());
            else
                placedBones.addFirst(choiceTaken.getBone());
        } else if (choiceTaken.getAction() == Action.PICKED_UP) {
            sizeOfBoneyard = previous.getSizeOfBoneyard() - 1;
        } else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());

        if (previous.isMyTurn()) {
            sizeOfOpponentHand = previous.getSizeOfOpponentHand();
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                myBones.remove(choiceTaken.getBone());
            else if (choiceTaken.getAction() == Action.PICKED_UP)
                myBones.add(choiceTaken.getBone());
            else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        } else {
            if (choiceTaken.getAction() == Action.PLACED_LEFT || choiceTaken.getAction() == Action.PLACED_RIGHT)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand() - 1;
            else if (choiceTaken.getAction() == Action.PICKED_UP)
                sizeOfOpponentHand = previous.getSizeOfOpponentHand() + 1;
            else throw new RuntimeException("Unhandled action: " + choiceTaken.getAction());
        }
    }

    public Choice getBestChoice() {
        // TODO: allow good choices to get higher ply.  Number of good choices to allow forward should depend on probThatOpponentHasBone()

        return getBestNChoices(3).get(0);
    }

    public GameState getBestFinalState() {
        return getBestNFinalStates(3).get(0);
    }

    public List<Choice> getBestNChoices(int N) {
        List<Choice> list_of_best_choices = new LinkedList<Choice>();

        for (Map.Entry<Choice,GameState> e : getNBestChoicesAndFinalStates(N)) {
            list_of_best_choices.add(e.getKey());
        }

        return list_of_best_choices;
    }

    public List<GameState> getBestNFinalStates(int N) {
        List<GameState> list_of_best_final_states = new LinkedList<GameState>();

        for (Map.Entry<Choice, GameState> e : getNBestChoicesAndFinalStates(N)) {
            list_of_best_final_states.add(e.getValue());
        }

        return list_of_best_final_states;
    }

    public void printBestAfterSelectivelyIncreasingPly(int N) {
        for (Map.Entry<Choice, GameState> e : getNBestChoicesAndFinalStates(N)) {
            e.getValue().extraPly += PLY;
        }

        printBestN(N);
    }

    public Map<Choice,GameState> getValidChoices() {
        return Collections.unmodifiableMap(choices);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isMyTurn() {
        return isMyTurn;
    }

    public List<Map.Entry<Choice,GameState>> getNBestChoicesAndFinalStates(int N) {
        // TODO: split this method into two: getAllBestChoicesAndFinalStates() and getBestChoiceAndFinalState()

        calculateAsNecessary();
        Map<GameState,Choice> choices_by_final_state = new HashMap<GameState, Choice>();
        //System.out.println("Getting best choices for state " + this);


        for (Map.Entry<Choice,GameState> e : choices.entrySet()) {
            // For each choice
            Choice choice = e.getKey();
            GameState next_state = e.getValue();
            //System.out.println("\tlooking into choice " + choice);

            //if (next_state.extraPly > 0)
            //    System.out.println("");
            next_state.calculateAsNecessary();

            if (next_state.status == Status.HAS_CHILD_STATES) {
                //System.out.println("\t\tlooking into children... " + choice);
                // For each of the best final states given this choice

                // Changed N to 1 here, because finding more than just the best route given this choice
                // means taking less-optimal routes (eg. the opponent doesn't chose the most devistating routes)
                // which is not as likely.
                // The point of this method is to get the most desirable next_states to pursue further.
                for (Map.Entry<Choice, GameState> e_child : next_state.getNBestChoicesAndFinalStates(1)) {
                    GameState final_state = e_child.getValue();
                    //System.out.println("\t\t\t...got recursive final state " + final_state);

                    // Store what final state is achievable given this choice
                    assert ! choices_by_final_state.containsKey(final_state);
                    choices_by_final_state.put(final_state, choice);
                }
            } else {
                // If the next_state is a final state (ie. a leaf or too unimportant to calculate)
                choices_by_final_state.put(next_state, choice);
                //System.out.println("\t\t\t...got final state " + next_state + " in state " + next_state.status);
            }
        }

//        int[] counter = new int[6];
//        for (GameState final_state : choices_by_final_state.keySet()) {
//            ++counter[final_state.moveNumber];
//        }
//
//        System.out.println("Counter: " + Arrays.toString(counter));

//        int[] counter = new int[2];
//        for (GameState final_state : choices_by_final_state.keySet()) {
//            ++counter[final_state.isMyTurn ? 1 : 0];
//        }

//        System.out.println("Counter: " + Arrays.toString(counter));

        // Find at most N best final states, but no more than were found.
        N = Math.min(N, choices_by_final_state.size());
        Map<Choice,GameState> best_choices = new HashMap<Choice, GameState>();

        for (int i = 0; i < N; ++i) {
            // Find the entry with the best final state (which is maximum value if my turn, and minimum value if not)
            Map.Entry<GameState, Choice> best_choice;
            if (isMyTurn)
                best_choice = Collections.max(choices_by_final_state.entrySet(), inverseComparator);
            else
                best_choice = Collections.min(choices_by_final_state.entrySet(), inverseComparator);

            // Add the entry to best_choices (but inverted)
            best_choices.put(best_choice.getValue(), best_choice.getKey());

            // Remove the entry from the original 'choices_by_final_state' so the next iteration will find the (i+1)th
            // best final state.
            choices_by_final_state.remove(best_choice.getKey());
        }

        // Create a list of these best options
        List<Map.Entry<Choice, GameState>> list_of_best_choices = new LinkedList<Map.Entry<Choice, GameState>>(best_choices.entrySet());

        // Best option should be at front of list.
        Collections.sort(list_of_best_choices, comparator);
        if (isMyTurn)
            Collections.reverse(list_of_best_choices);

        return list_of_best_choices;

    }

    private Status getDesiredStatus() {
        if (status == Status.IS_LEAF)
            return Status.IS_LEAF;
        if (memo.getMovesPlayed() + PLY + extraPly > moveNumber)
            return Status.HAS_CHILD_STATES;
        if (memo.getMovesPlayed() + PLY + extraPly <= moveNumber)
            return Status.NOT_YET_CALCULATED;

        throw new RuntimeException("getDesiredStatus broke");
    }

    public void calculateAsNecessary() {
        Status desired_status = getDesiredStatus();
        if (desired_status == status)
            return;

        if (desired_status == Status.HAS_CHILD_STATES)
            calculateChildren();
    }

    public void calculateChildren() {
        // If either player has now played all of their bones, this is a leaf state in the tree.
        if (myBones.isEmpty() || sizeOfOpponentHand == 0) {
            status = Status.IS_LEAF;
            return;
        }

        // Otherwise, check for legal moves and the subsequent next_states.
        Set<GameState> next_states;

        if (isMyTurn) {
            next_states = getNextStates(myBones);
            if (next_states.isEmpty()) {
                // No possible move - must pick up from boneyard
                for (Bone2 bone : getPossibleOpponentBones()) {
                    next_states.add(createNextState(Action.PICKED_UP, bone));
                }
            }

        } else {
            // Assuming the opponent can place a bone
            Set<Bone2> possible_opponent_bones = getPossibleOpponentBones();
            next_states = getNextStates(possible_opponent_bones);

            // Assuming the opponent can't place a bone
            for (Bone2 bone : possible_opponent_bones) {
                next_states.add(createNextState(Action.PICKED_UP, bone));
            }
        }

        if (next_states.isEmpty()) {
            // If no valid moves were found, then this turn is passed.
            if (choiceTaken.getAction() == Action.PASS) {
                // If the previous turn was also passed, then game-over and this is a leaf-state.
                status = Status.IS_LEAF;
                return;
            }
            next_states.add(createNextState(Action.PASS, null));
        }

        // Formulate the next_states into the 'choices' map
        for (GameState state : next_states) {
            choices.put(state.choiceTaken, state);
        }

        status = Status.HAS_CHILD_STATES;
    }

    public GameState choose(Choice choice) {
        GameState next_state = choices.get(choice);
        if (next_state == null) {
            next_state = createNextState(choice);
        }
        memo.incrementMovesPlayed();
        return next_state;
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
        // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone = 0
        int total_possible_opponent_bones = sizeOfBoneyard + sizeOfOpponentHand;

        // TODO: this keeps returning 1.0 ...
        if (total_possible_opponent_bones == 0)
            return 0;
        else
            return ( (double) sizeOfOpponentHand) / total_possible_opponent_bones;
    }

    public double probThatBoneYardHasBone() {
        return 1 - probThatOpponentHasBone();
    }

    public Set<Bone2> getPossibleOpponentBones() {
        Set<Bone2> possible_opponent_bones = new HashSet<Bone2>(getAllBones());
        possible_opponent_bones.removeAll(myBones);
        possible_opponent_bones.removeAll(placedBones);
        return possible_opponent_bones;
    }

    private Set<GameState> getNextStates(Set<Bone2> availableBones) {
        Set<GameState> new_states = new HashSet<GameState>();

        if (placedBones.isEmpty()) {
            // No bones have been placed (ie. this is the first move of the game)
            for (Bone2 bone : availableBones) {
                // Can place any of my bones
                new_states.add(createNextState(Action.PLACED_RIGHT, bone));
            }

        } else {
            // Bones have already been placed
            int right_val = placedBones.getLast().right();
            int left_val = placedBones.getFirst().left();

            for (Bone2 bone : availableBones) {
                // Check right/last of placed bones
                if (right_val == bone.left()) {
                    new_states.add(createNextState(Action.PLACED_RIGHT, bone));
                } else if (right_val == bone.right()) {
                    Bone2 flipped_bone = new Bone2(bone);
                    flipped_bone.flip();
                    new_states.add(createNextState(Action.PLACED_RIGHT, flipped_bone));
                }

                // Check left/first of placed bones
                if (left_val == bone.right()) {
                    new_states.add(createNextState(Action.PLACED_LEFT, bone));
                } else if (left_val == bone.left()) {
                    Bone2 flipped_bone = new Bone2(bone);
                    flipped_bone.flip();
                    new_states.add(createNextState(Action.PLACED_LEFT, flipped_bone));
                }
            }
        }

        return new_states;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "sizeOfOpponentHand=" + sizeOfOpponentHand +
                ", sizeOfBoneyard=" + sizeOfBoneyard +
                ", isMyTurn=" + isMyTurn +
                ", moveNumber=" + moveNumber +
                '}';
    }

    public void printMovesUpToFinalState(GameState finalState) {

        StringBuilder sbuilder = new StringBuilder(String.format("%n--- Choices (value = %.1f -> %.1f) ----%n", value, finalState.value));

        for (GameState next_state : getFutureStates(finalState)) {

            sbuilder.append(String.format("Move %d: %s %s , now value = %.1f%n\t%s%n",
                    next_state.moveNumber,
                    (next_state.isMyTurn ? "opponent" : "I"),
                    next_state.getChoiceTaken(), next_state.getValue(),
                    next_state.placedBones.toString()));
        }

        System.out.println(sbuilder.toString());
    }

    public void printBestN(int N) {
        for (GameState final_state : getBestNFinalStates(N)) {
            printMovesUpToFinalState(final_state);
        }
    }

    public Choice getChoiceTaken() {
        return choiceTaken;
    }

    private List<GameState> getFutureStates(GameState finalState) {
        LinkedList<GameState> list = new LinkedList<GameState>();

        do {
            list.addFirst(finalState);
            finalState = finalState.previous;
        } while(finalState.moveNumber != memo.getMovesPlayed());

        return list;
    }

}