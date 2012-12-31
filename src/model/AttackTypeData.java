/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author unreal
 */
public class AttackTypeData {
    private boolean active;
    private String data;
    
    public AttackTypeData(boolean active) {
        this.active = active;
    }
    
    public AttackTypeData(boolean active, String data) {
        this.active = active;
        this.data = data;
    }    
    
    public boolean isActive() {
        return active;
    }
    
    public String getData() {
        return data;
    }
    
}
