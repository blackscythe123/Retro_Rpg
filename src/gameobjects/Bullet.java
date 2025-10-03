package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Bullet projectile for Space Shooter game
 */
public class Bullet extends MovableObject {
    private int damage;
    private int lifetime;
    private Color coreColor;
    private Color trailColor;
    private final int baseVelocityX;
    private final int baseVelocityY;

    public Bullet(int x, int y) {
        this(x, y, 0, -12, 1);
    }

    public Bullet(int x, int y, int velocityX, int velocityY, int damage) {
        super(x, y, 5, 12, velocityX, velocityY);
        this.damage = Math.max(1, damage);
        this.lifetime = 120;
        this.coreColor = new Color(255, 238, 150);
        this.trailColor = new Color(255, 140, 40);
        this.baseVelocityX = velocityX;
        this.baseVelocityY = velocityY;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = Math.max(1, damage);
    }

    public void setColors(Color core, Color trail) {
        if (core != null) this.coreColor = core;
        if (trail != null) this.trailColor = trail;
    }

    public void applySpeedMultiplier(double multiplier) {
        double factor = Math.max(0.25, multiplier);
        setVelocityX((int) Math.round(baseVelocityX * factor));
        setVelocityY((int) Math.round(baseVelocityY * factor));
    }

    @Override
    public void update() {
        lifetime--;
        super.move();
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint paint = new GradientPaint(x, y, trailColor, x, y + height, coreColor);
        g2d.setPaint(paint);
        g2d.fillRoundRect(x, y, width, height, 6, 6);

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(255, 255, 255, 140));
        g2d.drawLine(x + width / 2, y - 4, x + width / 2, y);

        g2d.dispose();
    }
}