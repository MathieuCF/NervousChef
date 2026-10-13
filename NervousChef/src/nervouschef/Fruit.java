package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

public class Fruit extends FlyingObject {
    private GamePanel gamePanel; // Reference to the main game
    private boolean isSliced = false;

    public Fruit(double startX, double startY, double radius, GamePanel gamePanel) {
        super(startX, startY, radius);
        this.gamePanel = gamePanel;
        
    }

    @Override
    public boolean isCompleted() {
        return isSliced; // Placeholder / state tracker
    }
        
    public void createHalf(Fruit wholeFruit, Vector2D cutDir, double sizeMultipler) {
        // Creating a normal vector to cut 90° counter-clockwise: (-y, x)
        Vector2D normalToCut = new Vector2D(-cutDir.y, cutDir.x);
        
        Vector2D normalNormalized = normalToCut.normalize();
        
        // Where are the centers of both halves, compared to the initial whole fruit center?
        int offsetX1 = (int) (normalNormalized.x * (wholeFruit.getRadius() / 2.0));
        int offsetY1 = (int) (normalNormalized.y * (wholeFruit.getRadius() / 2.0));

        int offsetX2 = (int) (normalNormalized.x * (-wholeFruit.getRadius() / 2.0));
        int offsetY2 = (int) (normalNormalized.y * (-wholeFruit.getRadius() / 2.0));

        double absX1 = wholeFruit.x + wholeFruit.radius + offsetX1;
        double absY1 = wholeFruit.y + wholeFruit.radius + offsetY1;
        
        double absX2 = wholeFruit.x + wholeFruit.radius + offsetX2;
        double absY2 = wholeFruit.y + wholeFruit.radius + offsetY2;
        
        
        // Creating fruit halves
        Fruit half1 = new Fruit(absX1, absY1, wholeFruit.radius * sizeMultipler, gamePanel);
        Fruit half2 = new Fruit(absX2, absY2, wholeFruit.radius * sizeMultipler, gamePanel);
        
        gamePanel.spawnObject(half1);
        gamePanel.spawnObject(half2);
    }
    
            
    @Override
    public void slice() {
        this.isSliced = true; // Flips the state when sliced!
        
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