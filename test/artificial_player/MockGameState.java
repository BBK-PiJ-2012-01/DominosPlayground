package artificial_player;

import artificial_player.algorithm.GameState;
import artificial_player.algorithm.helper.BoneManager;
import artificial_player.algorithm.helper.Choice;
import artificial_player.algorithm.helper.ImmutableBone;

import java.util.LinkedList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 12/02/2013
 * Time: 18:06
 */
public class MockGameState implements GameState {
    private final GameState parent;
    private final int value;
    private Choice choiceTaken;
    private final List<GameState> childStates = new LinkedList<GameState>();
    private boolean isMyTurn;

    public static MockGameState createRoot() {
        return new MockGameState(null, 0);
    }

    public MockGameState(MockGameState parent, int value) {
        this.parent = parent;
        this.value = value;

        if (parent != null) parent.addChildState(this);
    }

    public void addChildState(MockGameState childState) {
        childStates.add(childState);
    }

    public void setChoiceTaken(Choice choiceTaken) {
        this.choiceTaken = choiceTaken;
    }

    public void setMyTurn(boolean isMyTurn) {
        this.isMyTurn = isMyTurn;
    }

    @Override
    public boolean isMyTurn() {
        return isMyTurn;
    }

    @Override
    public Status getStatus() {
        if (childStates.isEmpty())
            return Status.GAME_OVER;
        else
            return Status.HAS_CHILD_STATES;
    }

    @Override
    public List<GameState> getChildStates() {
        return childStates;
    }

    @Override
    public String toString() {
        return "Mock state with value = " + getValue();
    }

    // =========== Below are unimplemented functions ===========

    @Override
    public BoneManager getBoneManager() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public GameState choose(Choice choice) {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public List<ImmutableBone> getPossibleOpponentBones() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public Choice getChoiceTaken() {
        return choiceTaken;
    }

    @Override
    public List<ImmutableBone> getMyBones() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public GameState getParent() {
        return parent;
    }

    @Override
    public void increasePly(int plyIncrease) {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public int getSizeOfOpponentHand() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public int getSizeOfBoneyard() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public int getLayoutRight() {
        throw new UnsupportedOperationException("Not supported in mock");
    }

    @Override
    public int getLayoutLeft() {
        throw new UnsupportedOperationException("Not supported in mock");
    }
}
