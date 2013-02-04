package artificial_player;

import java.util.*;

public class StateStats {
    private final static Comparator<Map.Entry<Choice, GameState>> comp = new Comparator<Map.Entry<Choice, GameState>>(){
        @Override
        public int compare(Map.Entry<Choice, GameState> o1, Map.Entry<Choice, GameState> o2) {
            return Double.compare(o1.getValue().getHeuristic().getValue(),
                    o2.getValue().getHeuristic().getValue());
        }
    };

    private StateStats() {}

    public static Map.Entry<Choice, GameState> getBestOption(boolean my_turn, Map<Choice, GameState> choices) {
        if (my_turn) {
            // If my turn, I'll take the option that benefits me the most
            return Collections.max(choices.entrySet(), comp);
        } else {
            // If not my turn, my opponent will take the option that benefits me the least
            return Collections.min(choices.entrySet(), comp);
        }
    }

    public static List<Map.Entry<Choice, GameState>> getNBestOptions(boolean my_turn, Map<Choice, GameState> choices, int N) {
        List<Map.Entry<Choice, GameState>> sorted_options = new LinkedList<Map.Entry<Choice, GameState>>(choices.entrySet());
        Collections.sort(sorted_options, comp);

        int top_index;
        int bottom_index;

        if (my_turn) {
            top_index = sorted_options.size();
            bottom_index = Math.max(0, top_index - N);
        } else {
            bottom_index = 0;
            top_index = Math.min(sorted_options.size(), bottom_index + N);
        }

        return sorted_options.subList(bottom_index, top_index);
    }

    public static double getAverage(Map<Choice, GameState> choices) {
        double total = 0;
        int size = 0;

        for (GameState state : choices.values()) {
            ++size;
            total += state.getHeuristic().getValue();
        }
        return total / size;
    }
}
