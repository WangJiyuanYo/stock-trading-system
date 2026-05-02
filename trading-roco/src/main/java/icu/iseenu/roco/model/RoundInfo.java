package icu.iseenu.roco.model;

import java.util.List;

/**
 * 轮次信息模型
 */
public class RoundInfo {
    private Object current; // 可以是Integer或String("未开放")
    private int total;
    private String countdown;
    
    public RoundInfo() {
    }
    
    public RoundInfo(Object current, int total, String countdown) {
        this.current = current;
        this.total = total;
        this.countdown = countdown;
    }
    
    // Getters and Setters
    public Object getCurrent() {
        return current;
    }
    
    public void setCurrent(Object current) {
        this.current = current;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public String getCountdown() {
        return countdown;
    }
    
    public void setCountdown(String countdown) {
        this.countdown = countdown;
    }
}
