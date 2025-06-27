package entities;

import java.awt.Graphics2D;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import main.Constants;
import utils.SpriteLoader;

public class Player {
    public int x, y;
    public final int tileSize;

    private final Party party;

    private BufferedImage[][] sprites;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private int frameDelay = 12; // Puoi cambiare per renderla più lenta o veloce
    private boolean animatingForward = true;

    private Direction direction = Direction.DOWN;
    private boolean moving = false;
    private boolean facingLeft = false;

    private long lastMoveTime = 0;
    private final long moveDelay = 100;


    public enum Direction {
        UP, DOWN, SIDE
    }

    public Player(int x, int y, int tileSize) {
        this.x = x;
        this.y = y;
        this.tileSize = tileSize;
        loadSprites();

        System.out.println(Constants.MAINPARTYFILE);

        try {
            this.party = new Party(Constants.MAINPARTYFILE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public Party getParty() {
        return party;
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
        if (moving) {
            animationCounter++;
            if (animationCounter >= frameDelay) {
                animationCounter = 0;

                // Animazione ping-pong
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
            currentFrame = 1; // Idle frame centrale
        }

        moving = false;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;

        if (dy != 0) direction = (dy > 0) ? Direction.DOWN : Direction.UP;
        else if (dx != 0) {
            direction = Direction.SIDE;
            facingLeft = dx < 0;
        }

        moving = true;
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
