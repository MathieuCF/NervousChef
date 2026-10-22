/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Path2D;

/**
 *
 * @author lailx
 */
public class Bird {

    private double x;
    private double y;

    private double vx;
    private double vy;

    private double size;

    private double flapPhase;
    private double flapSpeed;

    private int alpha;
    
    public Bird(double x, double y, double vx, double vy, double size, double flapSpeed, int alpha){
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
        this.flapSpeed = flapSpeed;
        this.alpha = alpha;
        
        this.flapPhase = Math.random() * Math.PI * 2;
    }

    public void update() {
        // move bird
        x += vx;
        y += vy;

        // wing animation
        flapPhase += flapSpeed;
        y += Math.sin(flapPhase) * 0.2;
    }

    // removes bird after they leave screen
    public boolean isOffScreen(int width, int height) {
        return x > width + size || y < -size;
    }

    public void draw(Graphics2D g2) {
        
        double wingOffset = size * Math.sin(flapPhase);

        Path2D.Double bird = new Path2D.Double();
        
        // left wing
        bird.moveTo(x, y);
        bird.quadTo(x - size / 2, y - wingOffset, x - size, y );
        
        // right wing
        bird.moveTo(x, y);
        bird.quadTo(x + size / 2, y - wingOffset, x + size, y );
        
        Color oldColor = g2.getColor();
        
        Stroke oldStroke = g2.getStroke();
        
        g2.setColor(new Color(0,0,0,alpha));
        
        float thickness = (float) Math.max(1.5, size / 10.0);
        
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        g2.draw(bird);
        
        g2.setColor(oldColor);
        
        g2.setStroke(oldStroke);
        
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
}
