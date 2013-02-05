package artificial_player;

import java.util.*;


public class GameState {
    public static final Set<Bone2> all_bones;
    private static final int PLY = 5;
    private static final int COST_OF_MY_PICKUP = 20;
    private static final int VALUE_OF_OPPONENT_PICKUP = 5;
    private static final Comparator<Map.Entry<Choice, GameState>> comp = new Comparator<Map.Entry<Choice, GameState>>() {
        @Override
        public int compare(Map.Entry<Choice, GameState> o1, Map.Entry<Choice, GameState> o2) {
            return compareStates(o1.getValue(), o2.getValue());
        }
    };

    private static final Comparator<Map.Entry<GameState, Choice>> inverse_comp = new Comparator<Map.Entry<GameState, Choice>>() {

        @Override
        public int compare(Map.Entry<GameState, Choice> o1, Map.Entry<GameState, Choice> o2) {
            return compareStates(o1.getKey(), o2.getKey());
        }
    };

    private static int compareStates(GameState s1, GameState s2) {
        return Double.compare(s1.getValue(), s2.getValue());
    }


    static {
        Set<Bone2> temp_all_bones = new HashSet<Bone2>();

        for (int i = 0; i < 7; ++i) {
            for (int j = 0; j < 7; ++j) {
                temp_all_bones.add(new Bone2(i, j, false));
            }
        }
        assert temp_all_bones.size() == 28;
        all_bones = Collections.unmodifiableSet(temp_all_bones);
    }

    public static enum Action {
        PLACED_RIGHT, PLACED_LEFT, PICKED_UP, PASS;
    }

    public static enum Status {
        NOT_YET_CALCULATED, HAS_CHILD_STATES, IS_LEAF;
    }

    private final int size_of_opponent_hand, size_of_boneyard;
    private final double value;
    private final Set<Bone2> my_bones;
    private final LinkedList<Bone2> placed_bones;
    private final Map<Choice,GameState> choices = new HashMap<Choice, GameState>();
    private final boolean my_turn;
    private final int move_number;
    private final Memo memo;
    private final GameState previous;
    private final Choice choice_taken;

    private  Status status = Status.NOT_YET_CALCULATED;

    public GameState(Set<Bone2> my_bones, boolean my_turn) {
        this.my_turn = my_turn;
        this.my_bones = my_bones;
        placed_bones = new LinkedList<Bone2>();
        size_of_opponent_hand = my_bones.size();
        size_of_boneyard = all_bones.size() - 2 * size_of_opponent_hand;
        previous = null;

        int my_hand_weight = 0;
        for (Bone2 bone : my_bones) {
            my_hand_weight += bone.weight();
            System.out.println("my bone: " + bone);
        }

        int opponent_hand_weight = 0;
        for (Bone2 bone : getPossibleOpponentBones()) {
            opponent_hand_weight += bone.weight();
            System.out.println("opponent bone: " + bone);
        }
        System.out.println("opponent hand size = " + size_of_opponent_hand);
        System.out.println("boneyard size = " + size_of_boneyard);
        System.out.println("prob that opponent has bone = " + probThatOpponentHasBone());
        System.out.println("opponent hand weight = " + opponent_hand_weight);
        System.out.println("my hand weight = " + my_hand_weight);

        // TODO: once probThatOpponentHasBone() is fixed, use it instead of '0.5' below.
        value = opponent_hand_weight * probThatOpponentHasBone() - my_hand_weight;
        System.out.println("starting value = " + value);
        move_number = 0;
        memo = new Memo();

        choice_taken = null;
    }

    private GameState createNextState(Action action, Bone2 bone) {
        return new GameState(this, new Choice(action, bone), bone);
    }

    private GameState(GameState previous, Choice choice_taken, Bone2 choice_taken_getBone) {
        this.my_bones = new HashSet<Bone2>(previous.my_bones);
        this.placed_bones = new LinkedList<Bone2>(previous.placed_bones);
        this.choice_taken = choice_taken;
        this.my_turn = !previous.my_turn;
        this.move_number = previous.move_number + 1;
        this.memo = previous.memo;
        this.previous = previous;

        // TODO: value of picking up should be average of all bones I or the opponent could pick up
        // (otherwise I'll keep assuming I can pick up the [0,0] bone).


        if (choice_taken.getAction() == Action.PLACED_RIGHT || choice_taken.getAction() == Action.PLACED_LEFT) {

            if (choice_taken.getBone().isMine()) {
                assert previous.my_turn;
                my_bones.remove(choice_taken.getBone());
                size_of_opponent_hand = previous.size_of_opponent_hand;
                value = previous.value + choice_taken.getBone().weight();
            } else {
                assert ! previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand - 1;
                value = previous.value - choice_taken.getBone().weight();
            }

            if (choice_taken.getAction() == Action.PLACED_RIGHT)
                placed_bones.add(choice_taken.getBone());
            else
                placed_bones.addFirst(choice_taken.getBone());

            size_of_boneyard = previous.size_of_boneyard;

        } else if (choice_taken.getAction() == Action.PICKED_UP) {

            double average_of_boneyard_cards = 0;
            for (Bone2 pickupable_bone : getPossibleOpponentBones()) {
                average_of_boneyard_cards += pickupable_bone.weight();
            }
            average_of_boneyard_cards /= previous.size_of_boneyard + previous.size_of_opponent_hand;

            if (choice_taken.getBone().isMine()) {
                assert previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand;
                my_bones.add(choice_taken.getBone());
                value = previous.value - average_of_boneyard_cards - COST_OF_MY_PICKUP;
            } else {
                assert ! previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand + 1;
                value = previous.value + average_of_boneyard_cards + VALUE_OF_OPPONENT_PICKUP;
            }
            size_of_boneyard = previous.size_of_boneyard - 1;

        } else if (choice_taken.getAction() == Action.PASS) {

            value = previous.value;
            size_of_opponent_hand = previous.size_of_opponent_hand;
            size_of_boneyard = previous.size_of_boneyard;

        } else {
            throw new RuntimeException("Unhandled action");
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

    public List<Map.Entry<Choice,GameState>> getNBestChoicesAndFinalStates(int N) {
        calculateAsNecessary();
        Map<GameState,Choice> choices_by_final_state = new HashMap<GameState, Choice>();
        //System.out.println("Getting best choices for state " + this);


        for (Map.Entry<Choice,GameState> e : choices.entrySet()) {
            // For each choice
            Choice choice = e.getKey();
            GameState next_state = e.getValue();
            //System.out.println("\tlooking into choice " + choice);

            next_state.calculateAsNecessary();

            if (next_state.status == Status.HAS_CHILD_STATES) {
                //System.out.println("\t\tlooking into children... " + choice);
                // For each of the best final states given this choice
                for (Map.Entry<Choice, GameState> e_child : next_state.getNBestChoicesAndFinalStates(N)) {
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

        int[] counter = new int[6];
        for (GameState final_state : choices_by_final_state.keySet()) {
            ++counter[final_state.move_number];
        }

        System.out.println("Counter: " + Arrays.toString(counter));

        // Find at most N best final states, but no more than were found.
        N = Math.min(N, choices_by_final_state.size());
        Map<Choice,GameState> best_choices = new HashMap<Choice, GameState>();

        for (int i = 0; i < N; ++i) {
            // Find the entry with the best final state (which is maximum value if my turn, and minimum value if not)
            Map.Entry<GameState, Choice> best_choice;
            if (my_turn)
                best_choice = Collections.max(choices_by_final_state.entrySet(), inverse_comp);
            else
                best_choice = Collections.min(choices_by_final_state.entrySet(), inverse_comp);

            // Add the entry to best_choices (but inverted)
            best_choices.put(best_choice.getValue(), best_choice.getKey());

            // Remove the entry from the original 'choices_by_final_state' so the next iteration will find the (i+1)th
            // best final state.
            choices_by_final_state.remove(best_choice.getKey());
        }

        // Create a list of these best options
        List<Map.Entry<Choice, GameState>> list_of_best_choices = new LinkedList<Map.Entry<Choice, GameState>>(best_choices.entrySet());

        // Best option should be at front of list.
        Collections.sort(list_of_best_choices, comp);
        if (my_turn)
            Collections.reverse(list_of_best_choices);

        return list_of_best_choices;

    }

    private Status getDesiredStatus() {
        if (status == Status.IS_LEAF)
            return Status.IS_LEAF;
        if (memo.getMovesPlayed() + PLY > move_number)
            return Status.HAS_CHILD_STATES;
        if (memo.getMovesPlayed() + PLY <= move_number)
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
        if (my_bones.isEmpty() || size_of_opponent_hand == 0) {
            status = Status.IS_LEAF;
            return;
        }

        // Otherwise, check for legal moves and the subsequent next_states.
        Set<GameState> next_states;

        if (my_turn) {
            next_states = getNextStates(my_bones);
            if (next_states.isEmpty()) {
                // No possible move - must pick up from boneyard
                for (Bone2 bone : getPossibleOpponentBones()) {
                    Bone2 bone_if_it_becomes_mine = new Bone2(bone);
                    bone_if_it_becomes_mine.setMine(true);
                    next_states.add(createNextState(Action.PICKED_UP, bone_if_it_becomes_mine));
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
            if (choice_taken.getAction() == Action.PASS) {
                // If the previous turn was also passed, then game-over and this is a leaf-state.
                status = Status.IS_LEAF;
                return;
            }
            next_states.add(createNextState(Action.PASS, null));
        }

        // Formulate the next_states into the 'choices' map
        for (GameState state : next_states) {
            choices.put(state.choice_taken, state);
        }

        status = Status.HAS_CHILD_STATES;
    }

    public GameState choose(Choice choice) {
        GameState next_state = choices.get(choice);
        memo.incrementMovesPlayed();
        return next_state;
    }

    public double getValue() {
        return value;
    }

    private double probThatOpponentHasBone() {
        // TODO: if opponent picks up with 1s on left and right, prob of having a 1 bone = 0
        int total_possible_opponent_bones = size_of_boneyard + size_of_opponent_hand;

        // TODO: this keeps returning 1.0 ...
        if (total_possible_opponent_bones == 0)
            return 0;
        else
            return ( (double) size_of_opponent_hand) / total_possible_opponent_bones;
    }

    private double probThatBoneYardHasBone() {
        return 1 - probThatOpponentHasBone();
    }

    private Set<GameState> getOpponentNextStates() {
        return getNextStates(getPossibleOpponentBones());
    }

    private Set<Bone2> getPossibleOpponentBones() {
        Set<Bone2> possible_opponent_bones = new HashSet<Bone2>(all_bones);
        possible_opponent_bones.removeAll(my_bones);
        possible_opponent_bones.removeAll(placed_bones);
        return possible_opponent_bones;
    }

    private Set<GameState> getNextStates(Set<Bone2> available_bones) {
        Set<GameState> new_states = new HashSet<GameState>();

        if (placed_bones.isEmpty()) {
            // No bones have been placed (ie. this is the first move of the game)
            for (Bone2 bone : available_bones) {
                // Can place any of my bones
                new_states.add(createNextState(Action.PLACED_RIGHT, bone));
            }

        } else {
            // Bones have already been placed
            int right_val = placed_bones.getLast().right();
            int left_val = placed_bones.getFirst().left();

            for (Bone2 bone : available_bones) {
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
                "size_of_opponent_hand=" + size_of_opponent_hand +
                ", size_of_boneyard=" + size_of_boneyard +
                ", my_turn=" + my_turn +
                ", move_number=" + move_number +
                '}';
    }

    public void printMovesUpToFinalState(GameState final_state) {

        StringBuilder sbuilder = new StringBuilder(String.format("%n--- Choices (value = %.1f -> %.1f) ----%n", value, final_state.value));

        for (GameState next_state : getFutureStates(final_state)) {

            sbuilder.append(String.format("Move %d: %s %s , now value = %.1f%n\t%s%n",
                    next_state.move_number,
                    (next_state.my_turn ? "opponent chose" : "I chose"),
                    next_state.getChoiceTaken(), next_state.getValue(),
                    next_state.placed_bones.toString()));
        }

        System.out.println(sbuilder.toString());
    }

    public void printBestN(int N) {
        for (GameState final_state : getBestNFinalStates(N)) {
            printMovesUpToFinalState(final_state);
        }
    }

    public Choice getChoiceTaken() {
        return choice_taken;
    }

    private List<GameState> getFutureStates(GameState final_state) {
        LinkedList<GameState> list = new LinkedList<GameState>();

        do {
            list.addFirst(final_state);
            final_state = final_state.previous;
        } while(final_state.move_number != memo.getMovesPlayed());

        return list;
    }

}