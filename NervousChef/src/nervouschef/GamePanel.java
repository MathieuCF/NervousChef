package nervouschef;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int TICK_DELAY_MS = 16;
    private static final int MAX_TRAIL_POINTS = 20;

    private final Timer timer;
    private List<Point> trailPoints = new ArrayList<>();
    private List<FlyingObject> activeObjects = new ArrayList<>();
    private Point previousPoint;
    private BufferedImage appleImage;

    // 1. Declare the test fruit field here at the top
    private Fruit testFruit = new Fruit(600, 450, 25, this);

    public GamePanel() {
        try {
            appleImage = ImageIO.read(
                    getClass().getResource("apple 3d.png")
            );
        } catch (IOException e) {
            e.printStackTrace();

        }
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);

        // 2. Add the test fruit to active objects inside the constructor
        testFruit.subFruit = false;
        testFruit.setXVelocity(-3.5);
        testFruit.setYVelocity(-12);
        activeObjects.add(testFruit);

        timer = new Timer(TICK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Gradually remove old trail points for fading effect
                if (!trailPoints.isEmpty()) {
                    trailPoints.remove(0);
                }

                // Update physics every frame safely using an Iterator
                java.util.Iterator<FlyingObject> iterator = activeObjects.iterator();
                while (iterator.hasNext()) {
                    FlyingObject obj = iterator.next();

                    if (obj != null) {
                        obj.update();

                        // Check if object falls off the bottom of the screen
                        if (obj.getY() >= PANEL_HEIGHT + 50) {
                            if (obj instanceof Fruit) {
                                Fruit fruit = (Fruit) obj;

                                if (fruit.isSubFruit()) {
                                    // Sub-fruit fell off screen -> safely remove it using the iterator
                                    iterator.remove();
                                } else {
                                    // Whole fruit fell off screen without being cut -> reset it
                                    fruit.reset(600, 450);
                                }
                            }
                        }
                    }
                }

                // If testFruit was sliced, check if its sub-fruits are all gone. 
                // If they are gone, bring the test fruit back!
                if (testFruit.isCompleted()) {
                    boolean subFruitStillActive = false;
                    for (FlyingObject obj : activeObjects) {
                        if (obj instanceof Fruit && ((Fruit) obj).isSubFruit()) {
                            subFruitStillActive = true;
                            break;
                        }
                    }

                    // If no sub-fruits are left on screen, respawn the whole test fruit
                    if (!subFruitStillActive) {
                        testFruit.reset(600, 450);
                        testFruit.subFruit = false; // Make sure it's a whole fruit again
                        activeObjects.add(testFruit);
                    }
                }

                repaint();
            }
        });

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                trailPoints.clear();
                previousPoint = e.getPoint();
                trailPoints.add(previousPoint);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getPoint();

                // Add point to visual trail
                trailPoints.add(currentPoint);
                if (trailPoints.size() > MAX_TRAIL_POINTS) {
                    trailPoints.remove(0);
                }

                // Slice intersection check between previousPoint and currentPoint
                if (testFruit != null && !testFruit.isCompleted()) {
                    if (testFruit.lineIntersectsFruit(previousPoint, currentPoint)) {
                        
                        // use an older trail point for smoother swipe direction
                        Point cutStart;

                        if (trailPoints.size() >= 5) {
                            cutStart = trailPoints.get(trailPoints.size() - 5);
                        } else {
                            cutStart = previousPoint;
                        }

                        // calculate swipe direction
                        Vector2D cutDir
                                = Vector2D.between(cutStart, currentPoint);

                        // calculate swipe length
                        double length = Math.sqrt(
                                cutDir.x * cutDir.x
                                + cutDir.y * cutDir.y);

                        // ignore short swipes
                        if (length >= 10) {

                            // slice using swipe direction and cut poistion
                            testFruit.slice(
                                    cutDir,
                                    previousPoint,
                                    0.5
                            );
                        }
                    }
                }

                // Update previousPoint for the next drag event
                previousPoint = currentPoint;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                trailPoints.clear();
                previousPoint = null;
                repaint();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enables smooth anti-aliased rendering
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. Draw the slicing trail
        g2.setColor(Color.WHITE);
        for (int i = 0; i < trailPoints.size() - 1; i++) {
            Point p1 = trailPoints.get(i);
            Point p2 = trailPoints.get(i + 1);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // 2. Draw the test fruit / cube
        for (FlyingObject obj : activeObjects) {
            if (obj != null) {
                obj.draw(g2, appleImage);
            }
        }
    }

    public void spawnObject(FlyingObject obj) {
        activeObjects.add(obj);
    }

    public void unspawnObject(FlyingObject obj) {
        activeObjects.remove(obj);
    }

    public void startGameLoop() {
        timer.start();
    }

    public void stopGameLoop() {
        timer.stop();
    }

    public void reset() {
        if (trailPoints != null) {
            trailPoints.clear();
        }
        previousPoint = null;
        repaint();
    }
}
