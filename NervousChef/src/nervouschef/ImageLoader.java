/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageLoader {

    private ImageLoader() {
    }

    // Loads image safely and returns null if not found
    public static BufferedImage load(String filename) {

        try {

            java.net.URL url = ImageLoader.class.getResource(filename);

            if (url != null) {
                return ImageIO.read(url);
            }

        } catch (IOException e) {

            System.out.println("Failed to load " + filename);
        }

        return null;
    }
}
