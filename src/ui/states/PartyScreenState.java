package ui.states;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

import engine.InputHandler;
import entities.Player;
import entities.Pokemon;
import main.GamePanel;
import main.GameState;
import engine.LocalizationManager;

public class PartyScreenState implements IGameState {

    private Player player;
    private int selectedIndex = 0;
    private int swapIndex = -1; // -1 significa che non stiamo scambiando nulla

    private long lastNavTime = 0;
    private final long navCooldown = 150;

    public PartyScreenState(Player player) {
        this.player = player;
    }

    @Override
    public void onEnter() {
        this.selectedIndex = 0;
        this.swapIndex = -1; // Resetta tutto quando si apre la schermata
        System.out.println("Entrando in PartyScreenState...");
    }

    @Override
    public void update(GamePanel panel) {
        InputHandler input = panel.getInput();
        long now = System.currentTimeMillis();

        // Uscita dalla schermata
        if (input.isPressed(KeyEvent.VK_X) || input.isPressed(KeyEvent.VK_ESCAPE)) {
            if (swapIndex != -1) {
                swapIndex = -1; // Se stiamo scambiando, 'X' annulla lo scambio
                input.reset();
            } else {
                 if (now - panel.getLastMenuToggleTime() > panel.getMenuToggleCooldown()) {
                    panel.changeState(GameState.WORLD);
                }
            }
            return;
        }

        // Navigazione SU/GIÙ con cooldown
        if (now - lastNavTime > navCooldown) {
            if (input.isPressed(KeyEvent.VK_DOWN)) {
                if (selectedIndex < player.getParty().getSize() - 1) {
                    selectedIndex++;
                    lastNavTime = now;
                }
            } else if (input.isPressed(KeyEvent.VK_UP)) {
                if (selectedIndex > 0) {
                    selectedIndex--;
                    lastNavTime = now;
                }
            }
        }
        
        // Logica di selezione e scambio con 'Z'
        if (input.isPressed(KeyEvent.VK_Z)) {
            if (swapIndex == -1) {
                // Iniziamo lo scambio: memorizziamo l'indice del primo Pokémon
                swapIndex = selectedIndex;
            } else {
                // Finiamo lo scambio: scambiamo il vecchio indice con quello nuovo
                player.getParty().swap(swapIndex, selectedIndex);
                swapIndex = -1; // Resettiamo lo stato di scambio
            }
            input.reset(); // Evita doppie selezioni
        }
    }

    @Override
    public void draw(GamePanel panel, Graphics2D g) {
        drawMenuBackground(panel, g);

        List<Pokemon> party = player.getParty().getPokemonList();
        g.setFont(new Font("Arial", Font.PLAIN, 18));

        for (int i = 0; i < party.size(); i++) {
            Pokemon p = party.get(i);
            int y = 100 + i * 50;

            // Evidenziamo il Pokémon selezionato per lo scambio (se c'è)
            if (swapIndex != -1 && i == swapIndex) {
                 g.setColor(new Color(255, 0, 0, 100)); // Rosso per il primo Pokémon selezionato
                 g.fillRect(panel.getWidth() / 2 - 160, y - 22, 320, 30);
            }
            // Evidenziamo il Pokémon attualmente puntato dal cursore
            else if (i == selectedIndex) {
                g.setColor(new Color(255, 255, 0, 100)); // Giallo per la selezione corrente
                g.fillRect(panel.getWidth() / 2 - 160, y - 22, 320, 30);
            }

            g.setColor(Color.WHITE);
            String pokemonStatus = String.format("%s  %s%d  %s %d/%d",
                p.getName(),
                LocalizationManager.getInstance().getString("party.levelAbbr"), p.getLevel(),
                LocalizationManager.getInstance().getString("party.hpLabel"), p.getCurrentHp(), p.getMaxHp()
            );
            
            FontMetrics fm = g.getFontMetrics();
            g.drawString(pokemonStatus, panel.getWidth() / 2 - fm.stringWidth(pokemonStatus) / 2, y);
        }
        
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String controls = LocalizationManager.getInstance().getString("party.controls");
        FontMetrics fmControls = g.getFontMetrics();
        g.drawString(controls, panel.getWidth() / 2 - fmControls.stringWidth(controls) / 2, panel.getHeight() - 40);
    }
    
    private void drawMenuBackground(GamePanel panel, Graphics2D g) {
        float borderPercentage = 0.10f;
        int borderX = (int) (panel.getWidth() * borderPercentage);
        int borderY = (int) (panel.getHeight() * borderPercentage);
        int menuWidth = panel.getWidth() - (borderX * 2);
        int menuHeight = panel.getHeight() - (borderY * 2);

        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(borderX, borderY, menuWidth, menuHeight);
        g.setColor(Color.WHITE);
        g.drawRect(borderX, borderY, menuWidth, menuHeight);
        
        String title = LocalizationManager.getInstance().getString("party.title");
        g.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, panel.getWidth() / 2 - fm.stringWidth(title) / 2, borderY + fm.getAscent() + 20);
    }

    @Override
    public void onExit() {
        System.out.println("Uscendo da PartyScreenState...");
    }
}