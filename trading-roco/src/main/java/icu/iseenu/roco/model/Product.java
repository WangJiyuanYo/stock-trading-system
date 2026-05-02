package icu.iseenu.roco.model;

/**
 * 商品模型
 */
public class Product {
    private String name;
    private String image;
    private String timeLabel;
    
    public Product() {
    }
    
    public Product(String name, String image, String timeLabel) {
        this.name = name;
        this.image = image;
        this.timeLabel = timeLabel;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getTimeLabel() {
        return timeLabel;
    }
    
    public void setTimeLabel(String timeLabel) {
        this.timeLabel = timeLabel;
    }
}
