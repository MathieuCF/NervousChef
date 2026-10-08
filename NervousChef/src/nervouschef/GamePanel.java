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

    // Create our test fruit object instance (Step 1 test)
    private Fruit testFruit = new Fruit(500,400);

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);

        timer = new Timer(TICK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Gradually remove old trail points
                if (!trailPoints.isEmpty()) {
                    trailPoints.remove(0);
                }
                if (testFruit != null) {
                    testFruit.update(); // Update physics every frame
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
                previousPoint = currentPoint;

                trailPoints.add(currentPoint);
                if (trailPoints.size() > MAX_TRAIL_POINTS) {
                    trailPoints.remove(0);
                }
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

        // 1. Draw Laila's slicing trail
        g2.setColor(Color.WHITE);
        for (int i = 1; i < trailPoints.size(); i++) {
            Point p1 = trailPoints.get(i - 1);
            Point p2 = trailPoints.get(i);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // 2. Step 1: Draw the Fruit's placeholder square!
        if (testFruit != null) {
            testFruit.draw(g2);
        }
    }

    // Starts game loop timer
    public void startGameLoop() {
        timer.start();
    }

    // Stops game loop timer
    public void stopGameLoop() {
        timer.stop();
    }

    // Resets the game panel state
    public void reset() {
        if (trailPoints != null) {
            trailPoints.clear();
        }
        previousPoint = null;
        repaint();
    }
}