package nervouschef;

import java.awt.Color;
import java.awt.Graphics;


public abstract class flyingObject {
    // 1. Position and dimensions
    protected double x;
    protected double y;
    protected double gravity = 0.2;
    protected double v_y = -10;
    protected double v_x = -2.5;
    protected int width = 40;
    protected int height = 40;

    // Constructor with a test starting position
    public flyingObject(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }
    
    public void update() {
        x += v_x;
        y += v_y;
        v_y += gravity;
    }

    // Getters needed for logic/GUI
    public double getX() { return x; }
    public double getY() { return y; }

    // 2. Step 1 Drawing Method: Renders a simple red square placeholder
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        // Cast double coordinates to int for pixel rendering
        g.fillRect((int) x, (int) y, width, height);
    }
    
    // Abstract methods required by your project plan
    public abstract void slice();
    public abstract boolean isCompleted();
}