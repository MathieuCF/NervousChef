package nervouschef;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class FlyingObject {

    protected double x;
    protected double y;
    protected double cx;
    protected double cy;
    protected double vx; // Horizontal velocity (moves left/right)
    protected double vy; // Vertical velocity (gravity / up/down)
    protected double radius = 35; // Default size/radius
    protected double rotation; // current rotation angle in radians
    protected double rotationSpeed; // how much a rotation changes each update
    public static final double GRAVITY = 0.4;
    protected double gravity = GRAVITY;

    /**
     *
     * Abstract base class for anything that flies: position, velocity, gravity
     * and rotation, updated every tick.
     */
    public FlyingObject(double startX, double startY, double radius) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.cx = x + radius;
        this.cy = y + radius;
    }

    // Full physics update (Arc trajectory)
    public void update() {
        vy += gravity; // Pulls down over time
        x += vx;       // Moves horizontally
        y += vy;       // Moves vertically
        cx = x + radius;
        cy = y + radius;
        rotation += rotationSpeed;
    }

    // Reset method for re-launching the object
    public void reset(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.vx = 0;
        this.vy = 0;
        this.rotation = 0;
        this.rotationSpeed = 0;

    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public void setXVelocity(double vx) {
        this.vx = vx;
    }

    public void setYVelocity(double vy) {
        this.vy = vy;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getRadius() {
        return radius;
    }

    public double getRotation() {
        return rotation;
    }

    public double getGravity() {
        return gravity;
    }
    
    public double getCx() { return cx; }
    public double getCy() { return cy; }

    // Abstract methods that subclasses (like Fruit) must implement
    public abstract boolean isCompleted();

    public abstract void draw(Graphics2D g2, BufferedImage appleImage);
}
