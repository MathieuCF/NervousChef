/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lailx
 */
//Simple game panel used for rendering and testing the game loop.
public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private static final int START_X = 100;
    private static final int START_Y = 100;
    private static final int TICK_DELAY_MS = 16;
    private static final int OBJECT_DIAMETER = 50;
    private static final int MAX_TRAIL_POINTS = 20;

    private final Timer timer;
    private int x = START_X;
    private int y = START_Y;
    private List<Point> trailPoints = new ArrayList<>();
    private Point previousPoint;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);
        timer = new Timer(TICK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                x += 1;

                if (!trailPoints.isEmpty()) {
                    trailPoints.remove(0);
                    //shrinks tail when mouse stops moving
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

                //testing
//                    System.out.println(
//                            previousPoint.x + ", "
//                            + previousPoint.y + " -> "
//                            + currentPoint.x + ", "
//                            + currentPoint.y);
                previousPoint = currentPoint;
                
                trailPoints.add(currentPoint);
                if (trailPoints.size() > MAX_TRAIL_POINTS) {
                    trailPoints.remove(0);
                    // remove first/oldest point in list so the front of the tail disappears
                }

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                trailPoints.clear();
                previousPoint = null;
                //reset previous point after every drag to generate a new drag and not connect two unrelated points
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

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        for (int i = 1; i < trailPoints.size(); i++) {
            Point p1 = trailPoints.get(i - 1);
            Point p2 = trailPoints.get(i);

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        g2.setColor(Color.RED);
        g2.fillOval(x, y, OBJECT_DIAMETER, OBJECT_DIAMETER);
    }

    //starts game loop timer
    public void startGameLoop() {
        timer.start();
    }

    //stops game loop timer
    public void stopGameLoop() {
        timer.stop();
    }

    public void reset() {
        x = START_X;
        y = START_Y;
    }

}
