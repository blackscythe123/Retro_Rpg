package gameobjects;

/**
 * Abstract class for game objects that don't move
 */
public abstract class ImmovableObject extends GameObject {
    public ImmovableObject(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void update() {
        // Immovable objects don't need to update their position
    }
}