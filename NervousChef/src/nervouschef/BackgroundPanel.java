/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;


public class BackgroundPanel extends JPanel {
    
   
    private final BufferedImage background;

    public BackgroundPanel(String fileName, JPanel content) {

        this.background =
                ImageLoader.load(fileName);

        setLayout(new BorderLayout());

        content.setOpaque(false);

        add(content, BorderLayout.CENTER);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }
        
    }
}
