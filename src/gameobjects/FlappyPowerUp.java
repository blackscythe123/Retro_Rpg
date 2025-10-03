package gameobjects;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

/**
 * Collectible power-up for the Flappy Bird console.
 */
public class FlappyPowerUp extends MovableObject {
    public enum Type {
        SHIELD,
        DOUBLE_SCORE,
        SLOW_TIME
    }

    private final Type type;
    private final int baseSpeed;
    private final int anchorY;
    private int tick;
    private final Color accentColor;

    public FlappyPowerUp(int x, int y, Type type, int travelSpeed) {
        super(x, y, 30, 30, -Math.max(2, travelSpeed), 0);
        this.type = type;
        this.baseSpeed = Math.max(2, travelSpeed);
        this.anchorY = y;
        this.accentColor = resolveAccent(type);
    }

    private Color resolveAccent(Type type) {
        switch (type) {
            case SHIELD:
                return new Color(120, 204, 255);
            case DOUBLE_SCORE:
                return new Color(255, 200, 90);
            case SLOW_TIME:
            default:
                return new Color(185, 160, 255);
        }
    }

    public Type getType() {
        return type;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public void applySpeedModifier(double modifier) {
        int adjusted = (int) Math.round(baseSpeed * modifier);
        velocityX = -Math.max(1, adjusted);
    }

    @Override
    public void update() {
        tick++;
        super.move();
        int bob = (int) Math.round(Math.sin((tick + type.ordinal() * 12) / 10.0) * 6);
        y = anchorY + bob;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = (float) (Math.sin(tick / 8.0) * 0.25 + 0.75);
        int auraSize = 10;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * pulse + 0.4f));
        g2d.setColor(accentColor);
        g2d.fillOval(x - auraSize / 2, y - auraSize / 2, width + auraSize, height + auraSize);
        g2d.setComposite(AlphaComposite.SrcOver);

        g2d.setColor(new Color(250, 250, 250));
        g2d.fillOval(x + 2, y + 2, width - 4, height - 4);

        g2d.setStroke(new BasicStroke(2.5f));
        g2d.setColor(accentColor.darker());
        g2d.drawOval(x + 2, y + 2, width - 4, height - 4);

        g2d.setColor(accentColor.darker().darker());
        switch (type) {
            case SHIELD -> drawShield(g2d);
            case DOUBLE_SCORE -> drawStar(g2d);
            case SLOW_TIME -> drawHourglass(g2d);
        }

        g2d.dispose();
    }

    private void drawShield(Graphics2D g2d) {
        GeneralPath shield = new GeneralPath();
        shield.moveTo(x + width / 2.0, y + 6);
        shield.lineTo(x + width - 8, y + height / 2.5);
        shield.lineTo(x + width / 2.0, y + height - 6);
        shield.lineTo(x + 8, y + height / 2.5);
        shield.closePath();
        g2d.fill(shield);
    }

    private void drawStar(Graphics2D g2d) {
        GeneralPath star = new GeneralPath();
        double centerX = x + width / 2.0;
        double centerY = y + height / 2.0;
        double outer = width / 2.4;
        double inner = outer / 2.4;
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i - Math.PI / 2;
            double radius = (i % 2 == 0) ? outer : inner;
            double px = centerX + Math.cos(angle) * radius;
            double py = centerY + Math.sin(angle) * radius;
            if (i == 0) {
                star.moveTo(px, py);
            } else {
                star.lineTo(px, py);
            }
        }
        star.closePath();
        g2d.fill(star);
    }

    private void drawHourglass(Graphics2D g2d) {
        GeneralPath hourglass = new GeneralPath();
        hourglass.moveTo(x + 10, y + 6);
        hourglass.lineTo(x + width - 10, y + 6);
        hourglass.lineTo(x + width - 12, y + height / 2.0);
        hourglass.lineTo(x + width - 10, y + height - 6);
        hourglass.lineTo(x + 10, y + height - 6);
        hourglass.lineTo(x + 12, y + height / 2.0);
        hourglass.closePath();
        g2d.fill(hourglass);

        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(x + 12, y + 10, x + width - 12, y + height / 2 - 2);
        g2d.drawLine(x + 12, y + height - 10, x + width - 12, y + height / 2 + 2);
    }
}
