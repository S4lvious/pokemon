package ui;

import entities.Player;
import entities.Pokemon;

import engine.LocalizationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class PartyScreen extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Player player;
    private int selectedIndex = 0;
    private int swapIndex = -1;

    public PartyScreen(Player player) {
        this.player = player;
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e.getKeyCode());
            }
        });
    }

    private void handleInput(int keyCode) {
        List<Pokemon> party = player.getParty();

        switch (keyCode) {
            case KeyEvent.VK_UP:
                if (selectedIndex > 0) selectedIndex--;
                break;
            case KeyEvent.VK_DOWN:
                if (selectedIndex < party.size() - 1) selectedIndex++;
                break;
            case KeyEvent.VK_Z:
            case KeyEvent.VK_ENTER:
                if (swapIndex == -1) {
                    swapIndex = selectedIndex;
                } else {
                    // Swap Pokémon
                    Pokemon temp = party.get(swapIndex);
                    party.set(swapIndex, party.get(selectedIndex));
                    party.set(selectedIndex, temp);
                    swapIndex = -1;
                }
                break;
            case KeyEvent.VK_X:
            case KeyEvent.VK_ESCAPE:
                // Chiudi lo screen
                SwingUtilities.getWindowAncestor(this).dispose();
                break;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Otteniamo un'istanza del nostro gestore di localizzazione
        LocalizationManager lm = LocalizationManager.getInstance();
        List<Pokemon> party = player.getParty();

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < party.size(); i++) {
            Pokemon p = party.get(i);
            int y = 50 + i * 50;
            if (i == selectedIndex) {
                g.setColor(Color.YELLOW);
                g.fillRect(40, y - 20, getWidth() - 80, 40); // Larghezza dinamica
            }
            g.setColor(Color.BLACK);
            
            // Costruiamo la stringa di stato del Pokémon usando le chiavi localizzate
            String pokemonStatus = String.format("%s  %s%d  %s %d/%d",
                p.getName(),
                lm.getString("party.levelAbbr"), p.getLevel(),
                lm.getString("party.hpLabel"), p.getCurrentHp(), p.getMaxHp()
            );
            g.drawString(pokemonStatus, 50, y);
        }

        if (swapIndex != -1) {
            g.setColor(Color.RED);
            g.drawString(lm.getString("party.swapPrompt"), 50, getHeight() - 50);
        } else {
            g.setColor(Color.GRAY);
            g.drawString(lm.getString("party.controls"), 50, getHeight() - 50);
        }
    }
}
