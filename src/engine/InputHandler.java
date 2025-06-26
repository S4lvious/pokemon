package engine;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class InputHandler extends KeyAdapter {

    private boolean[] keys = new boolean[256];

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;
    }

    public void reset() {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i]) {
                keys[i] = false;
            }
        }
    }
    

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    public boolean isPressed(int keyCode) {
        return keys[keyCode];
    }
}
