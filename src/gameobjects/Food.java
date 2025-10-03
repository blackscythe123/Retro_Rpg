package gameobjects;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Food object for Snake game
 */
public class Food extends ImmovableObject {
    private int animationTick;

    public Food(int x, int y) {
        super(x, y, 20, 20);
        animationTick = 0;
    }

    @Override
    public void update() {
        animationTick = (animationTick + 1) % 360;
    }

    @Override
    public void draw(Graphics g) {
        float pulse = 0.5f + 0.4f * (float) Math.sin(animationTick * Math.PI / 30);
        int radius = (int) (width * pulse / 2);
        g.setColor(new Color(255, 120, 120, 180));
        g.fillOval(x + width / 2 - radius, y + height / 2 - radius, radius * 2, radius * 2);

        g.setColor(Color.RED);
        g.fillOval(x, y, width, height);
    }
}