package dominoes.players.ai.algorithm.components;

import dominoes.players.ai.algorithm.helper.BoneState;
import dominoes.players.ai.algorithm.helper.Choice;
import dominoes.players.ai.algorithm.helper.ImmutableBone;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 22:24
 */
public class AbstractStateEnumeratorTest {
    private AbstractStateEnumerator stateEnumerator;
    private Set<ImmutableBone> bones;

    @Before
    public void setUp() throws Exception {
        stateEnumerator = new AbstractStateEnumerator() {
            @Override
            public List<Choice> getMyValidChoices(BoneState boneState) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public List<Choice> getOpponentValidChoices(BoneState boneState) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };

        bones = new HashSet<ImmutableBone>();
        bones.add(new ImmutableBone(0, 1));
        bones.add(new ImmutableBone(0, 2));
        bones.add(new ImmutableBone(2, 3));
        bones.add(new ImmutableBone(2, 4));
        bones.add(new ImmutableBone(4, 5));
        bones.add(new ImmutableBone(4, 6));
    }

    @Test
    public void testGetValidInitialChoices() throws Exception {
        List<Choice> choices = stateEnumerator.getValidInitialChoices(new LinkedList<ImmutableBone>(bones));
        Set<ImmutableBone> placeableBones = new HashSet<ImmutableBone>();

        assertEquals(bones.size(), choices.size());

        for (Choice choice : choices) {
            assertTrue(choice.getAction().equals(Choice.Action.PLACED_RIGHT)
                    || choice.getAction().equals(Choice.Action.PLACED_LEFT));

            placeableBones.add(choice.getBone());
        }

        assertEquals(bones, placeableBones);
    }

    @Test
    public void testGetValidPlacingChoices() throws Exception {
        int left = 2;
        int right = 4;

        Set<ImmutableBone> expectedLeftBones = getMatchingBones(bones, left);
        Set<ImmutableBone> expectedRightBones = getMatchingBones(bones, right);

        Set<ImmutableBone> leftBones = new HashSet<ImmutableBone>();
        Set<ImmutableBone> rightBones = new HashSet<ImmutableBone>();

        for (Choice choice : stateEnumerator.getValidPlacingChoices(new LinkedList<ImmutableBone>(bones), left, right)) {
            if (choice.getAction() == Choice.Action.PLACED_LEFT)
                leftBones.add(choice.getBone());
            else if (choice.getAction() == Choice.Action.PLACED_RIGHT)
                rightBones.add(choice.getBone());
            else
                throw new RuntimeException("Bone should have been placed, but instead was: " + choice.getAction());
        }

        assertEquals(expectedLeftBones, leftBones);
        assertEquals(expectedRightBones, rightBones);
    }

    private Set<ImmutableBone> getMatchingBones(Set<ImmutableBone> bonesToMatch, int valueToMatch) {
        Set<ImmutableBone> matchingBones = new HashSet<ImmutableBone>();

        for (ImmutableBone bone : bonesToMatch) {
            if (bone.right() == valueToMatch || bone.left() == valueToMatch)
                matchingBones.add(bone);
        }

        return matchingBones;
    }

    @Test
    public void testGetValidPickupChoices() throws Exception {
        Set<ImmutableBone> pickedUpBones = new HashSet<ImmutableBone>();

        for (Choice choice : stateEnumerator.getValidPickupChoices(new LinkedList<ImmutableBone>(bones))) {
            assertEquals(Choice.Action.PICKED_UP, choice.getAction());
            pickedUpBones.add(choice.getBone());
        }

        assertEquals(bones, pickedUpBones);
    }
}
