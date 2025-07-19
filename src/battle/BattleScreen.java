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
    private final String[] menu = {"Attacca", "Fuggi", "Cattura"};
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

        Timer timer = new Timer(1000 / 60, e -> repaint());
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
            player.getParty().saveToFile();
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

        final int spriteWidth = 250;
        final int spriteHeight = 250;
        final int trainerX = 30;
        final int trainerY = getHeight() - spriteHeight;
        final int pokeballX = getWidth() - 250;
        final int pokeballY = 250;

        switch (captureStep) {
            case 0 -> handleTrainerThrow(g2d, now, trainerX, trainerY, spriteWidth, spriteHeight);
            case 1 -> handlePokeballFlight(g2d, now, pokeballX, pokeballY);
            case 2 -> handleCaptureResult(g2d, now, pokeballX, pokeballY);
        }
    }


    private void handleTrainerThrow(Graphics2D g2d, long now, int x, int y, int width, int height) {
        int frameIndex = (int) ((now - captureTimer) / 150) % trainerThrowSprites.length;

        if (frameIndex == 0 && !hidePokemon) {
            hidePokemon = true;
        }

        g2d.drawImage(trainerThrowSprites[frameIndex], x, y, width, height, null);

        if (now - captureTimer > 600) {
            advanceCaptureStep(now);
        }
    }

    private void handlePokeballFlight(Graphics2D g2d, long now, int x, int y) {
        g2d.drawImage(pokeballSprite, x - 50, y - 100, 64, 64, null);

        if (now - captureTimer > 500) {
            hideEnemyPokemon = true;
            advanceCaptureStep(now);
        }
    }

    private void handleCaptureResult(Graphics2D g2d, long now, int x, int y) {
        g2d.drawImage(groundPokeballSprite, x, y, 64, 64, null);

        if (now - captureTimer > 700) {
            boolean captureSuccessful = Math.random() < 0.6;

            if (captureSuccessful) {
                player.getParty().addPokemon(battle.getEnemyPokemon());
                endMessage = "Hai catturato " + battle.getEnemyPokemon().getName() + "!";
            } else {
                endMessage = "Oh no! Il Pokémon è fuggito!";
            }

            battleOver = true;
            advanceCaptureStep(now);
        }
    }

    private void advanceCaptureStep(long now) {
        captureStep++;
        captureTimer = now;
    }


    private void returnToGame() {

        saveParty();

        parentPanel.setInBattle(false);
        parentPanel.resetInput();
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

        drawBackground(g2d);
        drawPokemonSprites(g2d);
        drawEnemyInfoBox(g2d);
        drawPlayerInfoBox(g2d);
        drawCommandBox(g2d);
        drawEndMessage(g2d);
    }

    private void drawBackground(Graphics2D g2d) {
        if (background != null) {
            g2d.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
    }

    private void drawPokemonSprites(Graphics2D g2d) {
        int spriteWidth = 250;
        int spriteHeight = 250;

        int playerX = 30;
        int playerY = getHeight() - spriteHeight;
        int enemyX = getWidth() - spriteWidth - 150;
        int enemyY = 250;

        if (capturing) {
            updateCaptureAnimation(g2d);
        }

        if (!capturing || !hidePokemon) {
            BufferedImage playerSprite = battle.getPlayerPokemon().getSprite();
            if (playerSprite != null) {
                g2d.drawImage(playerSprite, playerX, playerY, spriteWidth, spriteHeight, null);
            }
        }

        if (!capturing || !hideEnemyPokemon) {
            BufferedImage enemySprite = battle.getEnemyPokemon().getSprite();
            if (enemySprite != null) {
                g2d.drawImage(enemySprite, enemyX, enemyY, spriteWidth, spriteHeight, null);
            }
        }
    }

    private void drawEnemyInfoBox(Graphics2D g2d) {
        Pokemon enemy = battle.getEnemyPokemon();
        int spriteWidth = 250;
        int enemyX = getWidth() - spriteWidth - 150;
        int enemyY = 250;

        int boxWidth = 300;
        int boxHeight = 90;
        int boxX = enemyX;
        int boxY = enemyY - boxHeight - 20;

        drawInfoBox(g2d, boxX, boxY, boxWidth, boxHeight, enemy.getName(), enemy.getLevel(), enemy.getCurrentHp(), enemy.getMaxHp(), Color.RED);
    }

    private void drawPlayerInfoBox(Graphics2D g2d) {
        Pokemon player = battle.getPlayerPokemon();
        int spriteHeight = 250;
        int playerX = 30;
        int playerY = getHeight() - spriteHeight;

        int boxWidth = 300;
        int boxHeight = 90;
        int boxX = playerX;
        int boxY = playerY - boxHeight - 20;

        drawInfoBox(g2d, boxX, boxY, boxWidth, boxHeight, player.getName(), player.getLevel(), player.getCurrentHp(), player.getMaxHp(), Color.GREEN);
    }

    private void drawInfoBox(Graphics2D g2d, int x, int y, int width, int height, String name, int level, int currentHp, int maxHp, Color hpColor) {
        g2d.setColor(new Color(250, 250, 250));
        g2d.fillRoundRect(x, y, width, height, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x, y, width, height, 12, 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(name + "  Lv." + level, x + 10, y + 25);

        int maxBarWidth = 220;
        int hpWidth = (int) ((currentHp / (float) maxHp) * maxBarWidth);
        g2d.setColor(hpColor);
        g2d.fillRect(x + 10, y + 40, hpWidth, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x + 10, y + 40, maxBarWidth, 15);
    }

    private void drawCommandBox(Graphics2D g2d) {
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
    }

    private void drawEndMessage(Graphics2D g2d) {
        if (!battleOver || endMessage == null) return;

        int boxWidth = getWidth() - 160;
        int boxHeight = 120;
        int boxX = 80;
        int boxY = getHeight() / 2 - 60;

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(endMessage);
        g2d.drawString(endMessage, (getWidth() - textWidth) / 2, getHeight() / 2 + 5);

        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Premi INVIO per continuare...", getWidth() / 2 - 100, getHeight() / 2 + 30);
    }


}
