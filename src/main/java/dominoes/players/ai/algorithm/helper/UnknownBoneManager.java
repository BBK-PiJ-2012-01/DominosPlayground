package dominoes.players.ai.algorithm.helper;

import java.util.List;
import java.util.Map;

/**
 * User: Sam Wright
 * Date: 06/03/2013
 * Time: 14:18
 */
public interface UnknownBoneManager {
    UnknownBoneManager createNext(Choice choiceTaken, boolean isMyTurn, int layoutLeft, int layoutRight);

    Map<ImmutableBone, Float> getOpponentBoneProbs();

    int getSizeOfOpponentHand();

    int getSizeOfBoneyard();

    List<ImmutableBone> getUnknownBones();
}
