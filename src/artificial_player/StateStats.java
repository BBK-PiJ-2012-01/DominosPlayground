package artificial_player;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

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
