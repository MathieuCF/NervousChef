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
    APPLE("apple.png", 1, Color.RED),
    ORANGE("orange.png", 2, Color.ORANGE),
    WATERMELON("watermelon.png", 3, Color.RED),
    COCONUT("coconut.png", 1, Color.WHITE),
    LEMONS("lemon.png", 2, Color.YELLOW),
    PEACHES("peach.png", 3, Color.PINK),
    KIWI("kiwi.png", 1, Color.GREEN);
    
    private final String imageFile;
    private final int slicesRequired; 
    private final Color juiceColor;
    
    FruitType(String imageFile, int slicesRequired, Color juiceColor) {
        this.imageFile = imageFile;
        this.slicesRequired = slicesRequired;
        this.juiceColor = juiceColor;
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
}
