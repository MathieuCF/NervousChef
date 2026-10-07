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

/**
 *
 * @author lailx
 */
public class GamePanel extends javax.swing.JPanel {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 500;
    private final Timer timer;
    private int x = 100;
    private int y = 100;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLUE);
        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                x += 1;
                repaint();
            }

        });
        startGameLoop();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.red);
        g2.fillOval(x, y, 50, 50);
    }

    public void startGameLoop() {
        timer.start();
    }

    public void stopGameLoop() {
        timer.stop();
    }
    

}
