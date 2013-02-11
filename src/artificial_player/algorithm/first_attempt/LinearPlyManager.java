package artificial_player.algorithm.first_attempt;


import artificial_player.algorithm.virtual.PlyManager;

public class LinearPlyManager implements PlyManager {

    @Override
    public int getInitialPly() {
        return 2;
    }

    @Override
    public int[] getPlyIncreases(double[] bestFinalStateValues) {
        int[] ply_increases = new int[bestFinalStateValues.length];

        for (int i = 0; i < ply_increases.length && i < 3; ++i) {
            ply_increases[i] = 2;
        }

        return ply_increases;
    }

}
