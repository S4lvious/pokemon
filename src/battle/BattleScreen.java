package battle;

import entities.Player;
import entities.Pokemon;
import main.GamePanel;
import main.GameWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BattleScreen extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Battle battle;
    private final String[] menu = { "Attacca", "Fuggi", "Cattura" };
    private final Player player;
    private int selected = 0;
    
    private String endMessage = "";
    private boolean battleOver = false;

    private BufferedImage background;
    private BufferedImage[] trainerThrowSprites;
    private BufferedImage pokeballSprite;
    private BufferedImage groundPokeballSprite;
    private boolean capturing = false;
    private int captureStep = 0;
    private long captureTimer = 0;


    private GameWindow parent;
    private GamePanel parentPanel;


    private boolean hidePokemon = false;
    private boolean hideEnemyPokemon = false;



    public BattleScreen(Pokemon playerPokemon, Pokemon enemyPokemon, GameWindow parent, GamePanel parentPanel, Player player) {
        this.battle = new Battle(playerPokemon, enemyPokemon);
        this.parent = parent;
        this.parentPanel = parentPanel;
        this.player = player;

        try {
            background = ImageIO.read(getClass().getResourceAsStream("/assets/battle_background.png"));
            trainerThrowSprites = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                trainerThrowSprites[i] = ImageIO.read(getClass().getResourceAsStream("/assets/trainer_throw_" + i + ".png"));
            }
            pokeballSprite = ImageIO.read(getClass().getResourceAsStream("/assets/ground_pokeball.png"));
            groundPokeballSprite = ImageIO.read(getClass().getResourceAsStream("/assets/ground_pokeball.png"));

        } catch (IOException | NullPointerException e) {
            System.err.println("Errore nel caricare lo sfondo battaglia: " + e.getMessage());
        }

        setPreferredSize(new Dimension(640, 480));
        setFocusable(true);
        requestFocusInWindow();
        setupInput();

        Timer  timer = new Timer(1000 / 60, e -> repaint());
        timer.start();
    }

    private void setupInput() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
        getActionMap().put("up", new AbstractAction() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                selected = (selected - 1 + menu.length) % menu.length;
                repaint();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
        getActionMap().put("down", new AbstractAction() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                selected = (selected + 1) % menu.length;
                repaint();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enter");
        getActionMap().put("enter", new AbstractAction() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent e) {
                if (battleOver) {
                    returnToGame(); // torna solo dopo aver visto il messaggio
                } else {
                    handleSelection();
                }
            }
        });

    }

    private void saveParty() {
        try {
            player.savePartyToFile("party.txt");
            System.out.println("Squadra salvata con successo!");
        } catch (Exception e) {
            System.err.println("Errore nel salvare la squadra: " + e.getMessage());
        }
    }

    private void handleSelection() {
        if (battleOver) {
            returnToGame();
            return;
        }

        if (battle.isBattleOver())
            return;

        if (selected == 0) { // Attacca
            String result = battle.performTurn();
            System.out.println(result);

            if (battle.getPlayerPokemon().isFainted()) {
                endMessage = "Il tuo " + battle.getPlayerPokemon().getName() + " è stato sconfitto!";
                battleOver = true;
            } else if (battle.getEnemyPokemon().isFainted()) {
                endMessage = "Hai sconfitto " + battle.getEnemyPokemon().getName() + "!";
                battleOver = true;
            }
        } else if (selected == 1) {
            endMessage = "Sei fuggito!";
            battleOver = true;
        } else if (selected == 2) {
            capturing = true;
            captureStep = 0;
            captureTimer = System.currentTimeMillis();
            repaint();
        }

        repaint();
    }


        private void updateCaptureAnimation(Graphics2D g2d) {
                long now = System.currentTimeMillis();
                int spriteWidth = 250;
                int spriteHeight = 250;

                int trainerX = 30;
                int trainerY = getHeight() - spriteHeight;
                int pokeballX = getWidth() - 250;
                int pokeballY = 250;
                boolean captureSuccessful = false;

            switch (captureStep) {
                case 0: // mostra allenatore che lancia
                    int frameIndex = (int) ((now - captureTimer) / 150) % trainerThrowSprites.length;
                    if (frameIndex == 0 && !hidePokemon) {
                        hidePokemon = true; 
                    }
                    g2d.drawImage(trainerThrowSprites[frameIndex], trainerX, trainerY, spriteWidth, spriteHeight, null);
                    
                    if (now - captureTimer > 600) {
                        captureStep++;
                        captureTimer = now;
                    }
                    break;
                case 1: // mostra pokeball in volo
                    g2d.drawImage(pokeballSprite, pokeballX - 50, pokeballY - 100, 64, 64, null);
                    if (now - captureTimer > 500) {
                        captureStep++;
                        captureTimer = now;
                        hideEnemyPokemon = true; 
                    }
                    break;

                case 2: // pokémon scompare, pokéball a terra
                    g2d.drawImage(groundPokeballSprite, pokeballX, pokeballY, 64, 64, null);
                    if (now - captureTimer > 700) {
                        captureStep++;
                        captureTimer = now;
                        captureSuccessful = Math.random() < 0.6; // probabilità di cattura
                        if (captureSuccessful) {
                            player.addPokemonToParty(battle.getEnemyPokemon());
                            endMessage = "Hai catturato " + battle.getEnemyPokemon().getName() + "!";
                            battleOver = true;

                        } else {
                            endMessage = "Oh no! Il Pokémon è fuggito!"; // Per semplificare.
                            battleOver = true;

                        }
                    }
                    break;
            }
        }
    private void returnToGame() {
        
        saveParty();

        parentPanel.setInBattle(false);
        parentPanel.getInput().reset();
        parent.setContentPane(parentPanel);
        parent.revalidate();
        parent.repaint();
        SwingUtilities.invokeLater(() -> {
            parentPanel.requestFocusInWindow();
        });
        parentPanel.startGameLoop();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Disegna lo sfondo
        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }

        // Pokémon
        Pokemon player = battle.getPlayerPokemon();
        Pokemon enemy = battle.getEnemyPokemon();

        int spriteWidth = 250;
        int spriteHeight = 250;

        int playerX = 30;
        int playerY = getHeight() - spriteHeight;

        int enemyX = getWidth() - spriteWidth - 150;
        int enemyY = 250;

        // Cattura pokemon

        if (capturing) {
            updateCaptureAnimation(g2d);
        } 

        if (!capturing || !hidePokemon) {
            if (player.getSprite() != null)
                g2d.drawImage(player.getSprite(), playerX, playerY, spriteWidth, spriteHeight, null);
        } 
        if (!capturing || !hideEnemyPokemon) {
            if (enemy.getSprite() != null)
                g2d.drawImage(enemy.getSprite(), enemyX, enemyY, spriteWidth, spriteHeight, null);
        }
        // Posizione sopra lo sprite nemico
        int infoBoxWidth = 300;
        int infoBoxHeight = 90;
        int infoBoxX = enemyX;
        int infoBoxY = enemyY - infoBoxHeight - 20;

        // Disegno del box
        g2d.setColor(new Color(250, 250, 250));
        g2d.fillRoundRect(infoBoxX, infoBoxY, infoBoxWidth, infoBoxHeight, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(infoBoxX, infoBoxY, infoBoxWidth, infoBoxHeight, 12, 12);

        // Testo
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(enemy.getName() + "  Lv." + enemy.getLevel(), infoBoxX + 10, infoBoxY + 25);

        // Barra HP più spessa
        int maxBarWidth = 220;
        int hpWidth = (int) ((enemy.getCurrentHp() / (float) enemy.getMaxHp()) * maxBarWidth);
        g2d.setColor(Color.RED);
        g2d.fillRect(infoBoxX + 10, infoBoxY + 40, hpWidth, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(infoBoxX + 10, infoBoxY + 40, maxBarWidth, 15);

        int playerInfoBoxWidth = 300;
        int playerInfoBoxHeight = 90;
        int playerInfoBoxX = playerX;
        int playerInfoBoxY = playerY - playerInfoBoxHeight - 20;




        // Disegno del box
        g2d.setColor(new Color(250, 250, 250));
        g2d.fillRoundRect(playerInfoBoxX, playerInfoBoxY, playerInfoBoxWidth, playerInfoBoxHeight, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(playerInfoBoxX, playerInfoBoxY, playerInfoBoxWidth, playerInfoBoxHeight, 12, 12);

        // Testo
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(player.getName() + "  Lv." + player.getLevel(), playerInfoBoxX + 10, playerInfoBoxY + 25);

        // Barra HP
        int playerMaxBarWidth = 220;
        int playerHpWidth = (int) ((player.getCurrentHp() / (float) player.getMaxHp()) * playerMaxBarWidth);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(playerInfoBoxX + 10, playerInfoBoxY + 40, playerHpWidth, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(playerInfoBoxX + 10, playerInfoBoxY + 40, playerMaxBarWidth, 15);

        // Box inferiore comandi
        g2d.setColor(new Color(20, 20, 20, 220));
        int boxHeight = 120;
        int boxY = getHeight() - boxHeight;

        g2d.setColor(new Color(20, 20, 20, 220));
        g2d.fillRoundRect(0, boxY, getWidth(), boxHeight, 0, 0);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("Scegli un'azione:", 30, boxY + 30);

        for (int i = 0; i < menu.length; i++) {
            g2d.setColor(i == selected ? Color.YELLOW : Color.WHITE);
            g2d.drawString((i + 1) + ". " + menu[i], 50, boxY + 60 + i * 25);
        }
        if (battleOver && endMessage != null) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRoundRect(80, getHeight() / 2 - 60, getWidth() - 160, 120, 20, 20);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(endMessage);
            g2d.drawString(endMessage, (getWidth() - textWidth) / 2, getHeight() / 2 + 5);

            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString("Premi INVIO per continuare...", getWidth() / 2 - 100, getHeight() / 2 + 30);
        }

    }
}
