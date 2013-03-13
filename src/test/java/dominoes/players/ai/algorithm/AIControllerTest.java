package dominoes.players.ai.algorithm;

import dominoes.players.ai.algorithm.helper.Bones;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class AIControllerTest {
    private AIController my_ai, opponent_ai;
    private List<ImmutableBone> my_bones, opponent_bones;
    private List<ImmutableBone> boneyard_bones;


    private void setUpBones() {
        List<ImmutableBone> all_bones = new LinkedList<ImmutableBone>(Bones.getAllBones());
        Collections.shuffle(all_bones);

        my_bones = all_bones.subList(0, 7);
        opponent_bones = all_bones.subList(7, 14);
        boneyard_bones = new LinkedList<ImmutableBone>(all_bones.subList(14, 28));
    }

    @Before
    public void setUp() throws Exception {
        my_ai = AIBuilder.createAI("SlowProbabilisticAI");
        opponent_ai = AIBuilder.createAI("SlowProbabilisticAI");

        setUpBones();

        my_ai.setInitialState(my_bones, true);
        opponent_ai.setInitialState(opponent_bones, false);
    }

    @Test
    public void testSetInitialState() throws Exception {
        my_ai.setInitialState(my_bones, true);
    }

    // TODO: write some proper tests here...
}
