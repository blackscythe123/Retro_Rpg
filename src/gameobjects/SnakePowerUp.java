package gameobjects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Special collectible for Snake that grants temporary buffs.
 */
public class SnakePowerUp extends GameObject {
    public enum PowerUpType {
        DOUBLE_SCORE,
        SHIELD,
        SLOW_TIME
    }

    private PowerUpType type;
    private int lifetime;
    private boolean expired;
    private int animationTick;

    public SnakePowerUp(int x, int y, PowerUpType type) {
        super(x, y, 20, 20);
        this.type = type;
        this.lifetime = 600; // ~30 seconds at 20 FPS
        this.expired = false;
    }

    public PowerUpType getType() {
        return type;
    }

    public boolean isExpired() {
        return expired;
    }

    public int getLifetime() {
        return lifetime;
    }

    @Override
    public void update() {
        if (lifetime > 0) {
            lifetime--;
        }
        if (lifetime <= 0) {
            expired = true;
        }
        animationTick = (animationTick + 1) % 360;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color glowColor;
        switch (type) {
            case DOUBLE_SCORE:
                glowColor = new Color(255, 215, 0);
                break;
            case SHIELD:
                glowColor = new Color(80, 180, 255);
                break;
            default:
                glowColor = new Color(150, 255, 150);
                break;
        }

        float alpha = 0.5f + 0.4f * (float) Math.sin(animationTick * Math.PI / 45);
        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255)));
        g2.fillOval(x - 4, y - 4, width + 8, height + 8);

        g2.setColor(glowColor);
        g2.fillOval(x, y, width, height);

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(12f).deriveFont(java.awt.Font.BOLD));
        String symbol = type == PowerUpType.DOUBLE_SCORE ? "x2" : type == PowerUpType.SHIELD ? "S" : "SN";
        int stringWidth = g2.getFontMetrics().stringWidth(symbol);
        g2.drawString(symbol, x + (width - stringWidth) / 2, y + (height + g2.getFontMetrics().getAscent()) / 2 - 4);

        g2.dispose();
    }
}
