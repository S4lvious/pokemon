package ui.states;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import engine.InputHandler;
import entities.Player;
import main.GamePanel;
import main.GameState;
import world.WorldMap;


public class WorldState implements IGameState {

    private Player player;
    private WorldMap worldMap;
    
    // Il costruttore riceve gli oggetti del mondo con cui deve interagire.
    public WorldState(Player player, WorldMap worldMap) {
        this.player = player;
        this.worldMap = worldMap;
    }

    @Override
    public void update(GamePanel panel) {
        long now = System.currentTimeMillis();
        InputHandler input = panel.getInput(); // Otteniamo l'input handler dal GamePanel

        // --- Gestione Transizione di Stato ---
        // Se premiamo 'X', diciamo al GamePanel di cambiare stato.
        if (input.isPressed(KeyEvent.VK_X) && now - panel.getLastMenuToggleTime() > panel.getMenuToggleCooldown()) {
            panel.changeState(GameState.MAIN_MENU);
            return; // Usciamo per evitare di processare altro input in questo frame
        }

        // --- Logica di Movimento (spostata da GamePanel) ---
        boolean wantsToMove = false;
        int dx = 0, dy = 0;

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
        
        // La logica di movimento effettiva, che rispetta il cooldown
        if (wantsToMove && (now - player.getLastMoveTime() >= player.getMoveCooldown())) {
            if (worldMap.isWalkable(player.x + dx, player.y + dy)) {
                player.setLastMoveTime(now);
                player.move(dx, dy);
                if (worldMap.isGrassTile(player.x, player.y)) { 
                    // Logica per l'incontro con Pokémon selvatici
                }
            }
        }
        
        // Infine, aggiorniamo lo stato interno del giocatore (animazione)
        player.update();
    }

    @Override
    public void draw(GamePanel panel, Graphics2D g) { // Aggiunto il parametro GamePanel
        // Questa classe non disegna nulla direttamente.
        // Il GamePanel disegnerà sempre il mondo e il giocatore come sfondo.
    }

    @Override
    public void onEnter() {
        System.out.println("Entrando in WorldState...");
        // Qui potremmo, ad esempio, riattivare la musica del mondo di gioco.
    }

    @Override
    public void onExit() {
        System.out.println("Uscendo da WorldState...");
        // La riga "player.setMoving(false);" è stata rimossa perché obsoleta.
    }
}