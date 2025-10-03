package exceptions;

/**
 * Exception thrown when game initialization fails
 */
public class GameInitializationException extends Exception {
    public GameInitializationException(String message) {
        super(message);
    }

    public GameInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}