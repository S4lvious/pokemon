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

import utils.SpriteLoader;

public class Player {
    public int x, y;
    public final int tileSize;

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
    }


    private List<Pokemon> party = new ArrayList<Pokemon>();


    public void addPokemonToParty(Pokemon pokemon) {
        if (party.size() < 6) {
            party.add(pokemon);
        } else {
            System.out.println("La tua squadra è piena!");
        }
    }

    public List<Pokemon> getParty() {
        return party;
    }


    public void savePartyToFile(String filename) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/assets/saves/" + filename))) {
            for (Pokemon p : party) {
           String line = String.join(",",
                    p.getName(),
                    String.valueOf(p.getLevel()),
                    String.valueOf(p.getMaxHp()),
                    String.valueOf(p.getCurrentHp()),
                    String.valueOf(p.getAttack()),
                    String.valueOf(p.getSpeed())
                );
                writer.write(line);
                writer.newLine();
            
            }
            System.out.println("Squadra salvata in " + filename);
        } catch (Exception e) {
            System.err.println("Errore nel salvare la squadra: " + e.getMessage());

        }
    }

        public void loadPartyFromFile(String filename) {
        party.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/assets/saves/" + filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String name = parts[0];
                    int level = Integer.parseInt(parts[1]);
                    int maxHp = Integer.parseInt(parts[2]);
                    int currentHp = Integer.parseInt(parts[3]);
                    int attack = Integer.parseInt(parts[4]);
                    int speed = Integer.parseInt(parts[5]);

                    Pokemon p = new Pokemon(name, level, maxHp, attack, speed, speed);
                    p.setCurrentHp(currentHp);
                    addPokemonToParty(p);
                }
            }
            System.out.println("Squadra caricata da: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
