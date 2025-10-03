package consoles;

import exceptions.CollisionException;
import gameobjects.Bird;
import gameobjects.FlappyPowerUp;
import gameobjects.Pillar;
import utils.GameObjectList;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Random;

/**
 * Console implementation for Flappy Bird game
 */
public class FlappyBirdConsole extends GamingConsole {
    private static final int FIELD_WIDTH = 800;
    private static final int FIELD_HEIGHT = 600;
    private static final int BASE_PILLAR_INTERVAL = 118;
    private static final int MIN_PILLAR_INTERVAL = 62;

    private Bird bird;
    private GameObjectList<Pillar> pillars;
    private GameObjectList<FlappyPowerUp> powerUps;
    private Random random;

    private int pillarSpawnTimer;
    private int powerUpSpawnTimer;
    private int nextPowerUpSpawn;
    private int backgroundTick;
    private int framesElapsed;
    private int difficultyMeter;

    private int combo;
    private int comboDecayTimer;
    private int bestCombo;

    private int scoreMultiplier = 1;
    private int multiplierTimer;
    private int slowMotionTimer;
    private int frenzyTimer;

    private String bannerText = "";
    private int bannerTimer;

    @Override
    protected void initializeGame() {
        random = new Random();
        bird = new Bird(120, FIELD_HEIGHT / 2);
        pillars = new GameObjectList<>();
        powerUps = new GameObjectList<>();

        pillarSpawnTimer = 0;
        powerUpSpawnTimer = 0;
        nextPowerUpSpawn = 240 + random.nextInt(300);
        backgroundTick = 0;
        framesElapsed = 0;
        difficultyMeter = 0;
        combo = 0;
        comboDecayTimer = 0;
        bestCombo = 0;
        scoreMultiplier = 1;
        multiplierTimer = 0;
        slowMotionTimer = 0;
        frenzyTimer = 0;
        bannerText = "";
        bannerTimer = 0;

        bird.setGravityScale(1.0);
        bird.setMaxFallSpeed(10.5);
        bird.setJumpForce(-8.8);

        gameObjects.add(bird);

        // Get the action started quickly with an early pillar spawn
        spawnPillar();
        pillarSpawnTimer = BASE_PILLAR_INTERVAL / 2;
    }

    @Override
    public void handleKeyPress(int keyCode) {
        if (handleCommonKeyPress(keyCode)) return;

        if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            bird.jump();
        }
    }

    @Override
    public void handleKeyRelease(int keyCode) {
        // No key release behaviour needed
    }

    @Override
    public void update() {
        if (gameOver || paused) return;

        framesElapsed++;
        backgroundTick++;
        updateTimers();
        syncSpeedModifiers();

        super.update();
    }

    private void updateTimers() {
        if (multiplierTimer > 0 && --multiplierTimer == 0) {
            scoreMultiplier = 1;
            announce("Multiplier faded", 70);
        }

        if (slowMotionTimer > 0 && --slowMotionTimer == 0) {
            announce("Time normalized", 90);
        }

        if (frenzyTimer > 0) {
            frenzyTimer--;
        }

        if (combo > 0) {
            if (comboDecayTimer > 0) {
                comboDecayTimer--;
            } else {
                combo = Math.max(0, combo - 1);
                comboDecayTimer = combo > 0 ? 80 : 0;
            }
        }

        if (bannerTimer > 0) {
            bannerTimer--;
        }

        if (framesElapsed % 480 == 0) {
            difficultyMeter = Math.min(12, difficultyMeter + 1);
        }

        applyFlightTuning();
    }

    private void applyFlightTuning() {
        double scale = 1.0;
        if (slowMotionTimer > 0) {
            scale *= 0.7;
            bird.setJumpForce(-7.8);
            bird.setMaxFallSpeed(8.5);
        } else if (frenzyTimer > 0) {
            scale *= 1.08;
            bird.setJumpForce(-9.2);
            bird.setMaxFallSpeed(11.5);
        } else {
            bird.setJumpForce(-8.8);
            bird.setMaxFallSpeed(10.5);
        }
        bird.setGravityScale(scale);
    }

    private void syncSpeedModifiers() {
        int baseSpeed = Math.min(8, 3 + score / 12 + difficultyMeter / 2);
        double modifier = getSpeedModifier();

        for (Pillar pillar : pillars) {
            pillar.setBaseSpeed(baseSpeed);
            pillar.applySpeedModifier(modifier);
        }

        for (FlappyPowerUp powerUp : powerUps) {
            powerUp.applySpeedModifier(Math.max(0.6, modifier * 0.85));
        }
    }

    private double getSpeedModifier() {
        double modifier = 1.0;
        if (slowMotionTimer > 0) {
            modifier *= 0.6;
        }
        if (frenzyTimer > 0) {
            modifier *= 1.18;
        }
        return modifier;
    }

    @Override
    protected void spawnObjects() {
        pillarSpawnTimer++;
        powerUpSpawnTimer++;

        int interval = Math.max(MIN_PILLAR_INTERVAL,
                BASE_PILLAR_INTERVAL - (score / 4) - difficultyMeter * 4);
        if (pillarSpawnTimer >= interval) {
            spawnPillar();
            pillarSpawnTimer = 0;
        }

        if (powerUpSpawnTimer >= nextPowerUpSpawn) {
            spawnPowerUp();
            powerUpSpawnTimer = 0;
            nextPowerUpSpawn = 260 + random.nextInt(360);
        }
    }

    private void spawnPillar() {
        int baseSpeed = Math.min(8, 3 + score / 12 + difficultyMeter / 2);

        int difficultyAdjustment = Math.min(60, score / 4 + difficultyMeter * 6);
        int gapHeight = Math.max(105, 180 - difficultyAdjustment + random.nextInt(24) - 12);

        int range = FIELD_HEIGHT - gapHeight - 140;
        int gapY = 80 + (range > 0 ? random.nextInt(range) : 0);

        boolean oscillates = score > 6 && random.nextFloat() < 0.35f;
        int amplitude = oscillates ? 18 + random.nextInt(22 + difficultyMeter * 4) : 0;
        int period = Math.max(28, 90 - difficultyMeter * 4 + random.nextInt(40));

        Color color = randomPillarColor();

        Pillar pillar = new Pillar(FIELD_WIDTH + 40, gapY, gapHeight, baseSpeed, oscillates, amplitude, period, color);
        pillar.applySpeedModifier(getSpeedModifier());

        pillars.add(pillar);
        gameObjects.add(pillar);
    }

    private void spawnPowerUp() {
        FlappyPowerUp.Type type;
        float roll = random.nextFloat();
        if (roll < 0.4f) {
            type = FlappyPowerUp.Type.SHIELD;
        } else if (roll < 0.7f) {
            type = FlappyPowerUp.Type.DOUBLE_SCORE;
        } else {
            type = FlappyPowerUp.Type.SLOW_TIME;
        }

        int y = 140 + random.nextInt(280);
        FlappyPowerUp powerUp = new FlappyPowerUp(FIELD_WIDTH + 20, y, type, Math.max(2, 3 + score / 20));
        powerUp.applySpeedModifier(Math.max(0.6, getSpeedModifier() * 0.85));

        powerUps.add(powerUp);
        gameObjects.add(powerUp);
    }

    @Override
    protected void checkCollisions() throws CollisionException {
        if (bird.getY() + bird.getHeight() >= FIELD_HEIGHT || bird.getY() <= 0) {
            throw new CollisionException("Bird hit the ground or ceiling!");
        }

        for (int i = 0; i < pillars.size(); i++) {
            Pillar pillar = pillars.get(i);

            if (!pillar.isCounted() && pillar.getRightEdge() < bird.getX()) {
                pillar.markCounted();
                rewardPass(pillar);
            }

            if (pillar.collidesWith(bird)) {
                if (bird.absorbHit()) {
                    logPillarEvent("SHIELD_SAVE", pillar, -1, true);
                    pillar.flash(36);
            announce("Shield saved you!", 90);
            gameObjects.remove(pillar);
            pillars.remove(pillar);
            i--;
                    continue;
                } else {
                    logPillarEvent("COLLISION", pillar, -1, true);
                    throw new CollisionException("Bird hit a pillar!");
                }
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            FlappyPowerUp powerUp = powerUps.get(i);
            if (bird.collidesWith(powerUp)) {
                applyPowerUp(powerUp);
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }
    }

    private void rewardPass(Pillar pillar) {
        int basePoints = 1;
        int gapCenter = pillar.getGapCenter();
        int birdCenter = bird.getY() + bird.getHeight() / 2;
        int clearance = Math.abs(gapCenter - birdCenter);
        int perfectThreshold = Math.max(10, pillar.getGapHeight() / 6);

        logPillarEvent("PASS", pillar, clearance, false);

        if (clearance <= perfectThreshold) {
            combo++;
            comboDecayTimer = 200;
            bestCombo = Math.max(bestCombo, combo);
            basePoints += 1 + combo / 3;
            pillar.flash(40);

            if (combo % 5 == 0) {
                frenzyTimer = 240;
                announce("Frenzy boost!", 90);
            } else {
                announce("Perfect! x" + combo, 80);
            }
        } else if (clearance > perfectThreshold * 2 && combo > 0) {
            combo = Math.max(0, combo - 1);
        }

        score += basePoints * scoreMultiplier;
    }

    private void applyPowerUp(FlappyPowerUp powerUp) {
        switch (powerUp.getType()) {
            case SHIELD:
                bird.activateShield(420);
                int charges = bird.getShieldCharges();
                announce(charges > 1 ? "Shield ready x" + charges + "!" : "Shield ready!", 100);
                break;
            case DOUBLE_SCORE:
                scoreMultiplier = Math.min(5, scoreMultiplier + 1);
                multiplierTimer = Math.min(1200, multiplierTimer + 600);
                announce("Multiplier x" + scoreMultiplier + "!", 100);
                break;
            case SLOW_TIME:
            default:
                slowMotionTimer = Math.min(960, slowMotionTimer + 320);
                bird.dampenVelocity(0.5);
                announce("Time warp " + String.format(Locale.US, "%.1fs", slowMotionTimer / 60.0), 110);
                break;
        }
        bird.flash(24);
        syncSpeedModifiers();
    }

    @Override
    protected void cleanupObjects() {
        for (int i = pillars.size() - 1; i >= 0; i--) {
            Pillar pillar = pillars.get(i);
            if (pillar.getRightEdge() < -80) {
                gameObjects.remove(pillar);
                pillars.remove(pillar);
            }
        }

        for (int i = powerUps.size() - 1; i >= 0; i--) {
            FlappyPowerUp powerUp = powerUps.get(i);
            if (powerUp.getX() + powerUp.getWidth() < -40) {
                gameObjects.remove(powerUp);
                powerUps.remove(powerUp);
            }
        }
    }

    @Override
    protected void drawUI(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("Score: " + score, 24, 46);

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Combo: " + combo + " (Best " + bestCombo + ")", 24, 70);

        if (scoreMultiplier > 1) {
            g2d.setColor(new Color(255, 200, 90));
            g2d.drawString("Multiplier x" + scoreMultiplier, 24, 92);
        }

        int statusY = 112;
        g2d.setFont(new Font("Arial", Font.PLAIN, 15));

        if (bird.isShieldActive()) {
            g2d.setColor(new Color(120, 204, 255));
            int charges = bird.getShieldCharges();
            double seconds = bird.getPrimaryShieldSeconds();
            String label = charges > 1 ? "Shield x" + charges : "Shield active";
            if (seconds > 0) {
                label += String.format(Locale.US, " (%.1fs)", seconds);
            }
            g2d.drawString(label, 24, statusY);
            statusY += 18;
        }

        if (slowMotionTimer > 0) {
            g2d.setColor(new Color(185, 160, 255));
            g2d.drawString(String.format(Locale.US, "Time warp %.1fs", slowMotionTimer / 60.0), 24, statusY);
            statusY += 18;
        }

        if (multiplierTimer > 0) {
            g2d.setColor(new Color(255, 220, 120));
            g2d.drawString(String.format(Locale.US, "Multiplier x%d (%.1fs)", scoreMultiplier, multiplierTimer / 60.0), 24, statusY);
            statusY += 18;
        }

    g2d.setColor(new Color(240, 240, 240));
    g2d.setFont(new Font("Arial", Font.PLAIN, 14));
    g2d.drawString("SPACE/UP: Flap | P: Pause | R: Restart | ESC: Exit", 20, FIELD_HEIGHT - 24);

        if (bannerTimer > 0 && bannerText != null && !bannerText.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(bannerText);
            int x = (FIELD_WIDTH - textWidth) / 2;
            int y = 86;
            g2d.setColor(new Color(0, 0, 0, 140));
            g2d.fillRoundRect(x - 18, y - 32, textWidth + 36, 48, 24, 24);
            g2d.setColor(Color.WHITE);
            g2d.drawString(bannerText, x, y);
        }

        if (gameOver) {
            g2d.setColor(new Color(255, 100, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 44));
            g2d.drawString("GAME OVER", 250, 250);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Score: " + score, 320, 288);
            g2d.drawString("Best Combo: " + bestCombo, 308, 316);
            g2d.drawString("Press R to restart", 300, 344);
            g2d.drawString("ESC to exit to menu", 286, 372);
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint sky = new GradientPaint(0, 0, new Color(22, 30, 60), 0, FIELD_HEIGHT, new Color(100, 185, 255));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        drawSun(g2d);
        drawCloudLayer(g2d, 0.35, 140, new Color(255, 255, 255, 180));
        drawCloudLayer(g2d, 0.6, 220, new Color(240, 240, 255, 150));
        drawHills(g2d);

        g2d.dispose();
    }

    private void drawSun(Graphics2D g2d) {
        int sunX = 640;
        int sunY = 110;
        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(sunX, sunY), 110f,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 236, 170), new Color(255, 236, 170, 0)});
        g2d.setPaint(paint);
        g2d.fillOval(sunX - 110, sunY - 110, 220, 220);
    }

    private void drawCloudLayer(Graphics2D g2d, double speed, int baseY, Color color) {
        int offset = (int) ((backgroundTick * speed) % 260);
        g2d.setColor(color);
        for (int i = -1; i < 5; i++) {
            int baseX = i * 260 - offset;
            g2d.fillRoundRect(baseX, baseY, 140, 40, 30, 30);
            g2d.fillRoundRect(baseX + 50, baseY - 18, 150, 48, 32, 32);
        }
    }

    private void drawHills(Graphics2D g2d) {
        int offset = (int) (backgroundTick * 0.8 % FIELD_WIDTH);
        g2d.setColor(new Color(42, 140, 78));
        for (int i = -1; i < 3; i++) {
            int baseX = i * 280 - offset;
            GeneralPath hill = new GeneralPath();
            hill.moveTo(baseX, FIELD_HEIGHT);
            hill.quadTo(baseX + 140, 520, baseX + 280, FIELD_HEIGHT);
            hill.closePath();
            g2d.fill(hill);
        }

        g2d.setColor(new Color(66, 160, 96));
        int groundOffset = (int) (backgroundTick * 1.4 % 80);
        for (int x = -groundOffset; x < FIELD_WIDTH; x += 80) {
            g2d.fillRect(x, FIELD_HEIGHT - 42, 60, 42);
        }

        g2d.setColor(new Color(28, 120, 68));
        g2d.fillRect(0, FIELD_HEIGHT - 36, FIELD_WIDTH, 36);
    }

    private void announce(String message, int durationFrames) {
        bannerText = message;
        bannerTimer = durationFrames;
    }

    private Color randomPillarColor() {
        float hue = 0.28f + random.nextFloat() * 0.08f;
        float saturation = 0.55f + random.nextFloat() * 0.15f;
        float brightness = 0.65f + random.nextFloat() * 0.25f;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    private void logPillarEvent(String tag, Pillar pillar, int clearance, boolean collision) {
        int birdLeft = bird.getX();
        int birdRight = birdLeft + bird.getWidth();
        int birdTop = bird.getY();
        int birdBottom = birdTop + bird.getHeight();
        int birdCenterY = birdTop + bird.getHeight() / 2;

        int pillarLeft = pillar.getX();
        int pillarRight = pillar.getRightEdge();
        int gapTop = pillar.getGapY();
        int gapBottom = gapTop + pillar.getGapHeight();

        System.out.printf(
                "[FLAPPY][%s] score=%d combo=%d bird[x=%d..%d,y=%d..%d,centerY=%d] pillar[x=%d..%d,gap=%d..%d,clearance=%d]%s%n",
                tag,
                score,
                combo,
                birdLeft,
                birdRight,
                birdTop,
                birdBottom,
                birdCenterY,
                pillarLeft,
                pillarRight,
                gapTop,
                gapBottom,
                clearance,
                collision ? " <-- collision" : ""
        );
    }
}