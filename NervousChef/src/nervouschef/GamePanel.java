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
import java.util.EnumMap;
import java.util.Random;
import javax.imageio.ImageIO;

public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int TICK_DELAY_MS = 16;
    private static final int MAX_TRAIL_POINTS = 20;
    private static final long GAME_DURATION_MS = 30000; // temp num for testing only 30s
    // used to generate diff fruit types without creating new random object each time
    private final Random random = new Random(); 

    // TEST ONLY: replace with throw mechanic later
    private static final int FRUIT_START_X = 600;
    private static final int FRUIT_START_Y = 450;

    private final Timer timer;
    private List<Point> trailPoints = new ArrayList<>();
    private List<FlyingObject> activeObjects = new ArrayList<>();
    private Point previousPoint;
    //enummap more efficient than hashmaps
    private EnumMap<FruitType, BufferedImage> fruitImages;
    private CountdownTimer gameTimer;
    private long previousTickTime;
    private ActionListener timerExpiredListener;

    // Declare the current fruit field
    private Fruit currentFruit;

    public GamePanel() {
        // initialise enummap
        fruitImages = new EnumMap<>(FruitType.class);

        for (FruitType type : FruitType.values()) {
            fruitImages.put(type, loadImage(type.getImageFile()));
        }
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);

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
                java.util.Iterator<FlyingObject> iterator = activeObjects.iterator();
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
                
                // launch replacement fruit after iteration finishes(when done in while loop caused a lag
                if(spawnNewFruit) {
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
                BufferedImage image = fruitImages.get(((Fruit) obj).getType());
                obj.draw(g2, image);
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

    // loads an image and returns null if not found
    private BufferedImage loadImage(String fileName) {
        try {
            java.net.URL url = getClass().getResource(fileName);

            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException e) {
            System.out.println("Failed to load " + fileName);
        }
        return null;
    }

    // returns a random fruit type
    private FruitType getRandomFruitType() {
        FruitType[] fruitTypes = FruitType.values();

        // randomly pick an entry from the enum array
        return fruitTypes[random.nextInt(fruitTypes.length)];
    }

    // creates and launches a random fruit
    private void launchRandomFruit() {
        FruitType randomType = getRandomFruitType();

        currentFruit = new Fruit(FRUIT_START_X, FRUIT_START_Y, 
                25, randomType, this);
        
        currentFruit.subFruit = false;
        
        // for test only purpose (should relace with throw mechanic)
        currentFruit.setXVelocity(-3.5);
        currentFruit.setYVelocity(-12);
        
        activeObjects.add(currentFruit);
    }

}
