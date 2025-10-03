package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Agile enemy craft that now hunts the player with targeted maneuvers.
 */
public class EnemySaucer extends MovableObject {
    public enum SpawnVector {
        LEFT,
        RIGHT,
        TOP
    }

    private final SpawnVector origin;
    private final Spacecraft target;
    private final int waveOffset;
    private final double baseSpeed;
    private final double lateralBias;
    private int health;
    private int fireCooldown;
    private int tick;
    private double globalMultiplier;
    private double enrageMultiplier;

    public EnemySaucer(int x, int y, SpawnVector origin, Spacecraft target) {
        super(x, y, 44, 24, 0, 0);
        this.origin = origin;
        this.target = target;
        this.waveOffset = (int) (Math.random() * 360);
        this.baseSpeed = 4.6;
        this.lateralBias = origin == SpawnVector.LEFT ? 0.9 : origin == SpawnVector.RIGHT ? -0.9 : 0.0;
        this.health = 4;
        this.fireCooldown = 80;
        this.tick = 0;
        this.globalMultiplier = 1.0;
        this.enrageMultiplier = 1.0;
    }

    public boolean readyToFire() {
        return fireCooldown <= 0;
    }

    public void resetFireCooldown(int frames) {
        fireCooldown = Math.max(34, frames);
    }

    public int getFireX() {
        return x + width / 2;
    }

    public int getFireY() {
        return y + height;
    }

    public void damage(int amount) {
        health -= Math.max(1, amount);
        enrageMultiplier = Math.min(1.7, enrageMultiplier + 0.12);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getScoreValue() {
        return 55;
    }

    public SpawnVector getOrigin() {
        return origin;
    }

    public boolean isOutOfBounds() {
        switch (origin) {
            case LEFT:
                return x > 860;
            case RIGHT:
                return x + width < -80;
            case TOP:
                return y > 660 || x + width < -120 || x > 920;
            default:
                return false;
        }
    }

    public void applySpeedMultiplier(double multiplier) {
        globalMultiplier = Math.max(0.4, multiplier);
    }

    @Override
    public void update() {
        tick++;
        if (fireCooldown > 0) fireCooldown--;

        double speedPulse = Math.sin((tick + waveOffset) / 90.0) * 0.4;
        double speed = (baseSpeed + speedPulse) * globalMultiplier * enrageMultiplier;

        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;

        double targetX = centerX;
        double targetY = centerY;
        if (target != null) {
            targetX = target.getX() + target.getWidth() / 2.0;
            targetY = target.getY() + target.getHeight() / 2.0;
        }

        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.hypot(dx, dy);
        if (distance < 1.0) distance = 1.0;
        dx /= distance;
        dy /= distance;

        double strafe = Math.sin((tick + waveOffset) / 30.0);
        double verticalWave = Math.cos((tick + waveOffset) / 42.0);

        double directionalBiasX = lateralBias;
        double directionalBiasY = 0.0;
        if (origin == SpawnVector.TOP) {
            directionalBiasX = strafe * 0.9;
            directionalBiasY = 0.9;
        } else {
            directionalBiasY = verticalWave * 0.45;
        }

        double desiredX = dx * 0.8 + directionalBiasX * 0.5;
        double desiredY = dy * 0.8 + directionalBiasY * 0.5;

        double norm = Math.hypot(desiredX, desiredY);
        if (norm < 1e-3) {
            desiredX = directionalBiasX;
            desiredY = origin == SpawnVector.TOP ? 1.0 : 0.2;
            norm = Math.hypot(desiredX, desiredY);
        }

        desiredX /= norm;
        desiredY /= norm;

        int velocityX = (int) Math.round(desiredX * speed * 2.1);
        int velocityY = (int) Math.round(desiredY * speed * 2.1);

        if (origin == SpawnVector.LEFT) {
            velocityX = Math.max(3, velocityX);
        } else if (origin == SpawnVector.RIGHT) {
            velocityX = Math.min(-3, velocityX);
        }

        setVelocityX(velocityX);
        setVelocityY(velocityY);

        super.move();

        if (origin != SpawnVector.TOP) {
            if (y < 40) y = 40;
            if (y > 440) y = 440;
        } else {
            if (x < 20) x = 20;
            if (x > 800 - width - 20) x = 800 - width - 20;
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint hullPaint = new GradientPaint(
                x, y,
                new Color(255, 120, 120),
                x, y + height,
                new Color(200, 40, 60));
        g2d.setPaint(hullPaint);
        g2d.fillRoundRect(x, y + 6, width, height - 6, 20, 20);

        g2d.setColor(new Color(255, 240, 180));
        g2d.fillOval(x + width / 2 - 14, y, 28, 14);

        g2d.setColor(new Color(255, 80, 80, 160));
        g2d.fillOval(x + width / 2 - 8, y + height - 6, 16, 12);

        g2d.setStroke(new BasicStroke(1.8f));
        g2d.setColor(new Color(120, 0, 40));
        g2d.drawRoundRect(x, y + 6, width, height - 6, 20, 20);
        g2d.dispose();
    }
}
