package gui.botLeft;

import gui.session.SessionManager;
import gui.session.SessionUser;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author unreal
 */
public class PanelLeftComboBoxModel extends AbstractListModel implements ComboBoxModel, Observer {
    private SessionManager sessionManager;
    private String selected = "";
    private String origSessionValue = null;
    private String origSessionName = null;

    private LinkedList<String> elements;
    
    public PanelLeftComboBoxModel() {
        super();

        sessionManager = SessionManager.getInstance();
        sessionManager.addObserver(this);
        init();
    }
    
    private void init() {
        elements = new LinkedList<String>();
        
        if (origSessionValue == null) {
            elements.add("<default>");
            selected = "<default>";
            
            for (SessionUser user : sessionManager.getSessionUsers()) {
                elements.add(user.getName());
            }
            
        } else {
            SessionUser sessionUser = SessionManager.getInstance().getUserFor(origSessionValue);
            if (sessionUser == null) {
                elements.add("<default>");
                selected = "<default>";

                for (SessionUser user : sessionManager.getSessionUsers()) {
                    elements.add(user.getName());
                }

            } else {
                origSessionName = sessionUser.getName();

                elements.add("<" + origSessionName + ">");
                selected = "<" + origSessionName + ">";

                for (SessionUser user : sessionManager.getSessionUsers()) {
                    if (!user.getValue().equals(origSessionValue)) {
                        elements.add(user.getName());
                    }
                }
            }
        }
    }
    
    
    public void setOrigSession(String s) {
        this.origSessionValue = s;
//        this.selected = origSession;
        init();
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
        return elements.size();
    }

    @Override
    public Object getElementAt(int index) {
        return elements.get(index);
    }


    @Override
    public void update(Observable o, Object arg) {
        init();
        myupdate();
    }
    
}
