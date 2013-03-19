package dominoes.players.dominoes.players;

import dominoes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Sam Wright
 * Date: 19/03/2013
 * Time: 15:47
 */
public class TestAIPlayer implements dominoes.players.DominoPlayer {
    private static TestAIPlayer lastPlayerToMove;

    private List<Bone> myBones;
    private int points;
    private String name;
    private Table table;

    private static boolean boneMatchesNumber(Bone bone, int number) {
        return bone.right() == number || bone.left() == number;
    }

    @Override
    public void takeBack(Bone bone) {
        throw new RuntimeException("Asked to takeBack()");
    }

    public TestAIPlayer() {
        newRound();
    }

    @Override
    public Play makePlay(Table table) throws CantPlayException {
        System.out.println(name + " makePlay()");

        if (lastPlayerToMove == this)
            throw new RuntimeException("BUG! last player to move was me!!!");

        lastPlayerToMove = this;
        int end = -1;
        this.table = table;
        Play play = null;

        System.out.println("\tLayout is: " + layoutToString(table.layout()));
        System.out.println("\tMy hand is " + layoutToString(bonesInHand()));

        for (Bone bone : myBones) {
            // Check if bone matches either end.
            if (boneMatchesNumber(bone, table.right())) {
                end = Play.RIGHT;
                if (bone.right() == table.right())
                    bone.flip();
            } else if (boneMatchesNumber(bone, table.left())) {
                end = Play.LEFT;
                if (bone.left() == table.left())
                    bone.flip();
            } else
                continue;


            // Check that bone is right:
            if (end == Play.RIGHT)
                assert bone.left() == table.right();
            else if (end == Play.LEFT)
                assert bone.right() == table.left();
            else
                throw new RuntimeException("This shouldn't happen...");

            play = new Play(bone, end);

            break;
        }

        if (play == null) {
            System.out.println("\tCannot play");
            throw new CantPlayException();
        } else {
            myBones.remove(play.bone());
            System.out.println("\tPlay is: " + play.end() + " with bone " + play.bone().left() + "," + play.bone().right());
            return play;
        }
    }

    @Override
    public void draw(BoneYard boneYard) {
        System.out.println(name + " draw()");
        myBones.add(boneYard.draw());
    }

    @Override
    public int numInHand() {
        return myBones.size();
    }

    @Override
    public Bone[] bonesInHand() {
        return myBones.toArray(new Bone[0]);
    }

    @Override
    public void newRound() {
        myBones = new ArrayList<Bone>(21);
        lastPlayerToMove = null;
    }

    @Override
    public void setPoints(int i) {
        points = 0;
    }

    @Override
    public int getPoints() {
        return points;
    }

    @Override
    public void setName(String s) {
        name = s;
    }

    @Override
    public String getName() {
        return name;
    }

    private String layoutToString(Bone[] layout) {
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("{ ");

        for (Bone bone : layout) {
            if (bone != null)
                sbuilder.append("[").append(bone.left()).append(",").append(bone.right()).append("] ");
            else {
                System.out.println("\t\t======= found null =========");
                sbuilder.append("[null] ");
            }
        }

        sbuilder.append("}");
        return sbuilder.toString();
    }
}
