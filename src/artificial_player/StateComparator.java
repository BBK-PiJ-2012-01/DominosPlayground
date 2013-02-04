package artificial_player;

import java.util.Comparator;
import java.util.Map;

public class StateComparator implements Comparator<Map.Entry<Choice, GameState>> {

    @Override
    public int compare(Map.Entry<Choice, GameState> o1, Map.Entry<Choice, GameState> o2) {
        return Double.compare(o1.getValue().getHeuristic().getValue(),
                    o2.getValue().getHeuristic().getValue());
    }
}
