package utils;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public class SpriteLoader {
    public static BufferedImage load(String path) {
        try {
            InputStream is = SpriteLoader.class.getResourceAsStream(path);
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            } else {
                return javax.imageio.ImageIO.read(is);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load sprite: " + path, e);
        }
    }
}
