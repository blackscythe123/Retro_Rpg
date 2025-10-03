package gameobjects;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

/**
 * Floating collectible that grants temporary bonuses in the Space Shooter console.
 */
public class SpacePowerUp extends MovableObject {
    public enum Type {
        SHIELD,
        TRIPLE_SHOT,
        RAPID_FIRE,
        TIME_SLOW,
        HULL_REPAIR,
        OVERDRIVE,
        HEART_CORE,
        DRONE_WING,
        PHASE_SHIFT,
        NOVA_BURST
    }

    private final Type type;
    private final int baseSpeed;
    private final int anchorY;
    private int tick;

    public SpacePowerUp(int x, int y, Type type, int speed) {
        super(x, y, 28, 28, -Math.max(2, speed), 0);
        this.type = type;
        this.baseSpeed = Math.max(2, speed);
        this.anchorY = y;
    }

    public Type getType() {
        return type;
    }

    public void applySpeedModifier(double modifier) {
        int adjusted = (int) Math.round(baseSpeed * modifier);
        velocityX = -Math.max(1, adjusted);
    }

    @Override
    public void update() {
        tick++;
        super.move();
        int offset = (int) Math.round(Math.sin(tick / 10.0) * 8);
        y = anchorY + offset;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = (float) (Math.sin(tick / 8.0) * 0.25 + 0.75);
        Color core = resolveCoreColor();
        Color halo = new Color(core.getRed(), core.getGreen(), core.getBlue(), 120);

        g2d.setColor(halo);
        g2d.fillOval(x - 6, y - 6, width + 12, height + 12);

        GradientPaint paint = new GradientPaint(x, y, core.brighter(), x, y + height, core.darker());
        g2d.setPaint(paint);
        g2d.fillOval(x, y, width, height);

        g2d.setStroke(new BasicStroke(2f));
        g2d.setColor(new Color(255, 255, 255, (int) (200 * pulse)));
        g2d.drawOval(x, y, width, height);

        g2d.setColor(Color.WHITE);
        switch (type) {
            case SHIELD:
                drawShield(g2d);
                break;
            case TRIPLE_SHOT:
                drawTripleShot(g2d);
                break;
            case RAPID_FIRE:
                drawRapidFire(g2d);
                break;
            case TIME_SLOW:
                drawHourglass(g2d);
                break;
            case HULL_REPAIR:
                drawRepair(g2d);
                break;
            case OVERDRIVE:
                drawOverdrive(g2d);
                break;
            case HEART_CORE:
                drawHeart(g2d);
                break;
            case DRONE_WING:
                drawDroneWing(g2d);
                break;
            case PHASE_SHIFT:
                drawPhaseShift(g2d);
                break;
            case NOVA_BURST:
                drawNovaBurst(g2d);
                break;
            default:
                break;
        }

        g2d.dispose();
    }

    private Color resolveCoreColor() {
        switch (type) {
            case SHIELD:
                return new Color(120, 210, 255);
            case TRIPLE_SHOT:
                return new Color(255, 215, 120);
            case RAPID_FIRE:
                return new Color(255, 140, 140);
            case TIME_SLOW:
                return new Color(170, 160, 255);
            case HULL_REPAIR:
                return new Color(140, 240, 190);
            case HEART_CORE:
                return new Color(255, 105, 180);
            case DRONE_WING:
                return new Color(110, 250, 210);
            case PHASE_SHIFT:
                return new Color(180, 200, 255);
            case NOVA_BURST:
                return new Color(255, 180, 80);
            case OVERDRIVE:
            default:
                return new Color(255, 120, 200);
        }
    }

    private void drawShield(Graphics2D g2d) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + width / 2.0, y + 6);
        path.lineTo(x + width - 8, y + height / 2.0);
        path.lineTo(x + width / 2.0, y + height - 6);
        path.lineTo(x + 8, y + height / 2.0);
        path.closePath();
        g2d.fill(path);
    }

    private void drawTripleShot(Graphics2D g2d) {
        g2d.fillOval(x + 6, y + 6, 6, 6);
        g2d.fillOval(x + width / 2 - 3, y + width / 2 - 3, 6, 6);
        g2d.fillOval(x + width - 12, y + 6, 6, 6);
    }

    private void drawRapidFire(Graphics2D g2d) {
        g2d.fillRect(x + width / 2 - 2, y + 4, 4, height - 8);
        g2d.fillRect(x + 10, y + height / 2 - 2, width - 20, 4);
    }

    private void drawHourglass(Graphics2D g2d) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + 8, y + 6);
        path.lineTo(x + width - 8, y + 6);
        path.lineTo(x + width - 12, y + height / 2.0);
        path.lineTo(x + width - 8, y + height - 6);
        path.lineTo(x + 8, y + height - 6);
        path.lineTo(x + 12, y + height / 2.0);
        path.closePath();
        g2d.fill(path);
    }

    private void drawRepair(Graphics2D g2d) {
        int cx = x + width / 2;
        int cy = y + height / 2;
        g2d.fillRect(cx - 3, y + 6, 6, height - 12);
        g2d.fillRect(x + 6, cy - 3, width - 12, 6);
    }

    private void drawOverdrive(Graphics2D g2d) {
        g2d.fillOval(x + width / 2 - 6, y + 6, 12, 12);
        g2d.fillRect(x + width / 2 - 2, y + 16, 4, 8);
    }

    private void drawHeart(Graphics2D g2d) {
        GeneralPath heart = new GeneralPath();
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        heart.moveTo(cx, cy + 6);
        heart.curveTo(cx + 10, cy - 2, cx + 8, cy - 14, cx, cy - 6);
        heart.curveTo(cx - 8, cy - 14, cx - 10, cy - 2, cx, cy + 6);
        g2d.fill(heart);
    }

    private void drawDroneWing(Graphics2D g2d) {
        int cx = x + width / 2;
        int cy = y + height / 2;
        g2d.fillOval(cx - 4, cy - 4, 8, 8);
        g2d.fillOval(x + 6, cy - 3, 6, 6);
        g2d.fillOval(x + width - 12, cy - 3, 6, 6);
        g2d.setStroke(new BasicStroke(1.6f));
        g2d.drawArc(x + 4, y + 6, width - 8, height - 12, 210, 120);
        g2d.drawArc(x + 4, y + 6, width - 8, height - 12, 30, 120);
    }

    private void drawPhaseShift(Graphics2D g2d) {
        GeneralPath loop = new GeneralPath();
        loop.moveTo(x + width / 2.0, y + 6);
        loop.curveTo(x + width + 6, y + 10, x + width - 4, y + height - 8, x + width / 2.0, y + height - 6);
        loop.curveTo(x - 4, y + height - 8, x + 6, y + 10, x + width / 2.0, y + 6);
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.draw(loop);
        g2d.fillOval(x + width / 2 - 4, y + height / 2 - 4, 8, 8);
    }

    private void drawNovaBurst(Graphics2D g2d) {
        GeneralPath star = new GeneralPath();
        star.moveTo(x + width / 2.0, y + 4);
        star.lineTo(x + width / 2.0 + 4, y + height / 2.0 - 2);
        star.lineTo(x + width - 4, y + height / 2.0 - 2);
        star.lineTo(x + width / 2.0 + 4, y + height / 2.0 + 2);
        star.lineTo(x + width / 2.0 + 2, y + height - 4);
        star.lineTo(x + width / 2.0, y + height / 2.0 + 2);
        star.lineTo(x + width / 2.0 - 4, y + height - 4);
        star.lineTo(x + width / 2.0 - 2, y + height / 2.0 + 2);
        star.lineTo(x + 4, y + height / 2.0 + 2);
        star.lineTo(x + width / 2.0 - 4, y + height / 2.0 - 2);
        star.closePath();
        g2d.fill(star);
    }
}
