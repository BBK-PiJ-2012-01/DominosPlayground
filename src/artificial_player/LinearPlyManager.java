package artificial_player;

import java.util.List;


public class LinearPlyManager implements PlyManager {

    @Override
    public int getInitialPly() {
        return 4;
    }

    @Override
    public int[] getPlyIncreases(List<GameState> bestFinalStates) {
        int[] ply_increases = new int[bestFinalStates.size()];

        for (int i = 0; i < ply_increases.length; ++i) {
            ply_increases[i] = 2;
        }

        return ply_increases;
    }

}
