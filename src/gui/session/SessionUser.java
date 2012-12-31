/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.session;

/**
 *
 * @author unreal
 */
public class SessionUser {
    
    private String name;
    private String value;
    
    public SessionUser(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    void setName(String name) {
        this.name = name;
    }
    
    void setValue(String value) {
        this.value = value;
    }
    
}
