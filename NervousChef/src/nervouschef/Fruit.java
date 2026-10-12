package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

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
    public void reset(double startX, double startY) {
        super.reset(startX, startY);  // reset physics
        this.isSliced = false;  // reset colour
    }
    
    public boolean lineIntersectsFruit(Point A, Point B) {  // Detects if two points of the slice crossed this fruit
            /**
             * For this, we can use our specially built Vector2D 
             * helper function that will do a lot of the math for us
             */
            Point fruitCenter = new Point((int) (x + radius), (int) (y + radius));
            
            return (Vector2D.shortestDistanceToSegment(A, B, fruitCenter) <= radius);  // Mouse passed through fruit hitbox
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