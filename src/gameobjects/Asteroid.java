package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Polygon;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Asteroid obstacle for Space Shooter game
 */
public class Asteroid extends MovableObject {
    private final int[] radii;
    private final int vertexCount;
    private final Color baseColor;
    private int health;
    private int value;
    private double rotation;
    private double spin;
    private double baseVelocityX;
    private double baseVelocityY;

    public Asteroid(int x, int y, int velocityX, int velocityY) {
        this(x, y, velocityX, velocityY, 34, 1);
    }

    public Asteroid(int x, int y, int velocityX, int velocityY, int size, int health) {
        super(x, y, size, size, velocityX, velocityY);
        this.vertexCount = 8;
        this.radii = new int[vertexCount];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double baseRadius = size / 2.0;
        for (int i = 0; i < vertexCount; i++) {
            radii[i] = (int) Math.round(baseRadius * (0.65 + random.nextDouble() * 0.45));
        }

        float hue = 0.04f + random.nextFloat() * 0.08f;
        float saturation = 0.35f + random.nextFloat() * 0.25f;
        float brightness = 0.55f + random.nextFloat() * 0.25f;
        this.baseColor = Color.getHSBColor(hue, saturation, brightness);

        this.health = Math.max(1, health);
        this.value = Math.max(10, size * 2);
        this.rotation = random.nextDouble() * Math.PI * 2;
        this.spin = (random.nextDouble() - 0.5) * 0.15;
        this.baseVelocityX = velocityX;
        this.baseVelocityY = velocityY;
    }

    public double getBaseVelocityX() {
        return baseVelocityX;
    }

    public double getBaseVelocityY() {
        return baseVelocityY;
    }

    public void applySpeedMultiplier(double multiplier) {
        double factor = Math.max(0.2, multiplier);
        setVelocityX((int) Math.round(baseVelocityX * factor));
        setVelocityY((int) Math.round(baseVelocityY * factor));
    }

    public int getHealth() {
        return health;
    }

    public int getScoreValue() {
        return value;
    }

    public void damage(int amount) {
        health -= Math.max(1, amount);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public boolean isLarge() {
        return width >= 40;
    }

    public int getSizeCategory() {
        if (width >= 42) return 3;
        if (width >= 30) return 2;
        return 1;
    }

    public void setSpin(double spin) {
        this.spin = spin;
    }

    @Override
    public void update() {
        super.move();
        rotation += spin;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Polygon polygon = new Polygon();
        double angleStep = Math.PI * 2 / vertexCount;
        for (int i = 0; i < vertexCount; i++) {
            double angle = rotation + angleStep * i;
            int px = (int) Math.round(Math.cos(angle) * radii[i]);
            int py = (int) Math.round(Math.sin(angle) * radii[i]);
            polygon.addPoint(px, py);
        }

        int centerX = x + width / 2;
        int centerY = y + height / 2;

        g2d.translate(centerX, centerY);
        g2d.setColor(baseColor);
        g2d.fillPolygon(polygon);
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.setColor(baseColor.darker());
        g2d.drawPolygon(polygon);

        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fillOval(-width / 4, -height / 4, width / 2, height / 3);

        g2d.dispose();
    }
}