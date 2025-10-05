package consoles;

import exceptions.CollisionException;
import exceptions.InvalidGameStateException;
import gameobjects.GameObject;
import interfaces.Drawable;
import interfaces.Updatable;
import utils.GameObjectList;
import java.awt.Graphics;

/**
 * Abstract base class for all gaming consoles
 * Implements Template Method pattern for game loop
 */
public abstract class GamingConsole implements Drawable, Updatable {
    protected GameObjectList<GameObject> gameObjects;
    protected boolean gameOver;
    protected boolean paused;
    protected boolean exitToMenu;
    protected int score;
    protected GameSpeedProfile speedProfile;
    protected long startTimeMillis = 0L;

    public GamingConsole() {
        gameObjects = new GameObjectList<>();
        gameOver = false;
        paused = false;
        exitToMenu = false;
        score = 0;
        speedProfile = GameSpeedProfile.RELAXED;
        initializeGame();
    }

    /**
     * Start internal timer for measuring elapsed time during a match.
     */
    public void startTimer() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    /**
     * Reset or stop the timer.
     */
    public void resetTimer() {
        this.startTimeMillis = 0L;
    }

    /**
     * Returns elapsed milliseconds since startTimer() was called, or 0 if timer not started.
     */
    public long getElapsedMillis() {
        if (startTimeMillis == 0L) return 0L;
        return System.currentTimeMillis() - startTimeMillis;
    }

    public enum GameSpeedProfile {
        RELAXED("Relaxed", 0.7),
        STANDARD("Standard", 1.0),
        TURBO("Turbo", 1.25);

        private final String label;
        private final double timeScale;

        GameSpeedProfile(String label, double timeScale) {
            this.label = label;
            this.timeScale = timeScale;
        }

        public double getTimeScale() {
            return timeScale;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Template method for game initialization
     */
    protected abstract void initializeGame();

    /**
     * Template method for handling key presses
     */
    public abstract void handleKeyPress(int keyCode);

    /**
     * Template method for handling key releases
     */
    public abstract void handleKeyRelease(int keyCode);

    /**
     * Optional mouse press handler. Subclasses can override to support mouse controls.
     */
    public void handleMousePressed(int button, int x, int y) {
        // Default: no-op
    }

    /**
     * Optional mouse release handler. Subclasses can override to support mouse controls.
     */
    public void handleMouseReleased(int button, int x, int y) {
        // Default: no-op
    }

    /**
     * Handle common key presses (pause, restart, exit)
     */
    public boolean handleCommonKeyPress(int keyCode) {
        if (keyCode == java.awt.event.KeyEvent.VK_P) {
            paused = !paused;
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_R) {
            reset();
            return true;
        }
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            exitToMenu = true;
            gameOver = true;
            return true;
        }
        return false;
    }

    /**
     * Template method for spawning game objects
     */
    protected abstract void spawnObjects();

    /**
     * Template method for checking collisions
     */
    protected abstract void checkCollisions() throws CollisionException;

    /**
     * Template method for cleaning up objects
     */
    protected abstract void cleanupObjects();

    @Override
    public void update() {
        if (gameOver || paused) return;

        try {
            // Update all game objects
            gameObjects.updateAll();

            // Spawn new objects
            spawnObjects();

            // Check for collisions
            checkCollisions();

            // Clean up objects
            cleanupObjects();

        } catch (CollisionException e) {
            gameOver = true;
            handleGameOver();
        }
    }

    @Override
    public void draw(Graphics g) {
        // Clear background
        g.clearRect(0, 0, 800, 600);

        // Draw dynamic background layer (overridable)
        drawBackground(g);

        // Draw all game objects
        gameObjects.drawAll(g);

        // Draw UI elements
        drawUI(g);

        // Draw pause overlay
        if (paused) {
            g.setColor(new java.awt.Color(0, 0, 0, 150));
            g.fillRect(0, 0, 800, 600);
            g.setColor(java.awt.Color.WHITE);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
            g.drawString("PAUSED", 320, 300);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
            g.drawString("Press P to resume", 320, 340);
        }
    }

    /**
     * Draws the background layer before game objects.
     * Subclasses can override for themed backdrops.
     */
    protected void drawBackground(Graphics g) {
        // Default background is solid black for a retro vibe
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, 800, 600);
    }

    /**
     * Template method for drawing UI elements
     */
    protected abstract void drawUI(Graphics g);

    /**
     * Handle game over state
     */
    protected void handleGameOver() {
        System.out.println("Game Over! Final Score: " + score);
    }

    // Getters
    public boolean isGameOver() { return gameOver; }
    public boolean shouldExitToMenu() { return exitToMenu; }
    public int getScore() { return score; }
    public boolean isPaused() { return paused; }
    public GameSpeedProfile getSpeedProfile() { return speedProfile; }
    public double getTimeScale() { return speedProfile.getTimeScale(); }

    /**
     * Reset the game
     */
    public void reset() {
        gameObjects.clear();
        gameOver = false;
        paused = false;
        exitToMenu = false;
        score = 0;
        resetTimer();
        initializeGame();
    }

    public void pauseGame() {
        paused = true;
    }

    public void resumeGame() {
        paused = false;
    }

    public void togglePause() {
        paused = !paused;
    }

    public void setSpeedProfile(GameSpeedProfile profile) {
        if (profile != null && profile != this.speedProfile) {
            this.speedProfile = profile;
            onSpeedProfileChanged(profile);
        }
    }

    protected void onSpeedProfileChanged(GameSpeedProfile newProfile) {
        // Subclasses can override to react to speed changes
    }
}