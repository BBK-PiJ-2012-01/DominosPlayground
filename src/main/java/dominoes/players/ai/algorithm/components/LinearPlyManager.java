package dominoes.players.ai.algorithm.components;

/**
 * A ply manager that increases ply by the same amount each time.
 *
 * @author Sam Wright
 */
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
