package nervouschef;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

public class FloatingText {

    private static final int PAD = 6;   // room for the outline
    private static final java.util.Map<String, BufferedImage> IMAGE_CACHE = new java.util.HashMap<>();

    private final BufferedImage image;  // text with outline, drawn once
    private double x;
    private double y;
    private double vy = -1.6;   // starts moving up

    private int life;
    private final int maxLife;

    public FloatingText(String text, double x, double y, int life, Font font, Color color) {
        this.x = x;
        this.y = y;
        this.life = life;
        this.maxLife = life;
        
        // reuse the rendered image if this word/size/colour was already built
        String key = text + "|" + font.getSize() + "|" + color.getRGB();
        image = IMAGE_CACHE.computeIfAbsent(key, k -> buildImage(text, font, color));
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
        float alpha = (float) Math.max(0, Math.min(1.0, fraction * 2));

        // centre on x, but keep the whole word on screen
        double drawX = Math.max(0, Math.min(panelWidth - image.getWidth(), x - image.getWidth() / 2.0));

        java.awt.Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g2.drawImage(image, (int) drawX, (int) y - image.getHeight() + PAD, null);
        g2.setComposite(old);
    }
    
    private static BufferedImage buildImage(String text, Font font, Color color) {  // buffer text as images, although not sure how useful this is
        // build the outline shape and size the image around it
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector gv = font.createGlyphVector(frc, text);
        Shape outline = gv.getOutline();
        java.awt.Rectangle bounds = outline.getBounds();
        

        BufferedImage img = new BufferedImage(bounds.width + PAD * 2, bounds.height + PAD * 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(PAD - bounds.x, PAD - bounds.y);
        

        // dark outline so it reads on any background
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 20, 60));
        g.draw(outline);

        g.setColor(color);
        g.fill(outline);
        g.dispose();
        return img;
    }
}