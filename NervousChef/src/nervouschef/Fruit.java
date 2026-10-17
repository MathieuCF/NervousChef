package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Shape;
import java.awt.Polygon;

public class Fruit extends FlyingObject {

    private FruitType type;
    private GamePanel gamePanel; // Reference to the main game
    private boolean isSliced = false;
    public boolean subFruit = true;
    private double minXSepVel = 2.0;
    private double maxXSepVel = 12.0;
    private double minYSepVel = 2.0;
    private double maxYSepVel = 12.0;
    private Vector2D cutNormal;
    private boolean firstHalf;
    private double cutOffsetX;
    private double cutOffsetY;

    public Fruit(double startX, double startY, double radius, FruitType type, GamePanel gamePanel) {
        super(startX, startY, radius);
        this.gamePanel = gamePanel;
        this.type = type;

    }

    // return this fruit's type
    public FruitType getType() {
        return type;
    }

    public void setCutData(
            Vector2D cutNormal, boolean firstHalf,
            double cutOffsetX, double cutOffsetY) {
        this.cutNormal = cutNormal;
        this.firstHalf = firstHalf;
        this.cutOffsetX = cutOffsetX;
        this.cutOffsetY = cutOffsetY;

    }

    @Override
    public boolean isCompleted() {
        return isSliced; // Placeholder / state tracker
    }

    public boolean isSubFruit() {
        return subFruit;
    }

    public void createHalves(Fruit wholeFruit, Vector2D cutVector, Point cutPoint, double sizeMultiplier) {
        // Creating a normal vector to cut 90° counter-clockwise: (-y, x)
        Vector2D normalToCut = new Vector2D(-cutVector.y, cutVector.x);

        Vector2D normalNormalized = normalToCut.normalize();

        // Creating fruit halves
        Fruit half1 = new Fruit(wholeFruit.x, wholeFruit.y, wholeFruit.radius, wholeFruit.getType(), gamePanel);
        Fruit half2 = new Fruit(wholeFruit.x, wholeFruit.y, wholeFruit.radius, wholeFruit.getType(), gamePanel);

        // copy the current angle so the fruit does not jump when sliced
        half1.setRotation(wholeFruit.getRotation());
        half2.setRotation(wholeFruit.getRotation());

        // Giving them separation velocities
        // 1. Determine how fast the cut was (magnitude of the normal vector)
        double cutSpeedX = Math.clamp(Math.abs(normalToCut.x), minXSepVel, maxXSepVel);
        double cutSpeedY = Math.clamp(Math.abs(normalToCut.y), minYSepVel, maxYSepVel);

        // 2. Apply the speed in the correct positive/negative directions
        // half1 moves along the natural normal vector directions
        half1.setXVelocity(wholeFruit.getVx() + (Math.signum(normalToCut.x) * cutSpeedX));
        half1.setYVelocity(wholeFruit.getVy() + (Math.signum(normalToCut.y) * cutSpeedY));

        // half2 moves in the exact opposite directions
        half2.setXVelocity(wholeFruit.getVx() - (Math.signum(normalToCut.x) * cutSpeedX));
        half2.setYVelocity(wholeFruit.getVy() - (Math.signum(normalToCut.y) * cutSpeedY));

        // Convert cut information from screen coordinates into the fruit's local coordinate system
        //so the cut stays aligned with the swipe after rotation.
        double angle = -wholeFruit.getRotation();

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        // rotate cut normal into fruit space using standard 2d rotation formula x' = xcos - ysin
        Vector2D localNormal
                = new Vector2D(
                        normalNormalized.x * cos
                        - normalNormalized.y * sin,
                        normalNormalized.x * sin
                        + normalNormalized.y * cos
                );

        // fruit centre (rotation point)
        double centreX = wholeFruit.getX() + wholeFruit.getRadius();
        double centreY = wholeFruit.getY() + wholeFruit.getRadius();

        // cut point relative to centre
        double relativeX
                = cutPoint.x - centreX;

        double relativeY
                = cutPoint.y - centreY;

        //rotate cut point into fruit space
        double rotatedX
                = relativeX * cos
                - relativeY * sin;

        double rotatedY
                = relativeX * sin
                + relativeY * cos;

        // Convert back to top-left based coordinates
        double localCutOffsetX = rotatedX + wholeFruit.getRadius();
        double localCutOffsetY = rotatedY +  wholeFruit.getRadius();

        double spinSpeed = 0.15;
        half1.setRotationSpeed(spinSpeed);
        half2.setRotationSpeed(-spinSpeed);

        half1.setCutData(localNormal, true, localCutOffsetX, localCutOffsetY);
        half2.setCutData(localNormal, false, localCutOffsetX, localCutOffsetY);

        gamePanel.unspawnObject(wholeFruit);
        gamePanel.spawnObject(half1);
        gamePanel.spawnObject(half2);
    }

    public void slice(Vector2D cutDir, Point cutPoint, double sizeMultiplier) {
        this.isSliced = true; // Flips the state when sliced!
        createHalves(this, cutDir, cutPoint, sizeMultiplier);

    }

    @Override
    public void reset(double startX, double startY) {
        super.reset(startX, startY);  // reset physics
        this.isSliced = false;  // reset colour
    }

    public boolean lineIntersectsFruit(Point A, Point B) {  // Detects if two points of the slice crossed this fruit
        /**
         * For this, we can use our specially built Vector2D helper function
         * that will do a lot of the math for us
         */
        Point fruitCenter = new Point((int) (x + radius), (int) (y + radius));

        return (Vector2D.shortestDistanceToSegment(A, B, fruitCenter) <= radius);  // Mouse passed through fruit hitbox
    }

    private void drawImage(Graphics2D g2, BufferedImage fruitImage) {
        // Draw the square representing the fruit using its x, y, and radius (diameter = radius * 2)
        int size = (int) (getRadius() * 2);

        //prevent crash if image file not found
        if (fruitImage == null) {

            g2.setColor(Color.RED);

            g2.fillRect(
                    (int) x,
                    (int) y,
                    size,
                    size
            );

            return;
        }

        // check the fruit is normal/whole 
        if (!subFruit) {
            g2.drawImage(
                    fruitImage,
                    (int) x,
                    (int) y,
                    size,
                    size,
                    null
            );

            return;
        }

        //store clipping region
        Shape oldClip = g2.getClip();

        // calculates where the line currently is 
        double centreX = x + cutOffsetX;
        double centreY = y + cutOffsetY;

        // guarantees line  covers  entire fruit so the clipping doesnt break
        double lineLength = 1000;
        double normalLength = 1000;

        // converts the normal vector back into the cut direction.
        Vector2D direction
                = new Vector2D(cutNormal.y, -cutNormal.x);

        // points form cut line so clipping boundry exists
        double x1 = centreX + direction.x * lineLength;
        double y1 = centreY + direction.y * lineLength;

        double x2 = centreX - direction.x * lineLength;
        double y2 = centreY - direction.y * lineLength;

        // chose which side we are drawing
        double sign = firstHalf ? 1 : -1;

        double x3 = x2 + cutNormal.x * sign * normalLength;
        double y3 = y2 + cutNormal.y * sign * normalLength;

        double x4 = x1 + cutNormal.x * sign * normalLength;
        double y4 = y1 + cutNormal.y * sign * normalLength;

        //creates a shape
        Polygon clip = new Polygon();

        //adds 4 corners
        clip.addPoint((int) x1, (int) y1);
        clip.addPoint((int) x2, (int) y2);
        clip.addPoint((int) x3, (int) y3);
        clip.addPoint((int) x4, (int) y4);

        // activates the clipping region
        g2.setClip(clip);

        g2.drawImage(
                fruitImage,
                (int) x,
                (int) y,
                size,
                size,
                null
        );

        // restores og clipping region 
        g2.setClip(oldClip);

    }

    @Override
    public void draw(Graphics2D g2, BufferedImage fruitImage) {

        java.awt.geom.AffineTransform oldTransform = g2.getTransform();

        g2.rotate(getRotation(), x + radius, y + radius);

        drawImage(g2, fruitImage);

        // restore transform so the rotation doesnt leak
        g2.setTransform(oldTransform);

    }
}
