/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nervouschef;

/**
 *
 * @author lailx
 */
public enum FruitType {
    // what image goes with what fruit 
    //and num of slices each fruit needs
    APPLE("apple.png", 1),
    ORANGE("orange.png", 2),
    WATERMELON("watermelon.png", 3),
    COCONUT("coconut.png", 1),
    LEMONS("lemon.png", 2),
    PEACHES("peach.png", 3),
    KIWI("kiwi.png", 1);
    
    private final String imageFile;
    private final int slicesRequired;
    
    FruitType(String imageFile, int slicesRequired) {
        this.imageFile = imageFile;
        this.slicesRequired = slicesRequired;
    }
    
    public String getImageFile() {
        return imageFile;
    }
    
    public int getSlicesRequired() {
        return slicesRequired;
    }
}
