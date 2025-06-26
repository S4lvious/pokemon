package world;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class MapLoader {

    public static int[][] loadTMXLayer(String tmxPath, String layerName, int width, int height) {
        int[][] layer = new int[height][width];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tmxPath));
            String line;
            boolean readingLayer = false;
            int y = 0;

            while ((line = reader.readLine()) != null) {
                if (line.contains("<layer") && line.contains("name=\"" + layerName + "\"")) {
                    readingLayer = true;
                } else if (readingLayer && line.contains("<data")) {
                    while ((line = reader.readLine()) != null && y < height) {
                        if (line.contains("</data>")) break;
                        String[] values = line.trim().split(",");
                        for (int x = 0; x < width && x < values.length; x++) {
                            String value = values[x].replaceAll("[^0-9]", "");
                            if (!value.isEmpty()) {
                                layer[y][x] = Integer.parseInt(value);
                            }
                        }
                        y++;
                    }
                    break;
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return layer;
    }

        public static Set<Integer> loadGrassTiles(String tsxPath) {
        Set<Integer> grassTileIds = new HashSet<>();

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(tsxPath));
            NodeList tiles = doc.getElementsByTagName("tile");

            for (int i = 0; i < tiles.getLength(); i++) {
                Element tile = (Element) tiles.item(i);
                int tileId = Integer.parseInt(tile.getAttribute("id"));

                NodeList propertiesList = tile.getElementsByTagName("property");
                for (int j = 0; j < propertiesList.getLength(); j++) {
                    Element prop = (Element) propertiesList.item(j);
                    String name = prop.getAttribute("name");
                    String type = prop.getAttribute("type");
                    String value = prop.getAttribute("value");

                    if ("isGrass".equals(name) && "bool".equals(type) && "true".equals(value)) {
                        grassTileIds.add(tileId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return grassTileIds;
    }

    public static boolean[][] loadCollisionMap(String tmxPath, int width, int height) {
        boolean[][] collisionMap = new boolean[height][width];
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(tmxPath));
            NodeList objectGroups = doc.getElementsByTagName("objectgroup");

            for (int i = 0; i < objectGroups.getLength(); i++) {
                Element group = (Element) objectGroups.item(i);
                if (!"Collision".equals(group.getAttribute("name"))) continue;

                NodeList objects = group.getElementsByTagName("object");
                for (int j = 0; j < objects.getLength(); j++) {
                    Element obj = (Element) objects.item(j);

                    float x = Float.parseFloat(obj.getAttribute("x"));
                    float y = Float.parseFloat(obj.getAttribute("y"));
                    float w = obj.hasAttribute("width") ? Float.parseFloat(obj.getAttribute("width")) : 0;
                    float h = obj.hasAttribute("height") ? Float.parseFloat(obj.getAttribute("height")) : 0;

                    int startX = (int)(x / 32);
                    int startY = (int)(y / 32);
                    int endX = (int)((x + w) / 32);
                    int endY = (int)((y + h) / 32);

                    for (int yy = startY; yy <= endY && yy < height; yy++) {
                        for (int xx = startX; xx <= endX && xx < width; xx++) {
                            if (xx >= 0 && yy >= 0) {
                                collisionMap[yy][xx] = true;
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return collisionMap;
    }
}
