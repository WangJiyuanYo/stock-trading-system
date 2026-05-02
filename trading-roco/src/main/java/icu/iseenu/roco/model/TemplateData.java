package icu.iseenu.roco.model;

import java.util.List;

/**
 * 处理后的模板数据模型
 */
public class TemplateData {
    private String title;
    private String subtitle;
    private int productCount;
    private RoundInfo roundInfo;
    private List<Product> products;
    
    // 为了适配原版HTML模板的变量
    private String resPath = "";
    private String background = "img/bg.C8CUoi7I.jpg";
    private boolean titleIcon = true;
    
    public TemplateData() {
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public int getProductCount() {
        return productCount;
    }
    
    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
    
    public RoundInfo getRoundInfo() {
        return roundInfo;
    }
    
    public void setRoundInfo(RoundInfo roundInfo) {
        this.roundInfo = roundInfo;
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    public String getResPath() {
        return resPath;
    }
    
    public void setResPath(String resPath) {
        this.resPath = resPath;
    }
    
    public String getBackground() {
        return background;
    }
    
    public void setBackground(String background) {
        this.background = background;
    }
    
    public boolean isTitleIcon() {
        return titleIcon;
    }
    
    public void setTitleIcon(boolean titleIcon) {
        this.titleIcon = titleIcon;
    }
}
