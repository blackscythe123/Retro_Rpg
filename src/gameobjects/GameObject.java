package gameobjects;

import interfaces.Drawable;
import interfaces.Updatable;

/**
 * Abstract base class for all game objects
 * Implements both Drawable and Updatable interfaces
 */
public abstract class GameObject implements Drawable, Updatable {
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Getters and setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    /**
     * Checks if this object collides with another game object
     * @param other The other game object to check collision with
     * @return true if collision detected, false otherwise
     */
    public boolean collidesWith(GameObject other) {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }

    // Abstract methods to be implemented by subclasses
    @Override
    public abstract void draw(java.awt.Graphics g);

    @Override
    public abstract void update();
}