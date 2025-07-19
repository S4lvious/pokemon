package world;

import java.util.Set;

public class WorldMap {
	 public final int width; // Larghezza della mappa in tile
	 public final int height; // Altezza della mappa in tile

    public int[][] groundLayer;
    public int[][] overlayLayer;
    public boolean[][] collisionMap;
    private Set<Integer> grassTileIds;

    public WorldMap(int width, int height) { //Dimensioni mappa
        this.width = width;
        this.height = height;

        String path = "src/assets/maps/FirstMap.tmx";

        groundLayer = MapLoader.loadTMXLayer(path, "Livello tile 1", width, height);
        overlayLayer = MapLoader.loadTMXLayer(path, "Livello tile 2", width, height);
        collisionMap = MapLoader.loadCollisionMap(path, width, height);
        grassTileIds = MapLoader.loadGrassTiles("src/assets/maps/Tileset.tsx");
        //grassTileIds = MapLoader.loadGrassTiles("assets/maps/Tileset.tsx");
    }

        public boolean isGrassTile(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height)
                return false;

            int id = groundLayer[y][x];
            int id2 = overlayLayer[y][x];

            boolean isGrass = grassTileIds.contains(id - 1) || grassTileIds.contains(id2 - 1);

            System.out.println("Tile ID: " + id + " → nel TSX: " + (id - 1) + " → isGrass: " + isGrass);
            return isGrass;
        }


    public boolean isWalkable(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return false;
        return !collisionMap[y][x];
    }

    public int getGroundTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return -1;
        return groundLayer[y][x];
    }

    public int getOverlayTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return -1;
        return overlayLayer[y][x];
    }
}
