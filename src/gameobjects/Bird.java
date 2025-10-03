package gameobjects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Bird game object for Flappy Bird game
 */
public class Bird extends MovableObject {
    private static final double DEFAULT_GRAVITY = 0.60;
    private static final double DEFAULT_JUMP_FORCE = -8.8;
    private static final double DEFAULT_MAX_FALL_SPEED = 10.0;

    private double preciseY;
    private double velocity;
    private double gravity = DEFAULT_GRAVITY;
    private double gravityScale = 1.0;
    private double jumpForce = DEFAULT_JUMP_FORCE;
    private double maxFallSpeed = DEFAULT_MAX_FALL_SPEED;

    private int flapTick;
    private int glowTimer;
    private final Deque<Integer> shieldStacks = new ArrayDeque<>();
    private int trailTimer;

    public Bird(int x, int y) {
        super(x, y, 34, 28, 0, 0);
        this.preciseY = y;
        this.velocity = 0;
    }

    public void jump() {
        velocity = jumpForce;
        flapTick = 0;
        trailTimer = 10;
    }

    public void setGravityScale(double scale) {
        gravityScale = Math.max(0.2, Math.min(2.0, scale));
    }

    public void setGravity(double gravity) {
        this.gravity = Math.max(0.2, gravity);
    }

    public void setMaxFallSpeed(double maxFallSpeed) {
        this.maxFallSpeed = Math.max(4.0, maxFallSpeed);
    }

    public void setJumpForce(double jumpForce) {
        this.jumpForce = Math.min(-4.0, jumpForce);
    }

    public double getVelocity() {
        return velocity;
    }

    public void dampenVelocity(double factor) {
        velocity *= Math.max(0.1, Math.min(1.0, factor));
    }

    public void pushVertical(double delta) {
        velocity += delta;
    }

    public void activateShield(int durationFrames) {
        shieldStacks.addLast(Math.max(1, durationFrames));
        glowTimer = Math.max(glowTimer, 16);
    }

    public boolean isShieldActive() {
        return !shieldStacks.isEmpty();
    }

    public boolean absorbHit() {
        if (!shieldStacks.isEmpty()) {
            shieldStacks.removeFirst();
            glowTimer = Math.max(glowTimer, 28);
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

    public void flash(int frames) {
        glowTimer = Math.max(glowTimer, frames);
    }

    @Override
    public void update() {
        flapTick++;
        if (!shieldStacks.isEmpty()) {
            int remaining = shieldStacks.removeFirst() - 1;
            if (remaining > 0) {
                shieldStacks.addFirst(remaining);
            }
        }
        if (glowTimer > 0) glowTimer--;
        if (trailTimer > 0) trailTimer--;

        velocity += gravity * gravityScale;
        if (velocity > maxFallSpeed) velocity = maxFallSpeed;
        preciseY += velocity;

        if (preciseY < 0) {
            preciseY = 0;
            velocity = 0;
        } else if (preciseY + height > 600) {
            preciseY = 600 - height;
            velocity = 0;
        }

        y = (int) Math.round(preciseY);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double tilt = Math.max(-0.7, Math.min(0.8, velocity / maxFallSpeed));
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        AffineTransform original = g2d.getTransform();
        g2d.rotate(tilt, centerX, centerY);

        GradientPaint bodyPaint = new GradientPaint(
                x, y,
                new Color(255, 232, 120),
                x, y + height,
                new Color(247, 181, 47));
        g2d.setPaint(bodyPaint);
        g2d.fillRoundRect(x, y, width, height, 20, 20);

        int wingOffset = (int) (Math.sin(flapTick * 0.45) * 6);
        g2d.setColor(new Color(255, 210, 70));
        g2d.fillOval(x + 6, y + 10 + wingOffset, 18, 12);

        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + width - 16, y + 6, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + width - 12, y + 8, 5, 5);

        Polygon beak = new Polygon(
                new int[]{x + width - 4, x + width + 10, x + width},
                new int[]{y + 14, y + 18, y + 22},
                3);
        g2d.setColor(new Color(255, 145, 0));
        g2d.fillPolygon(beak);

        g2d.setTransform(original);

        if (trailTimer > 0) {
            float alpha = Math.max(0.1f, trailTimer / 14f);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 255, 255, 170));
            g2d.fillOval(x - 12, y + height / 2, 20, 12);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        if (glowTimer > 0) {
            float glowAlpha = Math.min(1f, glowTimer / 24f);
            g2d.setColor(new Color(255, 255, 255, (int) (140 * glowAlpha)));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawOval(x - 4, y - 4, width + 8, height + 8);
        }

        if (!shieldStacks.isEmpty()) {
            int primaryTimer = shieldStacks.peekFirst();
            float phase = (float) (Math.sin(primaryTimer / 6.0) * 0.25 + 0.75);
            g2d.setColor(new Color(135, 206, 255, (int) (160 * phase)));
            g2d.setStroke(new BasicStroke(4f));
            g2d.drawOval(x - 8, y - 8, width + 16, height + 16);
            g2d.setColor(new Color(173, 216, 255, (int) (90 * phase)));
            g2d.fillOval(x - 7, y - 7, width + 14, height + 14);
        }

        g2d.dispose();
    }
}