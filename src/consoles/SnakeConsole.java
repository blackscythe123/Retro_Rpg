package consoles;

import exceptions.CollisionException;
import gameobjects.*;
import utils.GameObjectList;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Random;

/**
 * Revamped Snake game console featuring power-ups, hazards, combos, and adaptive difficulty.
 */
public class SnakeConsole extends GamingConsole {
    private static final int COMBO_DURATION_FRAMES = 180;
    private static final int DOUBLE_SCORE_DURATION = 360;
    private static final int SHIELD_DURATION = 420;
    private static final int SLOW_TIME_DURATION = 260;

    private Snake snake;
    private Food food;
    private Random random;
    private GameObjectList<SnakeObstacle> obstacles;
    private GameObjectList<SnakePowerUp> powerUps;

    private int obstacleSpawnTimer;
    private int powerUpSpawnTimer;
    private int comboTimer;
    private int comboCount;
    private int maxCombo;
    private int foodsEaten;
    private int baseSpeed;
    private int scoreMultiplier;
    private int multiplierTimer;
    private Deque<Integer> shieldTimers;
    private int slowTimeTimer;
    private int backgroundPulse;

    @Override
    protected void initializeGame() {
        random = new Random();
        snake = new Snake(200, 200);
        obstacles = new GameObjectList<>();
        powerUps = new GameObjectList<>();

        obstacleSpawnTimer = 0;
        powerUpSpawnTimer = 0;
        comboTimer = 0;
        comboCount = 0;
        maxCombo = 0;
        foodsEaten = 0;
    baseSpeed = 8;
    scoreMultiplier = 1;
        multiplierTimer = 0;
        if (shieldTimers == null) {
            shieldTimers = new ArrayDeque<>();
        } else {
            shieldTimers.clear();
        }
        slowTimeTimer = 0;
        backgroundPulse = 0;

    updateActiveSpeed();

        gameObjects.add(snake);
        spawnFood();
    }

    @Override
    public void handleKeyPress(int keyCode) {
        if (handleCommonKeyPress(keyCode)) {
            return;
        }

        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W:
                if (snake.getDirection() != 2) snake.setDirection(0);
                break;
            case java.awt.event.KeyEvent.VK_D:
                if (snake.getDirection() != 3) snake.setDirection(1);
                break;
            case java.awt.event.KeyEvent.VK_S:
                if (snake.getDirection() != 0) snake.setDirection(2);
                break;
            case java.awt.event.KeyEvent.VK_A:
                if (snake.getDirection() != 1) snake.setDirection(3);
                break;
        }
    }

    @Override
    public void handleKeyRelease(int keyCode) {
        // Snake doesn't need key release handling
    }

    @Override
    protected void spawnObjects() {
        backgroundPulse = (backgroundPulse + 1) % 360;

        if (comboTimer > 0) {
            comboTimer--;
            if (comboTimer == 0) {
                comboCount = 0;
            }
        }

        if (multiplierTimer > 0) {
            multiplierTimer--;
            if (multiplierTimer == 0) {
                scoreMultiplier = 1;
            }
        }

        if (!shieldTimers.isEmpty()) {
            int remaining = shieldTimers.removeFirst() - 1;
            if (remaining > 0) {
                shieldTimers.addFirst(remaining);
            }
        }

        if (slowTimeTimer > 0) {
            slowTimeTimer--;
            if (slowTimeTimer == 0) {
                updateActiveSpeed();
            }
        }

        obstacleSpawnTimer++;
        powerUpSpawnTimer++;

        int dynamicObstacleInterval = Math.max(90, 220 - score / 4);
        if (obstacleSpawnTimer >= dynamicObstacleInterval) {
            spawnObstacle();
            obstacleSpawnTimer = 0;
        }

        int dynamicPowerUpInterval = Math.max(240, 480 - score / 3);
        if (powerUpSpawnTimer >= dynamicPowerUpInterval) {
            spawnPowerUp();
            powerUpSpawnTimer = 0;
        }
    }

    private void spawnFood() {
        int x, y;
        int attempts = 0;
        do {
            x = random.nextInt(38) * 20;
            y = random.nextInt(28) * 20;
            attempts++;
        } while (attempts < 60 && isPositionOccupied(x, y));

        food = new Food(x, y);
        gameObjects.add(food);
    }

    private void spawnObstacle() {
        for (int attempts = 0; attempts < 40; attempts++) {
            int x = random.nextInt(38) * 20;
            int y = random.nextInt(28) * 20;
            if (!isPositionOccupied(x, y)) {
                SnakeObstacle obstacle = new SnakeObstacle(x, y);
                obstacles.add(obstacle);
                gameObjects.add(obstacle);
                return;
            }
        }
    }

    private void spawnPowerUp() {
        for (int attempts = 0; attempts < 40; attempts++) {
            int x = random.nextInt(38) * 20;
            int y = random.nextInt(28) * 20;
            if (!isPositionOccupied(x, y)) {
                SnakePowerUp.PowerUpType[] types = SnakePowerUp.PowerUpType.values();
                SnakePowerUp powerUp = new SnakePowerUp(x, y, types[random.nextInt(types.length)]);
                powerUps.add(powerUp);
                gameObjects.add(powerUp);
                return;
            }
        }
    }

    private void updateActiveSpeed() {
        double profileMultiplier = resolveProfileSpeedScale();
    int targetBase = slowTimeTimer > 0 ? Math.max(6, baseSpeed - 3) : baseSpeed;
        int scaledSpeed = (int) Math.round(targetBase * profileMultiplier);
        scaledSpeed = Math.max(4, Math.min(24, scaledSpeed));
        snake.setSpeed(scaledSpeed);
    }

    private double resolveProfileSpeedScale() {
        switch (getSpeedProfile()) {
            case RELAXED:
                return 0.6;
            case TURBO:
                return 1.25;
            case STANDARD:
            default:
                return 1.0;
        }
    }

    private boolean isPositionOccupied(int x, int y) {
        for (GameObject obj : gameObjects) {
            if (obj.getX() == x && obj.getY() == y) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void checkCollisions() throws CollisionException {
        if (snake.collidesWith(food)) {
            gameObjects.remove(food);
            snake.grow();

            foodsEaten++;
            comboCount++;
            comboTimer = COMBO_DURATION_FRAMES;
            if (comboCount > maxCombo) {
                maxCombo = comboCount;
            }

            if (foodsEaten % 5 == 0) {
                baseSpeed = Math.min(20, baseSpeed + 1);
                updateActiveSpeed();
            }

            int comboMultiplier = 1 + (comboCount / 3);
            int gained = 10 * comboMultiplier * Math.max(1, scoreMultiplier);
            score += gained;

            spawnFood();
        }

        if (snake.getX() < 0 || snake.getX() >= 800 ||
            snake.getY() < 0 || snake.getY() >= 600) {
            throw new CollisionException("Snake hit the wall!");
        }

        for (int i = 1; i < snake.getBody().size(); i++) {
            int[] segment = snake.getBody().get(i);
            if (snake.getX() == segment[0] && snake.getY() == segment[1]) {
                throw new CollisionException("Snake collided with itself!");
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            SnakePowerUp powerUp = powerUps.get(i);
            if (snake.collidesWith(powerUp)) {
                applyPowerUp(powerUp);
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }

        for (int i = obstacles.size() - 1; i >= 0; i--) {
            SnakeObstacle obstacle = obstacles.get(i);
            if (snake.collidesWith(obstacle)) {
                if (!shieldTimers.isEmpty()) {
                    shieldTimers.removeFirst();
                    gameObjects.remove(obstacle);
                    obstacles.remove(obstacle);
                } else {
                    throw new CollisionException("Snake slammed into a toxic crystal!");
                }
            }
        }
    }

    @Override
    protected void cleanupObjects() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            SnakePowerUp powerUp = powerUps.get(i);
            if (powerUp.isExpired()) {
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }
    }

    @Override
    protected void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Press Start 2P", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 40);
        g.drawString("Length: " + snake.getBody().size(), 20, 70);
        g.drawString("Speed: " + snake.getSpeed(), 20, 100);

        if (comboCount > 1) {
            g.setColor(new Color(255, 200, 0));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Combo x" + comboCount + " (" + (comboTimer / 20) + "s)", 20, 130);
        }

        g.setColor(new Color(180, 255, 255));
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Max Combo: x" + Math.max(1, maxCombo), 20, 160);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("WASD: Move | P: Pause | R: Restart | ESC: Exit", 20, 575);

        int hudX = 600;
        int hudY = 40;
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Power-Ups", hudX, hudY);
        hudY += 24;

        if (scoreMultiplier > 1) {
            g.setColor(new Color(255, 215, 0));
            g.drawString(String.format(Locale.US, "Score x%d - %.1fs", scoreMultiplier, multiplierTimer / 60.0), hudX, hudY);
            hudY += 20;
        }
        if (!shieldTimers.isEmpty()) {
            g.setColor(new Color(120, 200, 255));
            double seconds = shieldTimers.peekFirst() / 60.0;
            g.drawString(String.format(Locale.US, "Shield x%d - %.1fs", shieldTimers.size(), seconds), hudX, hudY);
            hudY += 20;
        }
        if (slowTimeTimer > 0) {
            g.setColor(new Color(160, 255, 160));
            g.drawString(String.format(Locale.US, "Slow Time - %.1fs", slowTimeTimer / 60.0), hudX, hudY);
        }

        if (gameOver) {
            g.setColor(new Color(255, 80, 80));
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.drawString("GAME OVER", 280, 260);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Final Score: " + score, 300, 300);
            g.drawString("Max Combo: x" + Math.max(1, maxCombo), 300, 330);
            g.drawString("Press R to restart", 300, 360);
            g.drawString("ESC to exit to menu", 300, 390);
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        float pulse = 0.5f + 0.3f * (float) Math.sin(backgroundPulse * Math.PI / 180);
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(10, 30, 10 + (int) (pulse * 60)),
                0, 600, new Color(10, 90, 40));
        g2.setPaint(gradient);
        g2.fillRect(0, 0, 800, 600);

        g2.setColor(new Color(255, 255, 255, 20));
        for (int x = 0; x < 800; x += 20) {
            g2.drawLine(x, 0, x, 600);
        }
        for (int y = 0; y < 600; y += 20) {
            g2.drawLine(0, y, 800, y);
        }

        g2.dispose();
    }

    private void applyPowerUp(SnakePowerUp powerUp) {
        switch (powerUp.getType()) {
            case DOUBLE_SCORE:
                scoreMultiplier = Math.min(5, scoreMultiplier + 1);
                multiplierTimer = Math.min(1200, multiplierTimer + DOUBLE_SCORE_DURATION);
                break;
            case SHIELD:
                shieldTimers.addLast(SHIELD_DURATION);
                break;
            case SLOW_TIME:
                slowTimeTimer = Math.min(900, slowTimeTimer + SLOW_TIME_DURATION);
                updateActiveSpeed();
                break;
        }
    }

    @Override
    protected void onSpeedProfileChanged(GameSpeedProfile newProfile) {
        updateActiveSpeed();
    }
}