/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.viewMessage;

import java.awt.Color;

/**
 *
 * @author unreal
 */
public class ResponseHighlight {
    private String str;
    private Color color;
    
    public ResponseHighlight(String str, Color color) {
        this.str = str;
        this.color = color;
    }
    
    public String getStr() {
        return str;
    }
    
    public Color getColor() {
        return color;
    }
    
}
