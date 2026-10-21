/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

/**
 *
 * @author lailx
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class ButtonStyler {

    private ButtonStyler() {
    }

    // style a button using an image
    public static void styleImageButton(JButton button, String imageFile, int width) {
        BufferedImage image = ImageLoader.load(imageFile);

        //leave normal text button if image is missing
        if (image == null) {
            return;
        }

        image = trimTransparentBorder(image);

        BufferedImage scaled = scaleImage(image, width);

        button.setIcon(new ImageIcon(scaled));

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // place text ontop of image
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        button.setVerticalTextPosition(SwingConstants.CENTER);

        button.setForeground(Color.WHITE);

        button.setFont(button.getFont().deriveFont(Font.BOLD));

        button.setMargin(new Insets(0, 0, 0, 0));

    }

    // scale image while keeping its aspect ratio
    private static BufferedImage scaleImage(BufferedImage image, int width) {
        int height = width * image.getHeight() / image.getWidth();

        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = scaled.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();
        return scaled;
    }

    private static BufferedImage trimTransparentBorder(BufferedImage image) {

        int minX = image.getWidth();
        int minY = image.getHeight();

        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                int alpha = (image.getRGB(x, y) >> 24) & 0xff;

                if (alpha > 20) {

                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);

                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return image;
        }

        return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

}
