package gui.botLeft;

import gui.session.SessionManager;
import gui.session.SessionUser;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author unreal
 */
public class PanelLeftComboBoxModel extends AbstractListModel implements ComboBoxModel {
    private SessionManager sessionManager;
    private String selected = "";
    private String origSession = null;

    public PanelLeftComboBoxModel() {
        super();

        selected = "<default>";
        sessionManager = SessionManager.getInstance();
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (String) anItem;
    }

    public void myupdate() {
        this.fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    @Override
    public int getSize() {
        //return 2 + sessionManager.getUserCount();
        return 1 + sessionManager.getUserCount();
    }

    @Override
    public Object getElementAt(int index) {
        switch (index) {
            case 0:
                if (origSession != null) {
                    return origSession;
                } else {
                    return "<default>";
                }
            //case 1:
            //    return "<new>";
        }

        // Get additional
        //int newIndex = index - 2;
        int newIndex = index - 1;
        String r = sessionManager.getUserAt(newIndex).getName();
        return r;
    }

    public void setOrigSession(String s) {
        this.origSession = "<" + s + ">";
        this.selected = origSession;
    }
    
    void selectIfPossible(String value) {
        if (value == null) {
            return;
        }
        
        SessionUser u = sessionManager.getUserFor(value);
        if (u != null) {
            setSelectedItem(u.getName());
        }
    }
    
}
