/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author unreal
 */
public class XssIndicator {

    private String indicator = "";
    static private XssIndicator xssIndicator;
            
    public static XssIndicator getInstance() {
        if (xssIndicator == null) {
            xssIndicator = new XssIndicator();
        }
        
        return xssIndicator;
    }
    
    
    
    public XssIndicator() {
        this.indicator = "CSNC";
    }
    
    public String getIndicator() {
        return indicator;
                
    }
    
}
