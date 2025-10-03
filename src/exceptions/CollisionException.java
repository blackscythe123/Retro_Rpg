package exceptions;

/**
 * Exception thrown when a collision occurs in the game
 */
public class CollisionException extends Exception {
    public CollisionException(String message) {
        super(message);
    }

    public CollisionException(String message, Throwable cause) {
        super(message, cause);
    }
}