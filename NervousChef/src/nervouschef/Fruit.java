package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;

public class Fruit extends FlyingObject {

    private boolean isSliced = false;

    public Fruit(double startX, double startY) {
        super(startX, startY);
        
    }

    @Override
    public boolean isCompleted() {
        return isSliced; // Placeholder / state tracker
    }
    
    @Override
    public void slice() {
        this.isSliced = true; // Flips the state when sliced!
        System.out.println("Fruit sliced!");
    }

    @Override
    public void draw(Graphics2D g2) {
        // Change color based on whether it has been sliced
        if (isSliced) {
            g2.setColor(Color.GREEN); // Turns green when sliced!
        } else {
            g2.setColor(Color.RED);   // Red when falling normally
        }
        
        // Draw the square representing the fruit using its x, y, and radius (diameter = radius * 2)
        int size = (int) (getRadius() * 2);
        g2.fillRect((int) getX(), (int) getY(), size, size);
    }
}