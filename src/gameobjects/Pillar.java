package gameobjects;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Pillar obstacle for Flappy Bird game
 */
public class Pillar extends MovableObject {
    private static final int PLAYFIELD_HEIGHT = 600;
    private static final int GAP_MARGIN = 60;

    private int gapY; // Y position of the gap
    private int gapHeight; // Height of the gap
    private int baseSpeed;
    private boolean counted;

    private final boolean oscillates;
    private final int oscillationAmplitude;
    private final int oscillationPeriod;
    private int oscillationTick;
    private int baseGapY;

    private int highlightTimer;
    private final Color bodyColor;

    public Pillar(int x, int gapY, int gapHeight) {
        this(x, gapY, gapHeight, 3);
    }

    public Pillar(int x, int gapY, int gapHeight, int speed) {
        this(x, gapY, gapHeight, speed, false, 0, 120);
    }

    public Pillar(int x, int gapY, int gapHeight, int speed, boolean oscillates, int amplitude, int period) {
        this(x, gapY, gapHeight, speed, oscillates, amplitude, period, new Color(76, 187, 23));
    }

    public Pillar(int x, int gapY, int gapHeight, int speed, boolean oscillates, int amplitude, int period, Color color) {
        super(x, 0, 60, PLAYFIELD_HEIGHT, -Math.max(1, speed), 0);
        this.gapHeight = Math.max(80, Math.min(260, gapHeight));
        this.baseSpeed = Math.max(1, speed);
        this.oscillates = oscillates && amplitude > 0;
        this.oscillationAmplitude = Math.min(140, Math.max(6, amplitude));
        this.oscillationPeriod = Math.max(24, period);
        this.bodyColor = color != null ? color : new Color(76, 187, 23);
        setGapY(gapY);
        this.baseGapY = this.gapY;
    }

    public int getGapY() {
        return gapY;
    }

    public int getGapHeight() {
        return gapHeight;
    }

    public int getGapCenter() {
        return gapY + gapHeight / 2;
    }

    public int getRightEdge() {
        return x + width;
    }

    public boolean isCounted() {
        return counted;
    }

    public void markCounted() {
        counted = true;
    }

    public void setBaseSpeed(int speed) {
        baseSpeed = Math.max(1, speed);
        applySpeedModifier(1.0);
    }

    public void applySpeedModifier(double modifier) {
        int adjusted = (int) Math.round(baseSpeed * modifier);
        velocityX = -Math.max(1, adjusted);
    }

    public void flash(int frames) {
        highlightTimer = Math.max(highlightTimer, frames);
    }

    private void setGapY(int gapY) {
        int minGapY = GAP_MARGIN;
        int maxGapY = PLAYFIELD_HEIGHT - GAP_MARGIN - gapHeight;
        this.gapY = Math.max(minGapY, Math.min(maxGapY, gapY));
    }

    @Override
    public void update() {
        super.move();

        if (oscillates) {
            oscillationTick++;
            double wave = Math.sin(oscillationTick / (double) oscillationPeriod) * oscillationAmplitude;
            setGapY(baseGapY + (int) Math.round(wave));
        }

        if (highlightTimer > 0) {
            highlightTimer--;
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint columnPaint = new GradientPaint(
                x, 0,
                bodyColor.brighter(),
                x + width, 0,
                bodyColor.darker());
        g2d.setPaint(columnPaint);

        int topHeight = Math.max(0, gapY);
        if (topHeight > 0) {
            g2d.fillRoundRect(x, 0, width, topHeight, 18, 18);
        }

        int bottomY = gapY + gapHeight;
        int bottomHeight = Math.max(0, PLAYFIELD_HEIGHT - bottomY);
        if (bottomHeight > 0) {
            g2d.fillRoundRect(x, bottomY, width, bottomHeight, 18, 18);
        }

        g2d.setColor(bodyColor.darker());
        if (topHeight > 0) {
            g2d.fillRect(x - 2, gapY - 6, width + 4, 6);
        }
        if (bottomHeight > 0) {
            g2d.fillRect(x - 2, bottomY, width + 4, 6);
        }

        if (highlightTimer > 0) {
            float alpha = Math.min(1f, highlightTimer / 24f);
            g2d.setColor(new Color(255, 255, 255, (int) (130 * alpha)));
            if (topHeight > 0) {
                g2d.fillRoundRect(x, 0, width, topHeight, 18, 18);
            }
            if (bottomHeight > 0) {
                g2d.fillRoundRect(x, bottomY, width, bottomHeight, 18, 18);
            }
        }

        g2d.dispose();
    }

    @Override
    public boolean collidesWith(GameObject other) {
        int otherLeft = other.getX();
        int otherRight = otherLeft + other.getWidth();
        int otherTop = other.getY();
        int otherBottom = otherTop + other.getHeight();

        int horizontalMargin = 6;
        if (otherRight <= x + horizontalMargin || otherLeft >= x + width - horizontalMargin) {
            return false;
        }

        int buffer = Math.max(10, gapHeight / 8);
        int safeTop = Math.max(0, gapY - buffer);
        int safeBottom = Math.min(PLAYFIELD_HEIGHT, gapY + gapHeight + buffer);

        if (otherBottom <= safeTop) {
            return false;
        }
        if (otherTop >= safeBottom) {
            return false;
        }

        int gapTop = gapY;
        int gapBottom = gapY + gapHeight;

        if (otherTop < gapTop && otherBottom > gapTop) {
            return true;
        }
        if (otherBottom > gapBottom && otherTop < gapBottom) {
            return true;
        }

        return false;
    }
}