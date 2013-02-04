package artificial_player;

import java.util.*;


public class GameState {
    public static final Set<Bone2> all_bones;
    private static final int PLY = 5;

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

    private final int size_of_opponent_hand, size_of_boneyard;
    private final double value;
    private final Set<Bone2> my_bones;
    private final LinkedList<Bone2> placed_bones;
    private final Map<Choice,GameState> choices = new HashMap<Choice, GameState>();
    private final Bone2 bone;
    private final Action action;
    private final boolean my_turn;
    private final int move_number;
    private final Memo memo;

    private Heuristic heuristic;

    public GameState(Set<Bone2> my_bones, boolean my_turn) {
        this.my_turn = my_turn;
        this.my_bones = my_bones;
        placed_bones = new LinkedList<Bone2>();
        size_of_opponent_hand = my_bones.size();
        size_of_boneyard = all_bones.size() - 2 * size_of_opponent_hand;

        int my_hand_value = 0;
        for (Bone2 bone : my_bones) {
            my_hand_value += bone.weight();
            System.out.println("my bone: " + bone);
        }

        int opponent_hand_value = 0;
        for (Bone2 bone : getPossibleOpponentBones()) {
            opponent_hand_value += bone.weight();
            System.out.println("opponent bone: " + bone);
        }
        System.out.println("opponent hand size = " + size_of_opponent_hand);
        System.out.println("boneyard size = " + size_of_boneyard);
        System.out.println("prob that opponent has bone = " + probThatOpponentHasBone());
        System.out.println("opponent hand value = " + opponent_hand_value);
        System.out.println("my hand value = " + my_hand_value);

        // TODO: once probThatOpponentHasBone() is fixed, use it instead of '0.5' below.
        value = opponent_hand_value * 0.5 - my_hand_value;
        System.out.println("starting value = " + value);
        move_number = 0;
        memo = new Memo();

        bone = null;
        action = null;
    }

    private GameState createNextState(Action action, Bone2 bone) {
        return new GameState(this, action, bone);
    }

    private GameState(GameState previous, Action action, Bone2 bone) {
        this.my_bones = new HashSet<Bone2>(previous.my_bones);
        this.placed_bones = new LinkedList<Bone2>(previous.placed_bones);
        this.action = action;
        this.bone = bone;
        this.my_turn = !previous.my_turn;
        this.move_number = previous.move_number + 1;
        this.memo = previous.memo;

        // TODO: value of picking up should be average of all bones I or the opponent could pick up
        // (otherwise I'll keep assuming I can pick up the [0,0] bone).


        if (action == Action.PLACED_RIGHT || action == Action.PLACED_LEFT) {

            if (bone.isMine()) {
                assert previous.my_turn;
                my_bones.remove(bone);
                size_of_opponent_hand = previous.size_of_opponent_hand;
                value = previous.value + bone.weight();
            } else {
                assert ! previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand - 1;
                value = previous.value - bone.weight();// * probThatOpponentHasBone();
            }

            if (action == Action.PLACED_RIGHT)
                placed_bones.add(bone);
            else
                placed_bones.addFirst(bone);

            size_of_boneyard = previous.size_of_boneyard;

        } else if (action == Action.PICKED_UP) {

            if (bone.isMine()) {
                assert previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand;
                my_bones.add(bone);
                value = previous.value - bone.weight();// * probThatBoneYardHasBone();
            } else {
                assert ! previous.my_turn;
                size_of_opponent_hand = previous.size_of_opponent_hand + 1;
                value = previous.value + bone.weight();// * probThatBoneYardHasBone();
            }
            size_of_boneyard = previous.size_of_boneyard - 1;

        } else if (action == Action.PASS) {

            value = previous.value;
            size_of_opponent_hand = previous.size_of_opponent_hand;
            size_of_boneyard = previous.size_of_boneyard;

        } else {
            throw new RuntimeException("Unhandled action");
        }
    }

    public Choice getBestChoice() {
        // TODO: allow good choices to get higher ply.  Number of good choices to allow forward should depend on probThatOpponentHasBone()

        Choice best_choice = getHeuristic().getChoice();
        for (Map.Entry<Choice,GameState> e : choices.entrySet()) {
            System.out.println(e.getKey() + " val: " + e.getValue().getValue() + " heuristic: " + e.getValue().getHeuristic());
        }
        return getHeuristic().getChoice();
    }

    public Heuristic getHeuristic() {
        // Lazy initialisation
        //System.out.format("Getting heuristic at level %d with %d moves played (ply is %d)%n", move_number, memo.getMovesPlayed(), PLY);
        if (heuristic == null) {
            if (memo.getMovesPlayed() + PLY > move_number) {
                // Fully calculate this heuristic
                heuristic = calculateHeuristic();
            } else if (memo.getMovesPlayed() + PLY == move_number) {
                // Partially create the heuristic (ie. treat it as a leaf,
                // but once more moves are made it will be recalculated fully.
                heuristic = new InterimHeuristic(my_turn, this);
            } else {
                throw new RuntimeException("Shouldn't be calculating the heuristic of such a deep layer...");
            }
        } else {
            // If heuristic has been created before, check if it needs recalculating
            if (memo.getMovesPlayed() + PLY > move_number && heuristic instanceof InterimHeuristic) {
                // Recalculate heuristic fully
                heuristic = calculateHeuristic();
            }
        }

        return heuristic;
    }

    private Heuristic calculateHeuristic() {
        // If either player has now played all of their bones, this is a leaf state in the tree.
        if (my_bones.isEmpty() || size_of_opponent_hand == 0)
            return new LeafHeuristic(my_turn, this);

        // Otherwise, check for legal moves and the subsequent next_states.
        boolean probabilistic = false;
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
                probabilistic = true;
            }

        } else {
            // Assuming the opponent can place a bone
            Set<Bone2> possible_opponent_bones = getPossibleOpponentBones();
            next_states = getNextStates(possible_opponent_bones);

            // Assuming the opponent can't place a bone, which implies they don't
            // have a valid bone to place:
            int left = placed_bones.getFirst().left();
            int right = placed_bones.getLast().right();


            // Remove placeable bones
            Set<Bone2> unplaceable_opponent_bones = new HashSet<Bone2>();
            for (Bone2 bone : possible_opponent_bones) {
                if (!bone.matchesNumber(left) && !bone.matchesNumber(right)) {
                    unplaceable_opponent_bones.add(bone);
                }
            }

            for (Bone2 bone : possible_opponent_bones) {
                next_states.add(createNextState(Action.PICKED_UP, bone));
            }

            // Take the pessimistic view that the opponent will always be able
            // to do the most devastating action possible.
        }

        // If no valid moves were found, then this turn is passed.
        if (next_states.isEmpty()) {
            // If the previous turn was also passed, then game-over and this is a leaf-state.
            if (action == Action.PASS) {
                return new LeafHeuristic(my_turn, this);
            }
            next_states.add(createNextState(Action.PASS, null));
        }

        // Formulate the next_states into the 'choices' map
        for (GameState state : next_states) {
            choices.put(new Choice(state.action, state.bone), state);
        }

        Heuristic heuristic;

        // Create heuristics for choices
        if (probabilistic) {
            heuristic = new AverageHeuristic(my_turn, choices);
        }
        else {
            heuristic = new DeterministicHeuristic(my_turn, choices);
        }

        //System.out.println("choices calculated: " + next_states.size() + " Best choice: " + heuristic.getChoice(Heuristic.Option.BEST));

        return heuristic;
    }

    public GameState choose(Choice choice) {
        memo.incrementMovesPlayed();
        return choices.get(choice);
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
                ", heuristic=" + heuristic +
                ", my_turn=" + my_turn +
                ", move_number=" + move_number +
                '}';
    }

    public void printBestChoices() {
        Choice choice;
        StringBuilder sbuilder = new StringBuilder();
        GameState state = this;

        do {
            choice = state.getHeuristic().getChoice();
            //sbuilder.append("val = "+ state.getValue() + ", now choose: " + choice + "\n");
            sbuilder.append(String.format("val=%.1f, now %s: %s%n", state.getValue(),
                    (state.getHeuristic().isMyTurn() ? "I choose" : "opponent choses"), choice));
            state = state.choices.get(choice);
        } while (choice != null);

        System.out.println(sbuilder.toString());
    }
}