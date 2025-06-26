package world;

public class Tile {
    public static final int GRASS = 0;
    public static final int WATER = 1;
    public static final int WALL = 2;

    private int type;


    public Tile(int type) {
        this.type = type;
    }



    public int getType() {
        return type;
    }

    public boolean isWalkable() {
        return type != WALL;
    }
}
