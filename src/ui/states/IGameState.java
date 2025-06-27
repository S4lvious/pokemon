package ui.states;

import java.awt.Graphics2D;
import main.GamePanel;

public interface IGameState {
    
    /**
     * Aggiorna la logica dello stato.
     * @param panel il GamePanel principale per accedere a dati e metodi globali.
     */
    void update(GamePanel panel);
    
    /**
     * Disegna l'interfaccia dello stato corrente.
     * @param panel il GamePanel, utile per ottenere dimensioni come getWidth().
     * @param g l'oggetto Graphics2D su cui disegnare.
     */
    void draw(GamePanel panel, Graphics2D g);
    
    /**
     * Chiamato quando si entra in questo stato.
     */
    void onEnter();
    
    /**
     * Chiamato quando si esce da questo stato.
     */
    void onExit();
}