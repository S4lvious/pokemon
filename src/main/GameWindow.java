package main;

import javax.swing.*;

public class GameWindow extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GameWindow() {
        setTitle("Pokemon Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);


        GamePanel gamePanel = new GamePanel(this);
        add(gamePanel);
        pack(); 

        setLocationRelativeTo(null); 
        setVisible(true);

    }
}
