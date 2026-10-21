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
import java.util.EnumMap;
import java.util.Random;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.geom.Path2D;
import java.util.Iterator;

public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int TICK_DELAY_MS = 16;
    private static final int MAX_TRAIL_POINTS = 35;
    private static final long GAME_DURATION_MS = 10000; // temp num for testing only 30s

    // used to generate diff fruit types without creating new random object each time
    private final Random random = new Random();

    // constants for launchRandomFruit
    private static final int LAUNCH_Y = PANEL_HEIGHT;
    private static final int LAUNCH_MARGIN = 40;
    private static final double MIN_LAUNCH_SPEED = 14;
    private static final double MAX_LAUNCH_SPEED = 18;
    private static final int FRUIT_SIZE = 70;

    private final Timer timer;
    private List<Point> trailPoints = new ArrayList<>();
    private List<FlyingObject> activeObjects = new ArrayList<>();
    private Point previousPoint;
    //enummap more efficient than hashmaps
    private EnumMap<FruitType, BufferedImage> fruitImages;
    private CountdownTimer gameTimer;
    private long previousTickTime;
    private ActionListener timerExpiredListener;

    private static final float TRAIL_MIN_WIDTH = 1f;
    private static final float TRAIL_MAX_WIDTH = 10f;

    private static final int TRAIL_MIN_ALPHA = 0;
    private static final int TRAIL_MAX_ALPHA = 180;

    private final List<Particle> particles = new ArrayList<>();
    private static final int JUICE_COUNT = 15;

    private static final double JUICE_MIN_SPEED = 2;
    private static final double JUICE_MAX_SPEED = 7;

    private static final int JUICE_MIN_LIFE = 25;
    private static final int JUICE_MAX_LIFE = 45;

    private static final double JUICE_MIN_SIZE = 4;
    private static final double JUICE_MAX_SIZE = 8;


    // Declare the current fruit field
    private Fruit currentFruit;

    public GamePanel() {

        // initialise enummap
        fruitImages = new EnumMap<>(FruitType.class);

        for (FruitType type : FruitType.values()) {
            fruitImages.put(type, ImageLoader.load(type.getImageFile()));
        }
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        setOpaque(false);

        launchRandomFruit();

        // create timer
        gameTimer = new CountdownTimer(GAME_DURATION_MS);

        timer = new Timer(TICK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // calculate real time passed since the previous game tick
                long currentTime = System.currentTimeMillis();

                long elapsedMs = currentTime - previousTickTime;

                previousTickTime = currentTime;

                // update countdown timer
                gameTimer.update(elapsedMs);

                // stops game when timer reaches zero
                if (gameTimer.isFinished()) {
                    stopGameLoop();

                    // notify MainFrame that timer expired
                    if (timerExpiredListener != null) {
                        timerExpiredListener.actionPerformed(
                                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "TIME_UP"));
                    }
                    return;
                }

                // gradually remove old trail points for fading effect
                if (!trailPoints.isEmpty()) {
                    trailPoints.remove(0);
                }

                // Update physics every frame safely using an Iterator
                Iterator<FlyingObject> iterator = activeObjects.iterator();
                boolean spawnNewFruit = false;

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
                                    iterator.remove();
                                    spawnNewFruit = true;
                                }
                            }
                        }
                    }
                }

                Iterator<Particle> particleIterator = particles.iterator();
                while (particleIterator.hasNext()) {

                    Particle particle
                            = particleIterator.next();

                    particle.update();

                    if (particle.isDead()) {
                        particleIterator.remove();
                    }
                }

                // launch replacement fruit after iteration finishes(when done in while loop caused a lag
                if (spawnNewFruit) {
                    launchRandomFruit();
                }

                // If currentFruit was sliced, check if its sub-fruits are all gone. 
                // If they are gone, bring the test fruit back!
                if (currentFruit.isCompleted()) {
                    boolean subFruitStillActive = false;
                    for (FlyingObject obj : activeObjects) {
                        if (obj instanceof Fruit && ((Fruit) obj).isSubFruit()) {
                            subFruitStillActive = true;
                            break;
                        }
                    }

                    // If no sub-fruits are left on screen, respawn the whole fruit
                    if (!subFruitStillActive) {

                        launchRandomFruit();
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
                if (currentFruit != null && !currentFruit.isCompleted()) {
                    if (currentFruit.lineIntersectsFruit(previousPoint, currentPoint)) {

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
                            currentFruit.slice(
                                    cutDir,
                                    previousPoint,
                                    0.5
                            );

                            spawnJuice(currentPoint, currentFruit.getType().getJuiceColor());
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

        // 1. draw the trail
        drawTrail(g2);

        // 2. Draw the test fruit / cube
        for (FlyingObject obj : activeObjects) {
            if (obj != null) {
                BufferedImage image = fruitImages.get(((Fruit) obj).getType());
                obj.draw(g2, image);
            }
        }

        // draw particles/splatter
        drawParticles(g2);
    }

    private void drawTrail(Graphics2D g2) {

        // Need at least 2 points to form a trail
        if (trailPoints.size() < 2) {
            return;
        }

        // Stores the left and right edges of the blade
        List<Point> leftPoints = new ArrayList<>();
        List<Point> rightPoints = new ArrayList<>();

        // Reuse the previous normal if a direction collapses to (0,0)
        Vector2D lastValidNormal = new Vector2D(0, 1);

        for (int i = 0; i < trailPoints.size(); i++) {

            Point point = trailPoints.get(i);

            Vector2D direction;

            // First point uses p1 - p0
            if (i == 0) {

                direction = Vector2D.between(
                        trailPoints.get(0),
                        trailPoints.get(1)
                ).normalize();

            } // Last point uses p[n-1] - p[n-2]
            else if (i == trailPoints.size() - 1) {

                direction = Vector2D.between(
                        trailPoints.get(i - 1),
                        trailPoints.get(i)
                ).normalize();

            } // Middle points use p[i+1] - p[i-1]
            else {

                direction = Vector2D.between(
                        trailPoints.get(i - 1),
                        trailPoints.get(i + 1)
                ).normalize();
            }

            // Calculate perpendicular vector
            Vector2D normal
                    = new Vector2D(
                            -direction.y,
                            direction.x
                    );

            // Reuse previous normal if direction is zero length
            if (normal.x == 0 && normal.y == 0) {
                normal = lastValidNormal;
            } else {
                lastValidNormal = normal;
            }

            // Position along trail (0 = tail, 1 = head)
            double progress
                    = (double) i
                    / (trailPoints.size() - 1);

            // Width increases towards the head
            double width
                    = TRAIL_MIN_WIDTH
                    + progress
                    * (TRAIL_MAX_WIDTH - TRAIL_MIN_WIDTH);

            double halfWidth = width / 2.0;

            // Left edge point
            int leftX = (int) (point.x
                    + normal.x * halfWidth);

            int leftY = (int) (point.y
                    + normal.y * halfWidth);

            // Right edge point
            int rightX = (int) (point.x
                    - normal.x * halfWidth);

            int rightY = (int) (point.y
                    - normal.y * halfWidth);

            leftPoints.add(
                    new Point(leftX, leftY)
            );

            rightPoints.add(
                    new Point(rightX, rightY)
            );
        }

        // create blade shape
        Path2D.Double blade = new Path2D.Double();

        // start at first left point
        blade.moveTo(leftPoints.get(0).x, leftPoints.get(0).y);

        // follow left edge
        for (Point p : leftPoints) {
            blade.lineTo(p.x, p.y);

        }

        //follow right edge backwards 
        for (int i = rightPoints.size() - 1; i >= 0; i--) {
            Point p = rightPoints.get(i);

            blade.lineTo(p.x, p.y);
        }

        blade.closePath();
        Paint oldPaint = g2.getPaint();

        Point tail = trailPoints.get(0);
        Point head = trailPoints.get(trailPoints.size() - 1);

        GradientPaint gradient = new GradientPaint(tail.x, tail.y,
                new Color(255, 255, 255, TRAIL_MIN_ALPHA), head.x, head.y,
                new Color(255, 255, 255, TRAIL_MAX_ALPHA));

        g2.setPaint(gradient);
        g2.fill(blade);

        g2.setPaint(oldPaint);

    }

    public void spawnObject(FlyingObject obj) {
        activeObjects.add(obj);
    }

    public void unspawnObject(FlyingObject obj) {
        activeObjects.remove(obj);
    }

    public void startGameLoop() {
        // prevents timer from jumping after pause/resume
        previousTickTime = System.currentTimeMillis();
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

        gameTimer.reset();

        activeObjects.clear();

        particles.clear();

        launchRandomFruit();

        repaint();
    }

    public int getSecondsRemaining() {
        return gameTimer.getSecondsRemaining();
    }

    // register listener for when timer expires
    public void setTimerExpiredListener(ActionListener listener) {
        this.timerExpiredListener = listener;
    }

    // returns a random fruit type
    private FruitType getRandomFruitType() {
        FruitType[] fruitTypes = FruitType.values();

        // randomly pick an entry from the enum array
        return fruitTypes[random.nextInt(fruitTypes.length)];
    }

    // helper method for launchRandomFruit
    private double randomBetween(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    // creates and launches a random fruit
    private void launchRandomFruit() {
        FruitType randomType = getRandomFruitType();

        // random start position of fruit
        double startX = randomBetween(LAUNCH_MARGIN, PANEL_WIDTH - FRUIT_SIZE - LAUNCH_MARGIN);
        double startY = LAUNCH_Y;

        currentFruit = new Fruit(startX, startY, 35, randomType, this);

        // random vertical launch speed
        double vy = -randomBetween(MIN_LAUNCH_SPEED, MAX_LAUNCH_SPEED);

        // flight time
        double flightTicks = 2 * Math.abs(vy) / FlyingObject.GRAVITY;

        // pick where fruit land to guarantee it stays on screen 
        double targetX = randomBetween(LAUNCH_MARGIN, PANEL_WIDTH - FRUIT_SIZE - LAUNCH_MARGIN);

        // horizontal speed
        double vx = (targetX - startX) / flightTicks;

        // random clockwise or anticlockwise spinning
        double spinSpeed = random.nextBoolean() ? 0.05 : -0.05;

        currentFruit.subFruit = false;
        
        currentFruit.setXVelocity(vx);
        currentFruit.setYVelocity(vy);
        currentFruit.setRotationSpeed(spinSpeed);
        activeObjects.add(currentFruit);
    }

    private void spawnJuice(Point position, Color color) {
        for (int i = 0; i < JUICE_COUNT; i++) {
            double angle = random.nextDouble() * Math.PI * 2;

            double speed = randomBetween(JUICE_MIN_SPEED, JUICE_MAX_SPEED);

            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            int life = JUICE_MIN_LIFE + random.nextInt(JUICE_MAX_LIFE - JUICE_MIN_LIFE + 1);

            double size = randomBetween(JUICE_MIN_SIZE, JUICE_MAX_SIZE);

            particles.add(new Particle(position.x, position.y, vx, vy, life, size, color));

        }
    }

    // draw juice splash particles over fruit
    private void drawParticles(Graphics2D g2) {
        for (Particle particle : particles) {
            particle.draw(g2);
        }
    }
}
