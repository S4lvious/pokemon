package main;

import javax.swing.*;

import battle.BattleManager;
import world.WorldMap;

import java.awt.*;

import entities.Player;
import entities.Pokemon;
import ui.PartyScreen;
import utils.SpriteLoader;
import engine.InputHandler;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements Runnable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int TILE_SIZE = 32;
    public static final int SCALE = 2;
    public static final int WIDTH = TILE_SIZE * 16;
    public static final int HEIGHT = TILE_SIZE * 12;
    public static final int FPS = 60;

    private boolean isMenuOpen = false;
    private int menuSelection = 0;
    private final String[] menuOptions = {"Pokemon", "Save"};

    private int cameraX = 0;
    private int cameraY = 0;


    BufferedImage tileSet;
    BufferedImage[][] tiles;

    private Player player;
    private InputHandler input;

    private int tileCols;
    private int tileRows;

    private WorldMap worldMap;

    private Thread gameThread;
    private GameWindow window;

    private long lastMenuToggleTime = 0;
    private final long menuToogleCooldown = 200; 

    boolean inBattle = false;
    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public GamePanel(GameWindow window) {
        this.window = window;
        worldMap = new WorldMap(30, 20);
        player = new Player(4, 6, TILE_SIZE);
        player.loadPartyFromFile("party.txt");
        input = new InputHandler();
        addKeyListener(input);
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

        tileSet = SpriteLoader.load("../assets/sprites/tileset.png");
        tileCols = tileSet.getWidth() / TILE_SIZE;
        tileRows = tileSet.getHeight() / TILE_SIZE;
        tiles = new BufferedImage[tileRows][tileCols];

        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                tiles[y][x] = tileSet.getSubimage(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        startGameLoop();

    }

    public InputHandler getInput() {
        return input;
    }
    

public void startGameLoop() {
    if (gameThread == null || !gameThread.isAlive()) {
        InputHandler input = new InputHandler();
        addKeyListener(input);
        cameraX = player.x * TILE_SIZE * SCALE - WIDTH / 2;
        cameraY = player.y * TILE_SIZE * SCALE - HEIGHT / 2;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        gameThread = new Thread(this);
        gameThread.start();


    }
}


    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        if (inBattle) {
            return; // Non aggiorna il gioco se è in battaglia
        }
        long now = System.currentTimeMillis();
        boolean mooved = false;

        if ((input.isPressed(KeyEvent.VK_X) && now - lastMenuToggleTime > menuToogleCooldown) ) {
            isMenuOpen = !isMenuOpen;
             lastMenuToggleTime = System.currentTimeMillis();
        }
         if (isMenuOpen) {
                if (input.isPressed(KeyEvent.VK_DOWN)) {
                    menuSelection = (menuSelection + 1) % menuOptions.length;
                } else if (input.isPressed(KeyEvent.VK_UP)) {
                    menuSelection = (menuSelection - 1 + menuOptions.length) % menuOptions.length;
                } else if (input.isPressed(KeyEvent.VK_Z)) {
                    handleMenuSelection();
                }
                return;
            }
        if (now - player.getLastMoveTime() >= player.getMoveCooldown()) {
            if (input.isPressed(KeyEvent.VK_UP)) {
                if (worldMap.isWalkable(player.x, player.y - 1)) {
                    player.move(0, -1);
                    player.setLastMoveTime(now);
                    mooved = true;
                }
            } else if (input.isPressed(KeyEvent.VK_DOWN)) {
                if (worldMap.isWalkable(player.x, player.y + 1)) {
                    player.move(0, 1);
                    player.setLastMoveTime(now);
                    mooved = true;
                }
            } else if (input.isPressed(KeyEvent.VK_LEFT)) {
                if (worldMap.isWalkable(player.x - 1, player.y)) {
                    player.move(-1, 0);
                    player.setLastMoveTime(now);
                    mooved = true;
                }
            } else if (input.isPressed(KeyEvent.VK_RIGHT)) {
                if (worldMap.isWalkable(player.x + 1, player.y)) {
                    player.move(1, 0);
                    player.setLastMoveTime(now);
                    mooved = true;
                }
            }

                if (mooved) {
                cameraX = player.x * TILE_SIZE * SCALE - WIDTH / 2;
                cameraY = player.y * TILE_SIZE * SCALE - HEIGHT / 2;
                player.setLastMoveTime(now);
                if (worldMap.isGrassTile(player.x, player.y)) {
                    double pokemonEncounterChance = 0.2;
                    if (Math.random() < pokemonEncounterChance) {
                        inBattle = true;
                        Pokemon wildPokemon = new Pokemon("Bulbasaur", 5, 10, 10, 10, 15);
                        Pokemon playerPokemon = player.getParty().get(0); // Prende il primo Pokémon della squadra del giocatore
                        BattleManager battleManager = new BattleManager(window, this, player);
                        battleManager.startBattle(playerPokemon, wildPokemon);

                    }

                }
            }
        }
        player.update();
    }

private void handleMenuSelection() {
    String selectedOption = menuOptions[menuSelection];
    switch (selectedOption) {
        case "Pokemon":
            JFrame partyFrame = new JFrame("Il tuo Party");
            partyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            partyFrame.setSize(400, 400);
            partyFrame.setLocationRelativeTo(null);
            partyFrame.add(new PartyScreen(player));
            partyFrame.setVisible(true);
            isMenuOpen = false;
            lastMenuToggleTime = System.currentTimeMillis(); // Aggiorna il tempo dell'ultimo toggle del menu
            input.reset();
            break;
        case "Save":
            player.savePartyToFile("party.txt");
            JOptionPane.showMessageDialog(this, "Gioco salvato con successo!");
            input.reset();
            break;
        default:
            break;
    }
}


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw((Graphics2D) g);
    }

    private void draw(Graphics2D g) {

        g.translate(-cameraX, -cameraY);

        for (int y = 0; y < worldMap.height; y++) {
            for (int x = 0; x < worldMap.width; x++) {
                int groundTile = worldMap.getGroundTile(x, y);
                if (groundTile > 0) {
                    int id = groundTile - 1;
                    int tileX = id % tileCols;
                    int tileY = id / tileCols;
                    g.drawImage(tiles[tileY][tileX],
                            x * TILE_SIZE * SCALE, y * TILE_SIZE * SCALE,
                            TILE_SIZE * SCALE, TILE_SIZE * SCALE, null);
                }

                int overlayTile = worldMap.getOverlayTile(x, y);
                if (overlayTile > 0) {
                    int id = overlayTile - 1;
                    int tileX = id % tileCols;
                    int tileY = id / tileCols;
                    g.drawImage(tiles[tileY][tileX],
                            x * TILE_SIZE * SCALE, y * TILE_SIZE * SCALE,
                            TILE_SIZE * SCALE, TILE_SIZE * SCALE, null);
                }
            }
        }

        player.draw(g, SCALE);
        g.translate(cameraX, cameraY); 
                if (isMenuOpen) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(WIDTH * SCALE - 160, 40, 140, 60);

            g.setColor(Color.WHITE);
            g.drawRect(WIDTH * SCALE - 160, 40, 140, 60);

            for (int i = 0; i < menuOptions.length; i++) {
                if (i == menuSelection) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.drawString(menuOptions[i], WIDTH * SCALE - 150, 60 + i * 20);
            }
        }
    }

}
