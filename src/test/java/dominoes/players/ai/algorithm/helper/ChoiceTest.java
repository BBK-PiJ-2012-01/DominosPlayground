package dominoes.players.ai.algorithm.helper;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 17:34
 */
public class ChoiceTest {
    private Choice choice;
    private Choice.Action expectedAction;
    private ImmutableBone expectedBone;

    @Before
    public void setUp() throws Exception {
        expectedAction = Choice.Action.PLACED_LEFT;
        expectedBone = new ImmutableBone(3, 4);
        choice = new Choice(expectedAction, expectedBone);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPassWithNonNullBone() throws Exception {
        new Choice(Choice.Action.PASS, expectedBone);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlaceLeftWithNullBone() throws Exception {
        new Choice(Choice.Action.PLACED_LEFT, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlaceRightWithNullBone() throws Exception {
        new Choice(Choice.Action.PLACED_LEFT, null);
    }

    @Test
    public void testPickupWithNullAndNotNullBone() throws Exception {
        new Choice(Choice.Action.PICKED_UP, null);
        new Choice(Choice.Action.PICKED_UP, expectedBone);
    }

    @Test
    public void testEquals() throws Exception {
        Choice duplicateChoice = new Choice(expectedAction, expectedBone);
        assertEquals(choice, duplicateChoice);
    }

    @Test
    public void testHashCode() throws Exception {
        Choice duplicateChoice = new Choice(expectedAction, expectedBone);
        assertEquals(choice.hashCode(), duplicateChoice.hashCode());
    }

    @Test
    public void testEquivalenceWithFlippedBone() throws Exception {
        ImmutableBone flippedBone = new ImmutableBone(expectedBone.right(), expectedBone.left());
        Choice duplicateChoice = new Choice(expectedAction, flippedBone);
        assertEquals(choice, duplicateChoice);
        assertEquals(choice.hashCode(), duplicateChoice.hashCode());
    }

    @Test
    public void testEquivalenceWithNullBone() throws Exception {
        Choice nullPickup = new Choice(Choice.Action.PICKED_UP, null);
        Choice nullPickupDupe = new Choice(Choice.Action.PICKED_UP, null);
        Choice pickup = new Choice(Choice.Action.PICKED_UP, expectedBone);

        assertEquals(nullPickup, nullPickupDupe);
        assertEquals(nullPickup.hashCode(), nullPickupDupe.hashCode());

        assertFalse(nullPickup.equals(pickup));
        assertFalse(pickup.equals(nullPickup));
    }

    @Test
    public void testGetAction() throws Exception {
        assertEquals(expectedAction, choice.getAction());
    }

    @Test
    public void testGetBone() throws Exception {
        assertEquals(expectedBone, choice.getBone());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("PLACED_LEFT bone [3,4]", choice.toString());
    }

    @Test
    public void testActionIsPlacement() throws Exception {
        assertFalse(Choice.Action.PASS.isPlacement());
        assertFalse(Choice.Action.PICKED_UP.isPlacement());
        assertTrue(Choice.Action.PLACED_LEFT.isPlacement());
        assertTrue(Choice.Action.PLACED_RIGHT.isPlacement());
    }

}
