package nervouschef;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;

public class FloatingText {

    private final String text;
    private final Font font;
    private final Color color;

    private double x;
    private double y;
    private double vy = -1.6;   // starts moving up

    private int life;
    private final int maxLife;

    public FloatingText(String text, double x, double y, int life, Font font, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.life = life;
        this.maxLife = life;
        this.font = font;
        this.color = color;
    }

    public void update() {
        y += vy;
        vy *= 0.96;   // rises quickly, then slows to a float
        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Graphics2D g2, int panelWidth) {
        // fully visible for the first half of its life, then fades out
        double fraction = (double) life / maxLife;
        int alpha = (int) (255 * Math.min(1.0, fraction * 2));
        alpha = Math.max(0, Math.min(255, alpha));

        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int width = fm.stringWidth(text);

        // centre on x, but keep the whole word on screen
        double drawX = Math.max(6, Math.min(panelWidth - width - 6, x - width / 2.0));

        GlyphVector gv = font.createGlyphVector(g2.getFontRenderContext(), text);
        Shape outline = gv.getOutline((float) drawX, (float) y);

        Stroke oldStroke = g2.getStroke();

        // dark outline so it reads on any background
        g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(40, 20, 60, alpha));
        g2.draw(outline);

        g2.setStroke(oldStroke);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2.fill(outline);
    }
}