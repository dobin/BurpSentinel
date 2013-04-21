/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.session.categorizer;

/**
 *
 * @author dobin
 */
public class CategoryEntry {
    private String name;
    private String regex;
    
    public CategoryEntry(String name, String regex) {
        this.name = name;
        this.regex = regex;
    }
    
    public String getTag() {
        return name;
    }
    
    public String getRegex() {
        return regex;
    }
    
    public void setTag(String tag) {
        this.name = tag;
    }
    
    public void setRegex(String regex) {
        this.regex = regex;
    }
    
}
