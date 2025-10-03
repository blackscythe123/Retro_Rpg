package gameobjects;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;

/**
 * Snake game object - represents the snake in Snake game
 */
public class Snake extends MovableObject {
    private LinkedList<int[]> body; // List of [x,y] positions for body segments
    private int direction; // 0: up, 1: right, 2: down, 3: left
    private boolean growing;
    private int speed;
    private int animationTick;

    public Snake(int x, int y) {
        super(x, y, 20, 20, 0, 0);
        body = new LinkedList<>();
        body.add(new int[]{x, y});
        direction = 1; // Start moving right
        growing = false;
        speed = 10;
        updateVelocity();
    }

    public void setDirection(int direction) {
        this.direction = direction;
        updateVelocity();
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = Math.max(4, speed);
        updateVelocity();
    }

    private void updateVelocity() {
        switch (direction) {
            case 0: velocityX = 0; velocityY = -speed; break; // Up
            case 1: velocityX = speed; velocityY = 0; break;  // Right
            case 2: velocityX = 0; velocityY = speed; break;  // Down
            case 3: velocityX = -speed; velocityY = 0; break; // Left
        }
    }

    public void grow() {
        growing = true;
    }

    public LinkedList<int[]> getBody() {
        return body;
    }

    public int getDirection() {
        return direction;
    }

    @Override
    public void update() {
        animationTick = (animationTick + 1) % 360;
        // Move head
        int[] head = body.getFirst();
        int newX = head[0] + velocityX;
        int newY = head[1] + velocityY;

        // Add new head position
        body.addFirst(new int[]{newX, newY});

        // Remove tail unless growing
        if (!growing) {
            body.removeLast();
        } else {
            growing = false;
        }

        // Update position to head
        x = newX;
        y = newY;
    }

    @Override
    public void draw(Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        int index = 0;
        for (int[] segment : body) {
            float hue = 0.28f - (index * 0.003f);
            float brightness = 0.6f + (float) Math.sin((animationTick + index * 10) * Math.PI / 180) * 0.1f;
            g2.setColor(Color.getHSBColor(hue, 0.9f, Math.min(1f, brightness)));
            g2.fillRoundRect(segment[0], segment[1], width, height, 6, 6);
            index++;
        }

        // Draw head with accent & eyes
        int[] head = body.getFirst();
        g2.setColor(Color.YELLOW);
        g2.fillRoundRect(head[0], head[1], width, height, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillOval(head[0] + 4, head[1] + 4, 4, 4);
        g2.fillOval(head[0] + width - 8, head[1] + 4, 4, 4);
        g2.dispose();
    }

    @Override
    public boolean collidesWith(GameObject other) {
        // Check if head collides with other object
        int[] head = body.getFirst();
        return head[0] < other.x + other.width &&
               head[0] + width > other.x &&
               head[1] < other.y + other.height &&
               head[1] + height > other.y;
    }
}