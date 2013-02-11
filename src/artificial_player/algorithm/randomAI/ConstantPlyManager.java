package artificial_player.algorithm.randomAI;

import artificial_player.algorithm.virtual.PlyManager;

/**
 * User: Sam Wright
 * Date: 11/02/2013
 * Time: 17:09
 */
public class ConstantPlyManager implements PlyManager {
    @Override
    public int getInitialPly() {
        return 1;
    }

    @Override
    public int[] getPlyIncreases(double[] bestFinalStateValues) {
        return new int[bestFinalStateValues.length];
    }
}
