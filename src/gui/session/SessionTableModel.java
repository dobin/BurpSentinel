package gui.session;

import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author unreal
 */
public class SessionTableModel extends AbstractTableModel {

    private LinkedList<SessionUser> sessionUsers = new LinkedList<SessionUser>();

    public SessionTableModel() {
        addDefaultNew();
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

    void addDefaultNew() {
        SessionUser su = new SessionUser("User A", "useraaaa");
        sessionUsers.add(su);
        
        SessionUser suu = new SessionUser("User B", "userbbb");
        sessionUsers.add(suu);
        
        this.fireTableDataChanged();
    }
    
    
    @Override
    public boolean isCellEditable(int row, int column) {
       return true;
    }

    String getSessionValueFor(String selectedSessionUser) {
        for (SessionUser u: sessionUsers) {
            if (u.getName().equals(selectedSessionUser)) {
                return u.getValue();
            }
        }
        return null;
    }

    LinkedList<SessionUser> getSessionUsers() {
        return sessionUsers;
    }
    
}
