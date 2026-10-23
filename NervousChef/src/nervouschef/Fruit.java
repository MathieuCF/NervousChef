package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Shape;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Fruit extends FlyingObject {

    private FruitType type;
    private GamePanel gamePanel; // Reference to the main game
    private boolean isSliced = false;
    public boolean subFruit = true;
    private double minXSepVel = 2.0;
    private double maxXSepVel = 5.0;
    private double minYSepVel = 2.0;
    private double maxYSepVel = 5.0;
    private Vector2D cutNormal;
    private boolean firstHalf;
    private double cutOffsetX;
    private double cutOffsetY;
    private int hitsTotal;
    private int hitsRemaining;
    private boolean touched;      // is swipe currently inside this fruit
    private int hitFlashFrames;
    private double accuracySum;
    private double accuracyCount;  // These are used to calculate points and praise words
    private static final int GLOW_DIAMETER = 120;  // size the cached glow images are rendered at
    private static final java.util.Map<Integer, BufferedImage> GLOW_CACHE = new java.util.HashMap<>();  // to fix problems with framerate 

    public Fruit(double startX, double startY, double radius, FruitType type, GamePanel gamePanel) {
        super(startX, startY, radius);
        this.gamePanel = gamePanel;
        this.type = type;
        this.hitsTotal = type.getSlicesRequired();
        this.hitsRemaining = hitsTotal;

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
    
    public void recordAccuracy(double accuracy) {
        accuracySum += accuracy;
        accuracyCount++;
    }
    
    public double getAverageAccuracy() {
        return accuracyCount == 0 ? 0 : accuracySum / accuracyCount;
    }
    
    // returns true if this hit was the final one
    public boolean takeHit() {
        hitsRemaining--;
        if (hitsRemaining <= 0) return true;
        hitFlashFrames = 8;
        vy = Math.max(Math.min(vy, 0) - 3, -9); // small cut velocity bump
        return false;
    }
    
    private void drawGlow(Graphics2D g2) {
        if (subFruit) return; // 1 hit fruits dont have glow?
        double glowSpeed = 120.0;  // higher means slower pulse, lower is faster pulse
        double pulse = 0.9 + 0.1 * Math.sin(System.currentTimeMillis() / glowSpeed);  // pulsating glow value between 0.8-1.0
        float glowR = (float) (radius * 1.7 * pulse);  // shrinking and growing glow
        
        Color c;
        if (hitsRemaining >= 3) {
            c = new Color(255, 70, 70);  // 3 hits? -> red
        } else if (hitsRemaining == 2) {
            c = new Color(255, 210, 60);  // 2 hits? -> yellow
        } else {
            c = new Color(102, 255, 0);  // 1 hit? -> green
        }
        //Color c = hitsRemaining >= 3 ? new Color(255, 70, 70) : new Color(255, 210, 60);
        if (hitFlashFrames > 0) c = Color.WHITE;
        
        int d = (int) (glowR * 2);
        g2.drawImage(getGlowImage(c), (int) (cx - glowR), (int) (cy - glowR), d, d, null);
    }
    
    public double getCutAccuracy(Point cutStart, Point cutEnd) {
        double dx = cutEnd.x - cutStart.x;
        double dy = cutEnd.y - cutStart.y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return 0;

        // distance from centre to the infinite line through the swipe
        double dist = Math.abs(dx * (cy - cutStart.y) - dy * (cx - cutStart.x)) / len;

        double offset = Math.min(dist / radius, 1.0);   // 0 = centre, 1 = edge
        return 1.0 - offset;                            // 1 = perfect, 0 = grazing
    }
    
    public static double centerMultiplier(double accuracy) {
        return 1.0 + accuracy;   // 1x at the edge, 2x dead centre
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
        this.accuracyCount = 0;
        this.accuracySum = 0;
    }
    
    // renders the radial glow for a colour once, then reuses it every frame
    private static BufferedImage getGlowImage(Color c) {  //straight from stack overflow -> why is java2D so slow
        return GLOW_CACHE.computeIfAbsent(c.getRGB(), key -> {
            BufferedImage img = new BufferedImage(GLOW_DIAMETER, GLOW_DIAMETER, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            float r = GLOW_DIAMETER / 2f;
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(r, r), r,
                    new float[]{0f, 0.6f, 1f},
                    new Color[]{withAlpha(c, 220), withAlpha(c, 110), withAlpha(c, 0)}));
            g.fillOval(0, 0, GLOW_DIAMETER, GLOW_DIAMETER);
            g.dispose();
            return img;
        });
    }

    public boolean lineIntersectsFruit(Point A, Point B) {  // Detects if two points of the slice crossed this fruit
        /**
         * For this, we can use our specially built Vector2D helper function
         * that will do a lot of the math for us
         */
        Point fruitCenter = new Point((int) (x + radius), (int) (y + radius));

        return (Vector2D.shortestDistanceToSegment(A, B, fruitCenter) <= radius);  // Mouse passed through fruit hitbox
    }
    
    // basic getters instead of using object field
    public boolean isTouched() { return touched; }
    public void setTouched(boolean touched) { this.touched = touched; }
    public int getHitsTotal() { return hitsTotal; }
    public int getHitsRemaining() { return hitsRemaining; }

    
    @Override
    public void update() {
        super.update();
        if (hitFlashFrames > 0) hitFlashFrames--;

        // bounce off side walls while still a whole multi-hit fruit
        if (!subFruit && hitsTotal > 1) {
            double w = gamePanel.getWidth();
            if (x < 0) { x = 0; vx = Math.abs(vx); }  // easy revert directions math
            else if (w > 0 && x + radius * 2 > w) { x = w - radius * 2; vx = -Math.abs(vx); }
        }
    }
    
    private static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);  // determine color based on a
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
        drawGlow(g2);

        AffineTransform oldTransform = g2.getTransform();
        g2.rotate(getRotation(), x + radius, y + radius);
        drawImage(g2, fruitImage);
        g2.setTransform(oldTransform);
    }
}