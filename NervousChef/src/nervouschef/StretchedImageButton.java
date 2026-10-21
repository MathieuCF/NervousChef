/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JButton;

/**
 *
 * @author lailx
 */
public class StretchedImageButton extends JButton {

    private final BufferedImage image;

    public StretchedImageButton(String fileName) {

        image = ImageLoader.load(fileName);

        if (image != null) {
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setForeground(Color.WHITE);
        }

        setPreferredSize(new Dimension(100, 60));
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        if (image != null) {

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }

        super.paintComponent(g);
    }
}
