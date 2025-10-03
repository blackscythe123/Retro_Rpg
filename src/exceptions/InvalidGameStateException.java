package exceptions;

/**
 * Exception thrown when the game is in an invalid state
 */
public class InvalidGameStateException extends Exception {
    public InvalidGameStateException(String message) {
        super(message);
    }

    public InvalidGameStateException(String message, Throwable cause) {
        super(message, cause);
    }
}