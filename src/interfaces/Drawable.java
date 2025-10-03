package interfaces;

import java.awt.Graphics;

/**
 * Interface for objects that can be drawn on screen
 */
public interface Drawable {
    /**
     * Draws the object using the provided Graphics context
     * @param g Graphics context for drawing
     */
    void draw(Graphics g);
}