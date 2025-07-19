package engine;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class InputHandler extends KeyAdapter {

    private final Map<Integer, Boolean> keys = new HashMap<>();

    @Override
    public void keyPressed(KeyEvent e) {
        keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.put(e.getKeyCode(), false);
    }

    public boolean isPressed(int keyCode) {
        return keys.getOrDefault(keyCode, false);
    }

    public void reset() {
        keys.replaceAll((k, v) -> false);
    }
}
