package ui.states;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import engine.InputHandler;
import main.GamePanel;
import main.GameState;
import engine.LocalizationManager;

public class SettingsMenuState implements IGameState {

    public SettingsMenuState() {
        // Il costruttore può rimanere vuoto per ora.
    }

    @Override
    public void onEnter() {
        // Questo metodo verrà chiamato quando apriamo le impostazioni.
        // Utile per inizializzare le opzioni del menu in futuro.
        System.out.println("Entrando in SettingsMenuState...");
    }

    @Override
    public void update(GamePanel panel) {
        InputHandler input = panel.getInput();
        long now = System.currentTimeMillis();

        // Per ora, l'unica interazione possibile è uscire con 'X' o 'ESC'.
        if ((input.isPressed(KeyEvent.VK_X) || input.isPressed(KeyEvent.VK_ESCAPE)) 
                && now - panel.getLastMenuToggleTime() > panel.getMenuToggleCooldown()) {
            panel.changeState(GameState.WORLD); // Torniamo direttamente al mondo di gioco
        }
    }

    @Override
    public void draw(GamePanel panel, Graphics2D g) {
        // --- Logica di disegno del menu a schermo intero ---

        // 1. Calcoliamo le dimensioni del menu con un bordo del 10%
        float borderPercentage = 0.10f;
        int borderX = (int) (panel.getWidth() * borderPercentage);
        int borderY = (int) (panel.getHeight() * borderPercentage);

        int menuX = borderX;
        int menuY = borderY;
        int menuWidth = panel.getWidth() - (borderX * 2);
        int menuHeight = panel.getHeight() - (borderY * 2);

        // 2. Disegniamo lo sfondo semi-trasparente
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(menuX, menuY, menuWidth, menuHeight);
        
        // 3. Disegniamo un bordo bianco per rifinire
        g.setColor(Color.WHITE);
        g.drawRect(menuX, menuY, menuWidth, menuHeight);
        
        // 4. Disegniamo il titolo localizzato
        LocalizationManager lm = LocalizationManager.getInstance();
        String title = lm.getString("menu.settings");
        
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        
        // Centriamo il titolo orizzontalmente e lo posizioniamo vicino al bordo superiore del menu
        g.drawString(title, panel.getWidth() / 2 - titleWidth / 2, menuY + fm.getAscent() + 20);

        // In futuro, qui disegneremo le varie opzioni (Lingua, Volume, etc.)
    }

    @Override
    public void onExit() {
        System.out.println("Uscendo da SettingsMenuState...");
    }
}