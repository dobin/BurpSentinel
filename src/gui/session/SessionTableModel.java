package gui.session;

import java.util.Collections;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;
import util.BurpCallbacks;
import util.UiUtil;

/**
 *
 * @author unreal
 */
public class SessionTableModel extends AbstractTableModel {

    private LinkedList<SessionUser> sessionUsers = new LinkedList<SessionUser>();

    public SessionTableModel() {
        UiUtil.restoreSessions(sessionUsers);
    }
    
    
    @Override
    public int getRowCount() {
        return sessionUsers.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
                return sessionUsers.get(rowIndex).getName();
            case 1:
                return sessionUsers.get(rowIndex).getValue();
            default:
                return "Null";
        }
    }
    
    @Override
    public void setValueAt(Object value, int row, int column) {
        switch(column) {
            case 0:
                sessionUsers.get(row).setName( (String) value);
                break;
            case 1:
                sessionUsers.get(row).setValue( (String) value);
                break;
            default:
                return;
        }
    }

    int getUserCount() {
        return getRowCount();
    }

    SessionUser getUserAt(int newIndex) {
        return sessionUsers.get(newIndex);
    }

    
    void addLine() {
        SessionUser su = new SessionUser("User " + (sessionUsers.size() + 1), "");
        sessionUsers.add(su);
        
        this.fireTableDataChanged();
    }
    
    void addDefaultNew() {
        SessionUser su = new SessionUser("User 1", "useraaaa");
        sessionUsers.add(su);
        
        SessionUser suu = new SessionUser("User 2", "userbbb");
        sessionUsers.add(suu);
        
        this.fireTableDataChanged();
    }
    
    
    @Override
    public boolean isCellEditable(int row, int column) {
       return true;
    }

    String getSessionValueFor(String selectedSessionUser) {
        for (SessionUser u: sessionUsers) {
            if (u.getName().equals(selectedSessionUser) 
                    || u.getName().equals("<" + selectedSessionUser + ">")) {
                return u.getValue();
            }
        }
        return null;
    }

    LinkedList<SessionUser> getSessionUsers() {
        return sessionUsers;
    }

    /* Check if there are multiple entries with the same username
     * That's a no-go
     */
    boolean isSaneUserInput() {
        LinkedList<String> usernames = new LinkedList<String>();
        for(SessionUser u: sessionUsers) {
            if (u.getValue().equals("")) {
                return false;
            }
            
            usernames.add(u.getName());
        }
        
        Collections.sort(usernames);
        for (int n=0; n < usernames.size() - 1; n++) {
            if (usernames.get(n).equals(usernames.get(n+1))) {
                return false;
            }
        }
        
        return true;
    }

    void storeUiPrefs() {
        UiUtil.storeSessions(sessionUsers);
    }

    void deleteEntry(int selectedRow) {
        sessionUsers.remove(selectedRow);
        this.fireTableDataChanged();
    }
    
}
