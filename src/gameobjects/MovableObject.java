package gameobjects;

/**
 * Abstract class for game objects that can move
 */
public abstract class MovableObject extends GameObject {
    protected int velocityX;
    protected int velocityY;

    public MovableObject(int x, int y, int width, int height, int velocityX, int velocityY) {
        super(x, y, width, height);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    // Getters and setters for velocity
    public int getVelocityX() { return velocityX; }
    public void setVelocityX(int velocityX) { this.velocityX = velocityX; }

    public int getVelocityY() { return velocityY; }
    public void setVelocityY(int velocityY) { this.velocityY = velocityY; }

    /**
     * Moves the object based on its current velocity
     */
    public void move() {
        x += velocityX;
        y += velocityY;
    }

    @Override
    public void update() {
        move();
    }
}