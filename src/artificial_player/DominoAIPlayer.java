package artificial_player;


import dominoes.*;

/**
 * User: Sam Wright
 * Date: 07/02/2013
 * Time: 17:50
 */
public class DominoAIPlayer implements dominoes.players.DominoPlayer{
    @Override
    public Play makePlay(Table table) throws CantPlayException {
        return null; // Dummy implementation
    }

    @Override
    public void takeBack(Bone bone) {
        // Dummy implementation
    }

    @Override
    public void draw(BoneYard boneYard) {
        // Dummy implementation
    }

    @Override
    public int numInHand() {
        return 0; // Dummy implementation
    }

    @Override
    public Bone[] bonesInHand() {
        return new Bone[0]; // Dummy implementation
    }

    @Override
    public void newRound() {
        // Dummy implementation
    }

    @Override
    public void setPoints(int i) {
        // Dummy implementation
    }

    @Override
    public int getPoints() {
        return 0; // Dummy implementation
    }

    @Override
    public void setName(String s) {
        // Dummy implementation
    }

    @Override
    public String getName() {
        return "AI Bob";
    }
}
