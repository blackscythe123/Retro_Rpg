package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Projectile fired by enemy saucers.
 */
public class EnemyProjectile extends MovableObject {
    private final int damage;
    private int lifetime;
    private final int baseVelocityX;
    private final int baseVelocityY;

    public EnemyProjectile(int x, int y, int velocityX, int velocityY, int damage) {
        super(x, y, 6, 14, velocityX, velocityY);
        this.damage = Math.max(1, damage);
        this.lifetime = 180;
        this.baseVelocityX = velocityX;
        this.baseVelocityY = velocityY;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public void applySpeedMultiplier(double multiplier) {
        double factor = Math.max(0.2, multiplier);
        setVelocityX((int) Math.round(baseVelocityX * factor));
        setVelocityY((int) Math.round(baseVelocityY * factor));
    }

    @Override
    public void update() {
        lifetime--;
        super.move();
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint paint = new GradientPaint(x, y, new Color(255, 120, 120), x, y + height, new Color(255, 20, 20));
        g2d.setPaint(paint);
        g2d.fillRoundRect(x, y, width, height, 6, 6);

        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(255, 255, 255, 140));
        g2d.drawLine(x + width / 2, y + height, x + width / 2, y + height + 6);

        g2d.dispose();
    }
}
