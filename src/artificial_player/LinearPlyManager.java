package artificial_player;


public class LinearPlyManager implements PlyManager {

    @Override
    public int getInitialPly() {
        return 4;
    }

    @Override
    public int[] getPlyIncreases(double[] bestFinalStateValues) {
        int[] ply_increases = new int[bestFinalStateValues.length];

        for (int i = 0; i < ply_increases.length; ++i) {
            ply_increases[i] = 2;
        }

        return ply_increases;
    }

}
