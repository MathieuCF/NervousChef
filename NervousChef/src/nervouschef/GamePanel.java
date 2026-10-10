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

public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int TICK_DELAY_MS = 16;
    private static final int MAX_TRAIL_POINTS = 20;

    private final Timer timer;
    private List<Point> trailPoints = new ArrayList<>();
    private Point previousPoint;

    // Test fruit instance spawned lower down so it arcs up into view
    private Fruit testFruit = new Fruit(600, 450);

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);

        timer = new Timer(TICK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Gradually remove old trail points for fading effect
                if (!trailPoints.isEmpty()) {
                    trailPoints.remove(0);
                }

                // Update physics every frame
                if (testFruit != null) {
                    testFruit.update(); 
                    
                    // Reset block if it falls off the bottom of the screen
                    if (testFruit.getY() >= PANEL_HEIGHT + 50) {
                        testFruit.resetTest(600, 450);
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
                    // Turning positions to int for easier pixel calculations
                    int tx = (int) testFruit.getX();
                    int ty = (int) testFruit.getY();
                    int tSize = (int) (testFruit.getRadius() * 2);
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

        // 1. Draw the slicing trail
        g2.setColor(Color.WHITE);
        for (int i = 0; i < trailPoints.size() - 1; i++) {
            Point p1 = trailPoints.get(i);
            Point p2 = trailPoints.get(i + 1);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // 2. Draw the test fruit / cube
        if (testFruit != null) {
            testFruit.draw(g2);
        }
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