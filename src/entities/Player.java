package entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.Constants;
import utils.SpriteLoader;

public class Player {
    public int x, y;
    public final int tileSize;

    // Il Player ora possiede un oggetto Party, invece di gestire direttamente la lista.
    private final Party party;

    // Variabili per l'animazione e il movimento
    private BufferedImage[][] sprites;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private int frameDelay = 8; // Questo valore ora controlla la velocità dell'animazione
    private boolean animatingForward = true;

    private Direction direction = Direction.DOWN;
    private boolean facingLeft = false;

    private long lastMoveTime = 0;
    private final long moveDelay = 200; // Questo valore ora controlla la velocità di movimento

    public enum Direction {
        UP, DOWN, SIDE
    }

    public Player(int x, int y, int tileSize) {
        this.x = x;
        this.y = y;
        this.tileSize = tileSize;
        
        // Inizializziamo il nuovo oggetto Party usando la costante che abbiamo definito.
        this.party = new Party(Constants.PARTY_SAVE_FILE);
        
        loadSprites();
    }

    // L'unico metodo di accesso al party è questo, che restituisce l'oggetto intero.
    public Party getParty() {
        return this.party;
    }

    private void loadSprites() {
        BufferedImage spriteSheet = SpriteLoader.load("../assets/sprites/player.png");
        int spriteWidth = spriteSheet.getWidth() / 3;
        int spriteHeight = spriteSheet.getHeight() / 3;

        sprites = new BufferedImage[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sprites[i][j] = spriteSheet.getSubimage(j * spriteWidth, i * spriteHeight, spriteWidth, spriteHeight);
            }
        }
    }

    public void update() {
        long now = System.currentTimeMillis();
        // L'animazione è legata alla durata del passo (moveDelay)
        if (now - lastMoveTime < moveDelay) {
            animationCounter++;
            if (animationCounter >= frameDelay) {
                animationCounter = 0;
                if (animatingForward) {
                    currentFrame++;
                    if (currentFrame >= 2) {
                        currentFrame = 2;
                        animatingForward = false;
                    }
                } else {
                    currentFrame--;
                    if (currentFrame <= 0) {
                        currentFrame = 0;
                        animatingForward = true;
                    }
                }
            }
        } else {
            // Se non ci stiamo muovendo, torniamo al frame di riposo.
            currentFrame = 1;
        }
    }
    
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;

        if (dy != 0) {
            direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        } else if (dx != 0) {
            direction = Direction.SIDE;
            facingLeft = dx < 0;
        }
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setDirection(Direction direction, boolean facingLeft) {
        this.direction = direction;
        this.facingLeft = facingLeft;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public long getMoveCooldown() {
        return moveDelay;
    }

    public void draw(Graphics2D g, int scale) {
        int px = x * tileSize * scale;
        int py = y * tileSize * scale;

        int row = switch (direction) {
            case UP -> 1;
            case DOWN -> 0;
            case SIDE -> 2;
        };

        BufferedImage sprite = sprites[row][currentFrame];
        if (direction == Direction.SIDE && !facingLeft) {
            g.drawImage(sprite, px + tileSize * scale, py, -tileSize * scale, tileSize * scale, null);
        } else {
            g.drawImage(sprite, px, py, tileSize * scale, tileSize * scale, null);
        }
    }
}