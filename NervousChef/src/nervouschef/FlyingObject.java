package nervouschef;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class FlyingObject {
    protected double x;
    protected double y;
    protected double vx; // Horizontal velocity (moves left/right)
    protected double vy; // Vertical velocity (gravity / up/down)
    protected double radius = 25; // Default size/radius

    public FlyingObject(double startX, double startY, double radius) {
        this.x = startX;
        this.y = startY;
        this.radius = 35;
    }

    // Full physics update (Arc trajectory)
    public void update() {
        double gravity = 0.4;
        vy += gravity; // Pulls down over time
        x += vx;       // Moves horizontally
        y += vy;       // Moves vertically
    }

    // Reset method for re-launching the test object
    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.vx = -3.5;
        this.vy = -12.0;
    }
    
    public void setXVelocity(double vx) {
        this.vx = vx;
    }
    
    public void setYVelocity(double vy) {
        this.vy = vy;
    }


    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getRadius() { return radius; }

    // Abstract methods that subclasses (like Fruit) must implement
    public abstract boolean isCompleted();
    public abstract void draw(Graphics2D g2, BufferedImage appleImage);
}