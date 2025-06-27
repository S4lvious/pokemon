package ui.states;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import engine.InputHandler;
import engine.models.ChoiceContext;
import main.GamePanel;

public class ChoiceState implements IGameState {

    private ChoiceContext currentChoice;
    private int selectedIndex = 0;

    private long lastNavTime = 0;
    private final long navCooldown = 150;

    public ChoiceState() {
        // Costruttore vuoto
    }

    public void configure(ChoiceContext context) {
        this.currentChoice = context;
        this.selectedIndex = 0;
    }

    @Override
    public void onEnter() {
        System.out.println("Entrando in ChoiceState...");
    }

    @Override
    public void update(GamePanel panel) {
        if (currentChoice == null) return;

        InputHandler input = panel.getInput();
        long now = System.currentTimeMillis();

        if (now - lastNavTime > navCooldown) {
            if (input.isPressed(KeyEvent.VK_DOWN)) {
                selectedIndex = (selectedIndex + 1) % currentChoice.getOptions().size();
                lastNavTime = now;
            } else if (input.isPressed(KeyEvent.VK_UP)) {
                selectedIndex = (selectedIndex - 1 + currentChoice.getOptions().size()) % currentChoice.getOptions().size();
                lastNavTime = now;
            }
        }

        if (input.isPressed(KeyEvent.VK_Z)) {
            currentChoice.getAction(selectedIndex).run();
            input.reset();
        } else if (input.isPressed(KeyEvent.VK_X)) {
            if (currentChoice.getCancelAction() != null) {
                currentChoice.getCancelAction().run();
            }
            input.reset();
        }
    }

    @Override
    public void draw(GamePanel panel, Graphics2D g) {
        if (currentChoice == null) return;

        int boxHeight = 120;
        int boxY = panel.getHeight() - boxHeight - 10;
        int boxX = 10;
        int boxWidth = panel.getWidth() - 20;

        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(boxX, boxY, boxWidth, boxHeight);
        g.setColor(Color.WHITE);
        g.drawRect(boxX, boxY, boxWidth, boxHeight);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        
        // --- LOGICA MODIFICATA ---
        // Usiamo il nostro nuovo metodo helper per disegnare la domanda
        int questionY = boxY + 30;
        int questionMaxWidth = boxWidth - 30; // Lasciamo un po' di padding
        int questionHeight = drawWrappedString(g, currentChoice.getQuestion(), boxX + 15, questionY, questionMaxWidth);

        // Disegniamo le opzioni sotto la domanda
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < currentChoice.getOptions().size(); i++) {
            String optionText = currentChoice.getOptions().get(i);
            if (i == selectedIndex) {
                g.setColor(Color.YELLOW);
                optionText = "> " + optionText;
            } else {
                g.setColor(Color.WHITE);
            }
            // Calcoliamo la Y delle opzioni basandoci sull'altezza occupata dalla domanda
            g.drawString(optionText, boxX + 25, questionY + questionHeight + 10 + (i * (fm.getHeight() + 5)));
        }
    }
    
    /**
     * NUOVO METODO HELPER per disegnare una stringa su più righe se necessario.
     * @return L'altezza totale in pixel occupata dal testo disegnato.
     */
    private int drawWrappedString(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(currentLine + " " + word) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }
        lines.add(currentLine.toString());

        int currentY = y;
        for (String line : lines) {
            g.drawString(line, x, currentY);
            currentY += fm.getHeight();
        }
        
        return (lines.size() * fm.getHeight());
    }

    @Override
    public void onExit() {
        this.currentChoice = null;
        System.out.println("Uscendo da ChoiceState...");
    }
}