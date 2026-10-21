/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

import java.awt.Color;

/**
 *
 * @author lailx
 */
public enum FruitType {
    
    // what image goes with what fruit 
    //and num of slices each fruit needs
    APPLE("apple.png", 1, Color.RED, 10),
    ORANGE("orange.png", 2, Color.ORANGE, 10),
    WATERMELON("watermelon.png", 3, Color.RED, 20),
    COCONUT("coconut.png", 1, Color.WHITE, 10),
    LEMONS("lemon.png", 2, Color.YELLOW, 10),
    PEACHES("peach.png", 3, Color.PINK, 10),
    KIWI("kiwi.png", 1, Color.GREEN, 10);
    
    private final String imageFile;
    private final int slicesRequired; 
    private final Color juiceColor;
    private final int baseScore;
    
    FruitType(String imageFile, int slicesRequired, Color juiceColor, int baseScore) {
        this.imageFile = imageFile;
        this.slicesRequired = slicesRequired;
        this.juiceColor = juiceColor;
        this.baseScore = baseScore;
    }
    
    public String getImageFile() {
        return imageFile;
    }
    
    public int getSlicesRequired() {
        return slicesRequired;
    }
    
    public Color getJuiceColor() {
        return juiceColor;
    }
    
    public int getBaseScore() {
        return baseScore;
    }
}
