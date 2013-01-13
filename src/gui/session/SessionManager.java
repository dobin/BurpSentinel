/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.session;

import java.util.LinkedList;
import java.util.Observable;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class SessionManager extends Observable {

    private static SessionManager sessionManager;
    private SessionManagerUi sessionManagerUi;
        
    
    public SessionManager() {
        this.sessionManagerUi = new SessionManagerUi();
    }
    
    public static SessionManager getInstance() {
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }
        
        return sessionManager;
    }
    
    public void show() {
        if (sessionManagerUi == null) {
            sessionManagerUi = new SessionManagerUi();
            
        }
        sessionManagerUi.setVisible(true);
    }

    public int getUserCount() {
        return sessionManagerUi.getUserCount();
    }

    public SessionUser getUserAt(int newIndex) {
        return sessionManagerUi.getUserAt(newIndex);
    }

    public String getSessionVarName() {
        return sessionManagerUi.getSessionVarName();
    }

    public String getValueFor(String selectedSessionUser) {
        return sessionManagerUi.getSessionValueFor(selectedSessionUser);
    }

    public LinkedList<SessionUser> getSessionUsers() {
        return sessionManagerUi.getSessionUsers();
    }

    public SessionUser getUserFor(String value) {
                BurpCallbacks.getInstance().print("y1");
    
        for (int n = 0; n < getUserCount(); n++) {
            SessionUser u = getUserAt(n);

            if (value.equals(u.getValue())) {
                return u;
            }
        }
        
                        BurpCallbacks.getInstance().print("y2");
        
        return null;
    }

    void myNotify() {
        this.setChanged();
        this.notifyObservers();
    }
    
}
