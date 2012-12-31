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
public class SentinelHighlight {
    private int start;
    private int end;
    private Color c;
    
    public SentinelHighlight(int start, int end, Color c) {
        this.start = start;
        this.end = end;
        this.c = c;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public Color getColor() {
        return c;
    }
}
