package gameobjects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Environmental hazard for Snake game that the player must avoid.
 */
public class SnakeObstacle extends GameObject {
    private int animationTick;

    public SnakeObstacle(int x, int y) {
        super(x, y, 20, 20);
    }

    @Override
    public void update() {
        animationTick = (animationTick + 1) % 360;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float pulse = 0.4f + 0.2f * (float) Math.sin(animationTick * Math.PI / 90);
        Color rockColor = new Color(80, 80 + (int) (pulse * 60), 90);
        g2.setColor(rockColor);
        g2.fillRoundRect(x, y, width, height, 8, 8);

        g2.setColor(new Color(40, 40, 50));
        g2.drawRoundRect(x, y, width, height, 8, 8);
        g2.dispose();
    }
}