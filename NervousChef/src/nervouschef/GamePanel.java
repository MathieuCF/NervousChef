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
import java.awt.Paint;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Path2D;
import java.util.Iterator;

public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int TICK_DELAY_MS = 16;
    private static final int MAX_TRAIL_POINTS = 35;
    private static final long GAME_DURATION_MS = 90000; // temp num for testing only 30s

    // used to generate diff fruit types without creating new random object each time
    private final Random random = new Random();

    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private static final Font POINTS_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font WORD_FONT = new Font("Segoe UI", Font.BOLD, 32);

    // fun part, making fruit inspired words ;)
    private static final String[] OK_WORDS = {"Sweet", "Fruity", "Yummy", "Tasty"};
    private static final String[] GOOD_WORDS = {"Juicy", "Delicious", "Splendid", "Peachy"};
    private static final String[] GREAT_WORDS = {"Scrumptious!", "Berry Good!", "Juicetastic!", "Zest-acular!"};
    private static final String[] PERFECT_WORDS = {"Pearfect!!", "Mango-nificent!!", "Suck-culent!!", "Tropical Bliss!!", "Fruit-iculous!!"};  // very creative i know

    // constants for launchRandomFruit
    private static final int LAUNCH_Y = PANEL_HEIGHT;
    private static final int LAUNCH_MARGIN = 40;
    private static final double MIN_LAUNCH_SPEED = 14;
    private static final double MAX_LAUNCH_SPEED = 18;
    private static final int FRUIT_SIZE = 70;

    // spawn ramp, time between launches shrinks from START to END over the whole game
    private static final double START_SPAWN_INTERVAL_MS = 1800;
    private static final double END_SPAWN_INTERVAL_MS = 350;
    private double spawnTimerMs = START_SPAWN_INTERVAL_MS;  // counts down to the next launch

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
    
    private long lastPaintMs;
    private long lastWorkMs;

    private int score;
    private ActionListener scoreListener;  // update HUD when score changes

    // Initialize sound effects
    private final SoundPlayer cutSounds = new SoundPlayer("sounds/", "cut", 12, -7f);  // last param is volume reduction in DB, adjust by ear

    public GamePanel() {
        // initialise enummap
        fruitImages = new EnumMap<>(FruitType.class);

        for (FruitType type : FruitType.values()) {
            fruitImages.put(type, scaleImage(ImageLoader.load(type.getImageFile()), FRUIT_SIZE));
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
                long workStart = System.nanoTime();                
                long currentTime = System.currentTimeMillis();

                long elapsedMs = currentTime - previousTickTime;
                if (elapsedMs > 25) {
                    System.out.println("slow tick: " + elapsedMs + "ms (prev tick work " + lastWorkMs
                            + "ms, prev paint " + lastPaintMs + "ms)");
                }

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

                while (iterator.hasNext()) {
                    FlyingObject obj = iterator.next();

                    if (obj != null) {
                        obj.update();

                        // Check if object falls off the bottom of the screen
                        if (obj.getY() >= panelHeight()) {
                            // remove everything that leaves the screen, halves included (the spawn timer launches the next fruit)
                            iterator.remove();

                            // only whole fruit that got away costs points, halves are just cleaned up
                            if (obj instanceof Fruit && !((Fruit) obj).isSubFruit()) {
                                Fruit fruit = (Fruit) obj;
                                int pointCount = fruit.getHitsRemaining();
                                int missValue = fruit.getType().getBaseScore();

                                // spawn -points text
                                for (int count = 1; count <= pointCount; count++) {  // how many times do we show "-points"?
                                    int variancex = random.nextInt(-30, 31);  // add a bit of variance in spawn coordinates (should be tweaked)
                                    int variancey = random.nextInt(-15, 16);
                                    spawnFloatingText("-" + missValue, fruit.getCx() + variancex,
                                            panelHeight() - 15 + variancey, POINTS_FONT, Color.RED, 40);  // 15 is hardcoded low point on the screen
                                    addScore(-missValue);
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
                // update floating text (points and praise words)
                Iterator<FloatingText> textIterator = floatingTexts.iterator();
                while (textIterator.hasNext()) {
                    FloatingText t = textIterator.next();
                    t.update();
                    if (t.isDead()) {
                        textIterator.remove();
                    }
                }

                // count down to the next launch, launches get faster the longer the game goes on
                spawnTimerMs -= elapsedMs;

                if (spawnTimerMs <= 0) {
                    // only launch if there's room on screen for another whole fruit
                    if (countWholeFruits() < maxWholeFruits()) {
                        launchRandomFruit();
                    }
                    spawnTimerMs = nextSpawnInterval();
                }
                lastWorkMs = (System.nanoTime() - workStart) / 1_000_000;
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
                long t = System.nanoTime();

                // Add point to visual trail
                trailPoints.add(currentPoint);
                if (trailPoints.size() > MAX_TRAIL_POINTS) {
                    trailPoints.remove(0);
                }

                // use an older trail point for smoother swipe direction
                Point cutStart;

                if (trailPoints.size() >= 5) {
                    cutStart = trailPoints.get(trailPoints.size() - 5);
                } else {
                    cutStart = previousPoint;
                }

                // calculate swipe direction
                Vector2D cutDir = Vector2D.between(cutStart, currentPoint);

                // loop over a copy because slice() adds and removes fruit in activeObjects
                for (FlyingObject obj : new ArrayList<>(activeObjects)) {

                    // only whole fruit can be cut, skip halves
                    if (!(obj instanceof Fruit)) {
                        continue;
                    }

                    Fruit fruit = (Fruit) obj;

                    if (fruit.isSubFruit() || fruit.isCompleted()) {
                        continue;
                    }

                    // Slice intersection check between previousPoint and currentPoint
                    boolean touching = fruit.lineIntersectsFruit(previousPoint, currentPoint);

                    if (!touching) {
                        // swipe left the fruit, so the next entry counts as a new hit
                        fruit.setTouched(false);

                    } else if (!fruit.isTouched()) {

                        // ignore short swipes
                        if (cutDir.getMagnitude() >= 10) {  // this is where real valid slices get detected

                            // mark as touched so this same swipe can't hit again
                            fruit.setTouched(true);

                            // score the hit
                            double accuracy = fruit.getCutAccuracy(cutStart, currentPoint);
                            fruit.recordAccuracy(accuracy);

                            int points = (int) Math.round(fruit.getType().getBaseScore()
                                    * Fruit.centerMultiplier(accuracy));  // base points * how close u got to the center
                            addScore(points);

                            // "+points" every hit when u cut
                            spawnFloatingText("+" + points, currentPoint.x, currentPoint.y - 10,
                                    POINTS_FONT, Color.WHITE, 40);
                            t = mark("points text", t);

                            spawnJuice(currentPoint, fruit.getType().getJuiceColor());  // particle effect
                            t = mark("juice", t);
                            cutSounds.playRandom();  // sound effect
                            t = mark("cut sound", t);

                            // takeHit() returns true only on the final hit
                            if (fruit.takeHit()) {
                                float scale = 1.0f + 0.2f * (float) accuracy;
                                double avgAccuracy = fruit.getAverageAccuracy();
                                // praise word above the fruit, only when it's fully sliced
                                double fx = fruit.getX() + fruit.getRadius();  // we can make this better to spawn it more randomly around the cut    
                                double fy = fruit.getY() - 15;
                                spawnFloatingText(pickPraise(avgAccuracy), fx, fy,
                                        WORD_FONT.deriveFont(WORD_FONT.getSize2D() * scale), praiseColor(avgAccuracy), 60);
                                t = mark("praise text", t);

                                fruit.slice(cutDir, previousPoint, 0.5);
                                t = mark("fruit.slice()", t);
                            }
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

    public void setScoreListener(ActionListener listener) {
        this.scoreListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        long paintStart = System.nanoTime();
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

        for (FloatingText t : floatingTexts) {
            t.draw(g2, getWidth());
        }
        long paintMs = (System.nanoTime() - paintStart) / 1_000_000;
        lastPaintMs = paintMs;
        if (paintMs > 14) {  // checking painting component
            System.out.println("slow paint: " + paintMs + "ms, objects=" + activeObjects.size()
                    + ", particles=" + particles.size() + ", texts=" + floatingTexts.size());
        }
    }
    
    private BufferedImage scaleImage(BufferedImage src, int size) {  // scale each image once at load instead to fix framerate
        if (src == null) {
            return null;
        }
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();
        return out;
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
        floatingTexts.clear();  // so old words dont carry over after restart
        spawnTimerMs = START_SPAWN_INTERVAL_MS;  // spawn ramp starts from the slow pace again
        launchRandomFruit();
        score = 0;

        repaint();
    }

    private String pickPraise(double accuracy) {
        String[] pool;
        if (accuracy >= 0.9) {
            pool = PERFECT_WORDS;
        } else if (accuracy >= 0.75) {
            pool = GREAT_WORDS;
        } else if (accuracy >= 0.5) {
            pool = GOOD_WORDS;
        } else {
            pool = OK_WORDS;
        }

        return pool[random.nextInt(pool.length)];  // pick random from list
    }

    private Color praiseColor(double accuracy) {
        if (accuracy >= 0.9) {
            return new Color(255, 120, 200);  // hot pink
        }
        if (accuracy >= 0.75) {
            return new Color(255, 200, 40);   // mango yellow
        }
        if (accuracy >= 0.4) {
            return new Color(120, 230, 120);  // lime
        }
        return Color.WHITE;
    }

    private void spawnFloatingText(String text, double x, double y, Font font, Color color, int life) {
        floatingTexts.add(new FloatingText(text, x, y, life, font, color));
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

    // time until the next launch, shrinks as the game goes on with a bit of randomness
    private double nextSpawnInterval() {
        double progress = gameTimer.getProgress();  // 0 at the start, 1 when time is up

        // the 1.2 makes the early game stay calm and the end ramp up faster (1.0 = straight line)
        double base = START_SPAWN_INTERVAL_MS
                + (END_SPAWN_INTERVAL_MS - START_SPAWN_INTERVAL_MS) * Math.pow(progress, 1.2);

        return base * randomBetween(0.8, 1.2);
    }

    // cap on whole fruits on screen, 1 at the start and 6 at the end
    private int maxWholeFruits() {
        return 1 + (int) (gameTimer.getProgress() * 5);
    }

    // counts whole fruits on screen, halves dont count
    private int countWholeFruits() {
        int count = 0;

        for (FlyingObject obj : activeObjects) {
            if (obj instanceof Fruit && !((Fruit) obj).isSubFruit()) {
                count++;
            }
        }

        return count;
    }

    // creates and launches a random fruit
    private void launchRandomFruit() {
        FruitType randomType = getRandomFruitType();

        // random start position of fruit
        double startX = randomBetween(LAUNCH_MARGIN, panelWidth() - FRUIT_SIZE - LAUNCH_MARGIN);
        double startY = panelHeight();

        Fruit fruit = new Fruit(startX, startY, 35, randomType, this);
        int hits = randomType.getSlicesRequired();
        fruit.setGravity(0.4 - 0.05 * (hits - 1));  // 0.4 grav at 1 hit, 0.35 at 2, and 0.3 at 3

        // scale random vertical launch speed so fruit reach the same fraction of the screen height at any window size
        double speedScale = Math.sqrt(panelHeight() / (double) PANEL_HEIGHT);
        double vy = -randomBetween(MIN_LAUNCH_SPEED, MAX_LAUNCH_SPEED) * speedScale;

        // flight time
        double flightTicks = 2 * Math.abs(vy) / FlyingObject.GRAVITY;

        // pick where fruit land to guarantee it stays on screen 
        double targetX = randomBetween(LAUNCH_MARGIN, panelWidth() - FRUIT_SIZE - LAUNCH_MARGIN);

        // horizontal speed
        double vx = (targetX - startX) / flightTicks;

        // random clockwise or anticlockwise spinning
        double spinSpeed = random.nextBoolean() ? 0.05 : -0.05;

        fruit.subFruit = false;

        fruit.setXVelocity(vx);
        fruit.setYVelocity(vy);
        fruit.setRotationSpeed(spinSpeed);
        activeObjects.add(fruit);
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
    
    private int panelWidth() {
        return getWidth() > 0 ? getWidth() : PANEL_WIDTH;
    }

    private int panelHeight() {
        return getHeight() > 0 ? getHeight() : PANEL_HEIGHT;
    }

    private void addScore(int points) {
        score = Math.max(0, score + points);
        if (scoreListener != null) {
            scoreListener.actionPerformed(
                    new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "SCORE"));
        }
    }

    public int getScore() {
        return score;
    }

    // draw juice splash particles over fruit
    private void drawParticles(Graphics2D g2) {
        for (Particle particle : particles) {
            particle.draw(g2);
        }
    }
    
    // temporary: prints how long a step took if it was slow
    private long mark(String label, long since) {
        long now = System.nanoTime();
        long ms = (now - since) / 1_000_000;
        if (ms >= 3) {
            System.out.println(label + ": " + ms + "ms");
        }
        return now;
    }
}