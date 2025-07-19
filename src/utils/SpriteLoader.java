package utils;

import static javax.imageio.ImageIO.read;

import java.awt.image.BufferedImage;
import java.io.InputStream;

public final class SpriteLoader {

    private SpriteLoader() {
        // Classe utility: costruttore privato per evitare istanziazione
    }

    public static BufferedImage load(String path) {
        try (InputStream is = SpriteLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Risorsa non trovata: " + path);
            }
            return read(is);
        } catch (Exception e) {
            throw new RuntimeException("Impossibile caricare lo sprite: " + path, e);
        }
    }
}
