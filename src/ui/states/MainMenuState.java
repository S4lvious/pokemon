package ui.states;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import engine.InputHandler;
import main.GamePanel;
import main.GameState;
import engine.LocalizationManager;

public class MainMenuState implements IGameState {

    private String[] menuOptions;
    private int menuSelection = 0;
    
    private long lastMenuNavTime = 0;
    private final long menuNavCooldown = 150;

    public MainMenuState() {
        // Il costruttore può rimanere vuoto, carichiamo i testi in onEnter
    }

    @Override
    public void onEnter() {
        this.menuSelection = 0;
        loadLocalizedTexts();
    }

    private void loadLocalizedTexts() {
        LocalizationManager lm = LocalizationManager.getInstance();
        menuOptions = new String[] {
            lm.getString("menu.pokemon"),
            lm.getString("menu.save"),
            lm.getString("menu.settings"),
            lm.getString("menu.exit")
        };
    }

    @Override
    public void update(GamePanel panel) {
        InputHandler input = panel.getInput();
        long now = System.currentTimeMillis();

        // Gestione uscita dal menu
        if (input.isPressed(KeyEvent.VK_X) && now - panel.getLastMenuToggleTime() > panel.getMenuToggleCooldown()) {
            panel.changeState(GameState.WORLD);
            return;
        }

        // Gestione navigazione menu con cooldown
        if (now - lastMenuNavTime > menuNavCooldown) {
            if (input.isPressed(KeyEvent.VK_DOWN)) {
                menuSelection = (menuSelection + 1) % menuOptions.length;
                lastMenuNavTime = now;
            } else if (input.isPressed(KeyEvent.VK_UP)) {
                menuSelection = (menuSelection - 1 + menuOptions.length) % menuOptions.length;
                lastMenuNavTime = now;
            }
        }

        // Gestione selezione opzione
        if (input.isPressed(KeyEvent.VK_Z)) {
            panel.handleMainMenuSelection(menuOptions[menuSelection]);
            input.reset();
        }
    }

    @Override
    public void draw(GamePanel panel, Graphics2D g) {
        int padding = 10;
        int lineSpacing = 5;

        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();

        int menuWidth = 0;
        for (String option : menuOptions) {
            menuWidth = Math.max(menuWidth, fm.stringWidth(option));
        }
        menuWidth += padding * 2;

        int menuHeight = (lineHeight * menuOptions.length) + (lineSpacing * (menuOptions.length - 1)) + (padding * 2);
        int menuX = panel.getWidth() - menuWidth - 10;
        int menuY = 10;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(menuX, menuY, menuWidth, menuHeight);
        g.setColor(Color.WHITE);
        g.drawRect(menuX, menuY, menuWidth, menuHeight);

        for (int i = 0; i < menuOptions.length; i++) {
            g.setColor(i == menuSelection ? Color.YELLOW : Color.WHITE);
            int textY = menuY + padding + fm.getAscent() + (i * (lineHeight + lineSpacing));
            g.drawString(menuOptions[i], menuX + padding, textY);
        }
    }
    
    @Override
    public void onExit() {
        // Nulla da fare qui per ora
    }
}
