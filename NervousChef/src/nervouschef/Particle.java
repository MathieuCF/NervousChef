/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author lailx
 */
public class Particle {

    private double x;
    private double y;

    private double vx;
    private double vy;

    private int life;
    private int maxLife;

    private double size;

    private Color baseColor;

    public Particle(double x, double y, double vx, double vy,
            int life, double size, Color baseColor) {

        this.x = x;
        this.y = y;

        this.vx = vx;
        this.vy = vy;

        this.life = life;
        this.maxLife = life;

        this.size = size;

        this.baseColor = baseColor;
    }

    public void update() {
        x += vx;
        y += vy;

        vy += 0.25;

        life--;

    }

    public boolean isDead() {
        return life <= 0;

    }

    public void draw(Graphics2D g2) {
        double fraction = (double) life / maxLife;

        int alpha
                = (int) (fraction * 255);

        alpha = Math.max(
                0,
                Math.min(255, alpha)
        );

        Color drawColor
                = new Color(
                        baseColor.getRed(),
                        baseColor.getGreen(),
                        baseColor.getBlue(),
                        alpha
                );

        g2.setColor(drawColor);

        double currentSize
                = size * fraction;

        g2.fillOval(
                (int) x,
                (int) y,
                (int) currentSize,
                (int) currentSize
        );
    }

}
