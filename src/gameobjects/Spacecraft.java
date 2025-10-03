package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Spacecraft game object for Space Shooter game
 */
public class Spacecraft extends MovableObject {
    private static final int BASE_SPEED = 7;

    private final Deque<Integer> shieldStacks = new ArrayDeque<>();
    private int tripleShotTimer;
    private int rapidFireTimer;
    private int speedBoostTimer;
    private int overdriveTimer;
    private int droneWingTimer;
    private int phaseShiftTimer;
    private int droneWingSpin;
    private int thrusterTick;
    private int maxHealth;
    private int health;
    private double speedScale = 1.0;

    public Spacecraft(int x, int y) {
        super(x, y, 42, 32, 0, 0);
        this.maxHealth = 5;
        this.health = maxHealth;
    }

    public void moveLeft() {
        velocityX = -getCurrentSpeed();
    }

    public void moveRight() {
        velocityX = getCurrentSpeed();
    }

    public void moveUp() {
        velocityY = -getCurrentSpeed();
    }

    public void moveDown() {
        velocityY = getCurrentSpeed();
    }

    public void stopHorizontalMoving() {
        velocityX = 0;
    }

    public void stopVerticalMoving() {
        velocityY = 0;
    }

    public void stopMoving() {
        velocityX = 0;
        velocityY = 0;
    }

    public boolean hasShield() {
        return !shieldStacks.isEmpty();
    }

    public void activateShield(int duration) {
        shieldStacks.addLast(Math.max(1, duration));
    }

    public boolean absorbHit() {
        if (!shieldStacks.isEmpty()) {
            shieldStacks.removeFirst();
            return true;
        }
        return false;
    }

    public int getShieldCharges() {
        return shieldStacks.size();
    }

    public double getPrimaryShieldSeconds() {
        if (shieldStacks.isEmpty()) {
            return 0;
        }
        return Math.max(0, shieldStacks.peekFirst()) / 60.0;
    }

    public void enableTripleShot(int duration) {
        tripleShotTimer = Math.min(1800, tripleShotTimer + duration);
    }

    public boolean hasTripleShot() {
        return tripleShotTimer > 0;
    }

    public void enableRapidFire(int duration) {
        rapidFireTimer = Math.min(1800, rapidFireTimer + duration);
    }

    public boolean hasRapidFire() {
        return rapidFireTimer > 0;
    }

    public void enableSpeedBoost(int duration) {
        speedBoostTimer = Math.min(1200, speedBoostTimer + duration);
    }

    public void enterOverdrive(int duration) {
        overdriveTimer = Math.min(1200, overdriveTimer + duration);
    }

    public boolean isOverdriveActive() {
        return overdriveTimer > 0;
    }

    public void enableDroneWing(int duration) {
        droneWingTimer = Math.min(1500, droneWingTimer + duration);
    }

    public boolean hasDroneWing() {
        return droneWingTimer > 0;
    }

    public void enablePhaseShift(int duration) {
        phaseShiftTimer = Math.min(900, phaseShiftTimer + duration);
    }

    public boolean isPhaseShiftActive() {
        return phaseShiftTimer > 0;
    }

    public int getFireDelay() {
        if (isOverdriveActive()) return 4;
        if (hasRapidFire()) return 8;
        return 16;
    }

    private int getCurrentSpeed() {
        int boost = speedBoostTimer > 0 || isOverdriveActive() ? 3 : 0;
        double adjusted = (BASE_SPEED + boost) * speedScale;
        return Math.max(3, (int) Math.round(adjusted));
    }

    public void setSpeedScale(double scale) {
        speedScale = Math.max(0.35, scale);
        if (velocityX != 0) {
            velocityX = velocityX > 0 ? getCurrentSpeed() : -getCurrentSpeed();
        }
        if (velocityY != 0) {
            velocityY = velocityY > 0 ? getCurrentSpeed() : -getCurrentSpeed();
        }
    }

    @Override
    public void update() {
        thrusterTick++;
        if (!shieldStacks.isEmpty()) {
            int remaining = shieldStacks.removeFirst() - 1;
            if (remaining > 0) {
                shieldStacks.addFirst(remaining);
            }
        }
        if (tripleShotTimer > 0) tripleShotTimer--;
        if (rapidFireTimer > 0) rapidFireTimer--;
        if (speedBoostTimer > 0) speedBoostTimer--;
        if (overdriveTimer > 0) overdriveTimer--;
        if (droneWingTimer > 0) droneWingTimer--;
        if (phaseShiftTimer > 0) phaseShiftTimer--;
        if (droneWingTimer > 0) {
            droneWingSpin = (droneWingSpin + 7) % 360;
        }

        super.move();

        if (x < 10) x = 10;
        if (x > 800 - width - 10) x = 800 - width - 10;
        if (y < 24) y = 24;
        if (y > 600 - height - 20) y = 600 - height - 20;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        health = Math.min(maxHealth, health + amount);
    }

    public void boostMaxHealth(int amount) {
        if (amount <= 0) return;
        maxHealth = Math.min(12, maxHealth + amount);
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public boolean applyDamage(int amount) {
        if (amount <= 0) {
            return false;
        }
        if (isPhaseShiftActive()) {
            return false;
        }
        if (!shieldStacks.isEmpty()) {
            shieldStacks.removeFirst();
            return false;
        }
        health = Math.max(0, health - amount);
        return health <= 0;
    }

    public double getTripleShotSeconds() {
        return Math.max(0, tripleShotTimer) / 60.0;
    }

    public double getRapidFireSeconds() {
        return Math.max(0, rapidFireTimer) / 60.0;
    }

    public double getSpeedBoostSeconds() {
        return Math.max(0, speedBoostTimer) / 60.0;
    }

    public double getOverdriveSeconds() {
        return Math.max(0, overdriveTimer) / 60.0;
    }

    public double getDroneWingSeconds() {
        return Math.max(0, droneWingTimer) / 60.0;
    }

    public double getPhaseShiftSeconds() {
        return Math.max(0, phaseShiftTimer) / 60.0;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int bodyX = x + 8;
        int bodyY = y + 8;
        int bodyWidth = width - 16;
        int bodyHeight = height - 16;

        GradientPaint bodyPaint = new GradientPaint(
                bodyX, bodyY,
                new Color(90, 177, 255),
                bodyX, bodyY + bodyHeight,
                new Color(32, 64, 220));
        g2d.setPaint(bodyPaint);
        g2d.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 16, 16);

        g2d.setColor(new Color(180, 220, 255));
        g2d.fillOval(x + 15, y + 6, 16, 16);

        g2d.setColor(new Color(50, 110, 255));
        g2d.fillRoundRect(x + 2, y + 14, 12, 18, 8, 8);
        g2d.fillRoundRect(x + width - 14, y + 14, 12, 18, 8, 8);

        g2d.setColor(new Color(240, 120, 40, 200));
        int thrusterHeight = 12 + (int) (Math.sin(thrusterTick * 0.4) * 4);
        g2d.fillRoundRect(x + 10, y + height - 6, 10, thrusterHeight, 6, 6);
        g2d.fillRoundRect(x + width - 20, y + height - 6, 10, thrusterHeight, 6, 6);

        if (hasTripleShot()) {
            g2d.setColor(new Color(255, 245, 120, 150));
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(bodyX - 6, bodyY - 6, bodyWidth + 12, bodyHeight + 12);
        }

        if (hasShield()) {
            int primaryTimer = shieldStacks.peekFirst();
            float alpha = (float) (0.6 + Math.sin(primaryTimer / 6.0) * 0.2);
            g2d.setColor(new Color(120, 210, 255, (int) (180 * alpha)));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(x - 6, y - 6, width + 12, height + 12);
            if (shieldStacks.size() > 1) {
                g2d.setColor(new Color(120, 210, 255, 80));
                g2d.drawOval(x - 10, y - 10, width + 20, height + 20);
            }
        }

        if (isOverdriveActive()) {
            g2d.setColor(new Color(255, 90, 90, 140));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawOval(x - 10, y - 10, width + 20, height + 20);
        }

        if (hasDroneWing()) {
            int cx = x + width / 2;
            int cy = y + height / 2;
            int radius = 24;
            double radiansPrimary = Math.toRadians(droneWingSpin);
            double radiansOpposite = Math.toRadians((droneWingSpin + 180) % 360);
            drawDrone(g2d, cx, cy, radius, radiansPrimary);
            drawDrone(g2d, cx, cy, radius, radiansOpposite);
        }

        if (isPhaseShiftActive()) {
            g2d.setColor(new Color(200, 210, 255, 120));
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawOval(x - 14, y - 14, width + 28, height + 28);
            g2d.drawOval(x - 20, y - 4, width + 40, height + 8);
        }

        g2d.dispose();
    }

    private void drawDrone(Graphics2D g2d, int cx, int cy, int radius, double angle) {
        int px = cx + (int) Math.round(Math.cos(angle) * radius);
        int py = cy + (int) Math.round(Math.sin(angle) * radius);
        g2d.setColor(new Color(110, 250, 210, 210));
        g2d.fillOval(px - 6, py - 6, 12, 12);
        g2d.setColor(new Color(40, 150, 160, 200));
        g2d.fillOval(px - 3, py - 3, 6, 6);
    }
}