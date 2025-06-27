package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import engine.InputHandler;
import engine.LocalizationManager;
import engine.models.ChoiceContext;
import entities.Player;
import ui.states.ChoiceState;
import ui.states.IGameState;
import ui.states.MainMenuState;
import ui.states.PartyScreenState;
import ui.states.SettingsMenuState;
import ui.states.WorldState;
import world.WorldMap;

public class GamePanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;
    public static final int TILE_SIZE = 32;
    public static final int SCALE = 2;
    public static final int WIDTH = 240;
    public static final int HEIGHT = 160;
    public static final int FPS = 60;

    // --- Componenti di Gioco ---
    private Player player;
    private WorldMap worldMap;
    private InputHandler input;
    private Thread gameThread;

    // --- Gestione Stati ---
    private Map<GameState, IGameState> gameStates;
    private IGameState currentState;

    // --- Gestione Cooldown ---
    private long lastMenuToggleTime = 0;
    private final long menuToogleCooldown = 200;

    // --- Grafica ---
    private int cameraX = 0, cameraY = 0;
    private BufferedImage tileSet;
    private BufferedImage[][] tiles;
    private int tileCols, tileRows;
    
    // --- Gestione Messaggi a Schermo ---
    private String onScreenMessage = null;
    private long messageSetTime = 0;
    private final long MESSAGE_DISPLAY_DURATION = 2000; // 2 secondi


    // --- Stato Battaglia ---
    private boolean inBattle = false;

    public GamePanel(GameWindow window) {
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setBackground(Color.BLACK);
        setFocusable(true);

        input = new InputHandler();
        addKeyListener(input);
        worldMap = new WorldMap(30, 20);
        player = new Player(4, 6, TILE_SIZE);
        player.getParty().loadFromFile(); // Nuova chiamata per caricare

        loadGraphics();
        initializeStates();
        startGameLoop();
    }

    private void initializeStates() {
        gameStates = new EnumMap<>(GameState.class);
        gameStates.put(GameState.WORLD, new WorldState(player, worldMap));
        gameStates.put(GameState.MAIN_MENU, new MainMenuState());
        gameStates.put(GameState.SETTINGS_MENU, new SettingsMenuState());
        gameStates.put(GameState.PARTY_SCREEN, new PartyScreenState(player));
        gameStates.put(GameState.CHOICE_BOX, new ChoiceState()); // Aggiungiamo il nuovo stato
        
        changeState(GameState.WORLD); // Impostiamo lo stato iniziale
    }
    
    private void loadGraphics() {
        tileSet = utils.SpriteLoader.load("../assets/sprites/tileset.png");
        tileCols = tileSet.getWidth() / TILE_SIZE;
        tileRows = tileSet.getHeight() / TILE_SIZE;
        tiles = new BufferedImage[tileRows][tileCols];
        for (int y = 0; y < tileRows; y++) {
            for (int x = 0; x < tileCols; x++) {
                tiles[y][x] = tileSet.getSubimage(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        while (gameThread != null) {
            long currentTime = System.nanoTime();
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
        if (inBattle) return;
        if (currentState != null) {
            currentState.update(this);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw((Graphics2D) g);
    }

    private void draw(Graphics2D g) {
        drawWorld(g);
        if (currentState != null) {
            currentState.draw(this, g);
        }

        if (onScreenMessage != null) {
            if (System.currentTimeMillis() - messageSetTime < MESSAGE_DISPLAY_DURATION) {
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRoundRect(10, getHeight() - 50, getWidth() - 20, 40, 15, 15);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(onScreenMessage);
                g.drawString(onScreenMessage, getWidth() / 2 - textWidth / 2, getHeight() - 25);
            } else {
                onScreenMessage = null;
            }
        }
    }

    private void drawWorld(Graphics2D g) {
        cameraX = player.x * TILE_SIZE * SCALE - (WIDTH * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;
        cameraY = player.y * TILE_SIZE * SCALE - (HEIGHT * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;
        g.translate(-cameraX, -cameraY);
        for (int y = 0; y < worldMap.height; y++) {
            for (int x = 0; x < worldMap.width; x++) {
                drawTile(g, worldMap.getGroundTile(x, y), x, y);
                drawTile(g, worldMap.getOverlayTile(x, y), x, y);
            }
        }
        player.draw(g, SCALE);
        g.translate(cameraX, cameraY);
    }

    private void drawTile(Graphics2D g, int tileId, int x, int y) {
        if (tileId > 0) {
            int id = tileId - 1;
            int tileX = id % tileCols;
            int tileY = id / tileCols;
            g.drawImage(tiles[tileY][tileX], x * TILE_SIZE * SCALE, y * TILE_SIZE * SCALE, TILE_SIZE * SCALE, TILE_SIZE * SCALE, null);
        }
    }

    // --- Metodi Pubblici per la Gestione degli Stati ---

    public void changeState(GameState newStateKey) {
        if (currentState != null) currentState.onExit();
        currentState = gameStates.get(newStateKey);
        if (currentState != null) {
            lastMenuToggleTime = System.currentTimeMillis();
            currentState.onEnter();
        }
    }
    
    public void handleMainMenuSelection(String selectedOptionKey) {
        LocalizationManager lm = LocalizationManager.getInstance();

        if (selectedOptionKey.equals(lm.getString("menu.pokemon"))) {
            changeState(GameState.PARTY_SCREEN);
        } else if (selectedOptionKey.equals(lm.getString("menu.save"))) {
        	if (player.getParty().saveToFile()) {
                displayOnScreenMessage(lm.getString("save.success"));
            } else {
                // Mostra un messaggio di errore
            }
            changeState(GameState.WORLD);
        } else if (selectedOptionKey.equals(lm.getString("menu.settings"))) {
            changeState(GameState.SETTINGS_MENU);
        } else if (selectedOptionKey.equals(lm.getString("menu.exit"))) {
            askToExitGame(); // Usiamo un metodo helper per la nuova logica
        }
    }
    
    private void askToExitGame() {
        LocalizationManager lm = LocalizationManager.getInstance();
        
        // Definiamo le azioni per "Sì" e "No"
        Runnable actionYes = () -> {
            player.getParty().saveToFile(); // <-- QUESTA È LA CHIAMATA CORRETTA
            System.exit(0);
        };
        
        Runnable actionNo = () -> {
            // Se l'utente dice 'No', poniamo la seconda domanda
            Runnable confirmYes = () -> System.exit(0);
            Runnable confirmNo = () -> changeState(GameState.WORLD); // Torna al gioco
            
            ChoiceContext confirmContext = new ChoiceContext(
                lm.getString("exit.confirm.nosave"),
                List.of(lm.getString("choice.yes"), lm.getString("choice.no")),
                List.of(confirmYes, confirmNo),
                confirmNo // Annullare riporta al gioco
            );

            // Riconfiguriamo lo stato esistente con la nuova domanda.
            // Non serve cambiare stato, perché siamo già in CHOICE_BOX.
            ((ChoiceState) gameStates.get(GameState.CHOICE_BOX)).configure(confirmContext);
        };
        
        // Creiamo e configuriamo il primo box di scelta
        ChoiceContext context = new ChoiceContext(
            lm.getString("exit.confirm.save"),
            List.of(lm.getString("choice.yes"), lm.getString("choice.no")),
            List.of(actionYes, actionNo),
            () -> changeState(GameState.WORLD) // Se l'utente annulla, torna al mondo
        );

        ((ChoiceState) gameStates.get(GameState.CHOICE_BOX)).configure(context);
        changeState(GameState.CHOICE_BOX);
    }    
    
    // --- Helper per messaggi ---
    public void displayOnScreenMessage(String message) {
        this.onScreenMessage = message;
        this.messageSetTime = System.currentTimeMillis();
    }

    // --- Getters per permettere agli stati di accedere a dati/oggetti globali ---

    public InputHandler getInput() { return this.input; }
    public long getLastMenuToggleTime() { return this.lastMenuToggleTime; }
    public long getMenuToggleCooldown() { return this.menuToogleCooldown; }
    public Player getPlayer() { return this.player; }
    public void setInBattle(boolean inBattle) { this.inBattle = inBattle; }
    
    public void resetInput() {
        this.input.reset();
    }
}