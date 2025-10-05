package utils;

import consoles.GamingConsole;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Game panel that handles rendering and input for games
 */
public class GamePanel extends JPanel {
    private GamingConsole console;

    public GamePanel(GamingConsole console) {
        this.console = console;
        setFocusable(true);
        addKeyListener(new GameKeyListener());
        addMouseListener(new GameMouseListener());
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

    private class GameMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
            if (console != null) {
                console.handleMousePressed(e.getButton(), e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (console != null) {
                console.handleMouseReleased(e.getButton(), e.getX(), e.getY());
            }
        }
    }
}