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
	public static final int WIDTH = 240; // Dimensioni GBA-Like
	public static final int HEIGHT = 160; // Dimensioni GBA-Like
	public static final int FPS = 60;

    private boolean isMenuOpen = false;
    private int menuSelection = 0;
    private final String[] menuOptions = {"Pokemon", "Save", "Esci"}; //Aggiunta uscita
    
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
    
    private long lastMenuNavTime = 0; // Memorizza l'ora dell'ultima navigazione
    private final long menuNavCooldown = 150; // Cooldown in millisecondi (prova 150-200)

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
        cameraX = player.x * TILE_SIZE * SCALE - (WIDTH * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;
        cameraY = player.y * TILE_SIZE * SCALE - (HEIGHT * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;
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

        if (input.isPressed(KeyEvent.VK_X) && now - lastMenuToggleTime > menuToogleCooldown) {
            isMenuOpen = !isMenuOpen;
            lastMenuToggleTime = System.currentTimeMillis();
        }
        
        if (isMenuOpen) {
            /*if (input.isPressed(KeyEvent.VK_DOWN)) {
                menuSelection = (menuSelection + 1) % menuOptions.length;
            } else if (input.isPressed(KeyEvent.VK_UP)) {
                menuSelection = (menuSelection - 1 + menuOptions.length) % menuOptions.length;
            } else if (input.isPressed(KeyEvent.VK_Z)) {
                handleMenuSelection();
            }
            // Quando il menu è aperto, il giocatore non si muove e non si anima
            player.setMoving(false);
            player.update();
            return;*/ //Vecchia logica
        	
            if (now - lastMenuNavTime > menuNavCooldown) { // Controlliamo se il cooldown è passato
                if (input.isPressed(KeyEvent.VK_DOWN)) {
                    menuSelection = (menuSelection + 1) % menuOptions.length;
                    lastMenuNavTime = now; // IMPORTANTE: Resettiamo il timer del cooldown
                } else if (input.isPressed(KeyEvent.VK_UP)) {
                    menuSelection = (menuSelection - 1 + menuOptions.length) % menuOptions.length;
                    lastMenuNavTime = now; // IMPORTANTE: Resettiamo il timer del cooldown
                }
            }
            
            // La gestione della selezione con 'Z' non ha bisogno di cooldown.
            if (input.isPressed(KeyEvent.VK_Z)) {
                handleMenuSelection();
            }
            return;
        }

        // --- NUOVA LOGICA DI MOVIMENTO ---

        boolean wantsToMove = false;
        int dx = 0;
        int dy = 0;

        // 1. Controlliamo l'input per la direzione (questo è invariato)
        if (input.isPressed(KeyEvent.VK_UP)) {
            player.setDirection(Player.Direction.UP);
            wantsToMove = true;
            dx = 0; dy = -1;
        } else if (input.isPressed(KeyEvent.VK_DOWN)) {
            player.setDirection(Player.Direction.DOWN);
            wantsToMove = true;
            dx = 0; dy = 1;
        } else if (input.isPressed(KeyEvent.VK_LEFT)) {
            player.setDirection(Player.Direction.SIDE, true);
            wantsToMove = true;
            dx = -1; dy = 0;
        } else if (input.isPressed(KeyEvent.VK_RIGHT)) {
            player.setDirection(Player.Direction.SIDE, false);
            wantsToMove = true;
            dx = 1; dy = 0;
        }

        // 2. Eseguiamo il MOVIMENTO se c'è l'intenzione E il cooldown è passato.
        if (wantsToMove && (now - player.getLastMoveTime() >= player.getMoveCooldown())) {
            if (worldMap.isWalkable(player.x + dx, player.y + dy)) {
                // AGGIORNAMO IL TEMPO QUI, all'inizio del nuovo passo.
                player.setLastMoveTime(now);
                player.move(dx, dy);

                if (worldMap.isGrassTile(player.x, player.y)) {
                    // ... (logica incontro Pokémon)
                }
            }
        }

        // 3. Aggiorniamo sempre il Player. Lui ora sa da solo se animarsi o no.
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
        case "Esci":
            // 1. Chiediamo all'utente se vuole salvare prima di uscire.
            int saveChoice = JOptionPane.showConfirmDialog(
                this,
                "Vuoi salvare la partita prima di uscire?",
                "Uscita",
                JOptionPane.YES_NO_OPTION
            );

            // 2. Analizziamo la scelta.
            if (saveChoice == JOptionPane.YES_OPTION) {
                // Se l'utente sceglie "Sì", salviamo e usciamo.
                player.savePartyToFile("party.txt");
                System.exit(0); // Termina l'applicazione
            } else if (saveChoice == JOptionPane.NO_OPTION) {
                // Se l'utente sceglie "No", chiediamo un'ulteriore conferma.
                int confirmExitChoice = JOptionPane.showConfirmDialog(
                    this,
                    "Sei sicuro di voler uscire senza salvare? I progressi non salvati andranno persi.",
                    "Conferma Uscita",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE // Aggiunge un'icona di avvertimento
                );

                if (confirmExitChoice == JOptionPane.YES_OPTION) {
                    // Se l'utente conferma di voler uscire senza salvare, terminiamo.
                    System.exit(0);
                }
                // Se sceglie "No" alla seconda domanda, non facciamo nulla.
                // La finestra di dialogo si chiude e il gioco continua.
            }
            // Se l'utente chiude la prima finestra, non succede nulla.
            
            isMenuOpen = false; // In ogni caso, chiudiamo il menu dopo l'interazione.
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
    	
        // Calcola la posizione della camera per centrarla sul giocatore
    	cameraX = player.x * TILE_SIZE * SCALE - (WIDTH * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;
        cameraY = player.y * TILE_SIZE * SCALE - (HEIGHT * SCALE) / 2 + (TILE_SIZE * SCALE) / 2;

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
            // --- NUOVA LOGICA PER MENU DINAMICO ---

            // 1. Definiamo i margini e le spaziature interne per un look pulito.
            int padding = 10; // Spazio tra il testo e i bordi del menu
            int lineSpacing = 5; // Spazio extra tra le opzioni del menu

            // 2. Otteniamo gli strumenti per misurare il nostro font.
            FontMetrics fm = g.getFontMetrics();
            int lineHeight = fm.getHeight(); // Altezza di una singola riga di testo

            // 3. Calcoliamo la larghezza necessaria per il menu.
            int menuWidth = 0;
            for (String option : menuOptions) {
                int optionWidth = fm.stringWidth(option);
                if (optionWidth > menuWidth) {
                    menuWidth = optionWidth; // Troviamo la larghezza del testo più lungo
                }
            }
            menuWidth += padding * 2; // Aggiungiamo il padding a sinistra e a destra

            // 4. Calcoliamo l'altezza totale del menu.
            int menuHeight = (lineHeight * menuOptions.length) // Altezza di tutti i testi
                             + (lineSpacing * (menuOptions.length - 1)) // Spazio tra le righe
                             + (padding * 2); // Aggiungiamo il padding sopra e sotto

            // 5. Calcoliamo la posizione del menu sullo schermo (in alto a destra).
            int menuX = getWidth() - menuWidth - 10; // 10 è un margine dal bordo destro dello schermo
            int menuY = 10; // Margine dal bordo superiore

            // 6. Disegniamo lo sfondo e il bordo del menu usando le nostre dimensioni calcolate.
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(menuX, menuY, menuWidth, menuHeight);

            g.setColor(Color.WHITE);
            g.drawRect(menuX, menuY, menuWidth, menuHeight);

            // 7. Disegniamo le opzioni del menu.
            for (int i = 0; i < menuOptions.length; i++) {
                if (i == menuSelection) {
                    g.setColor(Color.YELLOW); // Colore per l'opzione selezionata
                } else {
                    g.setColor(Color.WHITE);
                }

                // Calcoliamo la posizione Y di ogni opzione
                int textY = menuY + padding + fm.getAscent() + (i * (lineHeight + lineSpacing));
                
                g.drawString(menuOptions[i], menuX + padding, textY);
            }
        }
    }

}
