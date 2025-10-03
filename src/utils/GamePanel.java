package utils;

import consoles.GamingConsole;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Game panel that handles rendering and input for games
 */
public class GamePanel extends JPanel {
    private GamingConsole console;

    public GamePanel(GamingConsole console) {
        this.console = console;
        setFocusable(true);
        addKeyListener(new GameKeyListener());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (console != null) {
            console.draw(g);
        }
    }

    /**
     * Key listener for game input
     */
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (console != null) {
                console.handleKeyPress(e.getKeyCode());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (console != null) {
                console.handleKeyRelease(e.getKeyCode());
            }
        }
    }
}