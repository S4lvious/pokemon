package ui;

import entities.Party;
import entities.Player;
import entities.Pokemon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class PartyScreen extends JPanel {

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
        Party party = player.getParty();

        switch (keyCode) {
            case KeyEvent.VK_UP:
                if (selectedIndex > 0) selectedIndex--;
                break;
            case KeyEvent.VK_DOWN:
                if (selectedIndex < party.getSize() - 1) selectedIndex++;
                break;
            case KeyEvent.VK_Z:
            case KeyEvent.VK_ENTER:
                if (swapIndex == -1) {
                    swapIndex = selectedIndex;
                } else {
                    // Swap Pokémon
                    party.swap(swapIndex, selectedIndex);
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

        Party party = player.getParty();
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        int i = 0;
        for (Pokemon p : party.getExplicitParty()) {
            int y = 50 + i * 50;
            if (i == selectedIndex) {
                g.setColor(Color.YELLOW);
                g.fillRect(40, y - 20, 300, 40);
            }
            g.setColor(Color.BLACK);
            g.drawString(p.getName() + "  Lv." + p.getLevel() + "  HP: " + p.getCurrentHp() + "/" + p.getMaxHp(), 50, y);
            i++;
        }

        if (swapIndex != -1) {
            g.setColor(Color.RED);
            g.drawString("Seleziona Pokémon da scambiare...", 50, getHeight() - 50);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("Z/Enter: Seleziona | X/Esc: Esci", 50, getHeight() - 50);
        }
    }
}
