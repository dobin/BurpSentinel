package gui.botRight;

import gui.session.categorizer.CategorizerManager;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import model.SentinelHttpMessage;

/**
 *
 * @author Dobin
 */
public class PanelRightModel extends AbstractTableModel implements Observer {

    private LinkedList<SentinelHttpMessage> messages;
    private PanelRightUi parent;
    
    public PanelRightModel(PanelRightUi parent) {
        this.parent = parent;
        messages = new LinkedList<SentinelHttpMessage>();
        CategorizerManager.getInstance().addObserver(this);
    }
    
    /*
     * We observe 2 things:
     * - httpmessages 
     *   - currently ignored
     * - categorizer
     *   - used to update the httpmessages if categories change
     */
    @Override
    public void update(Observable o, Object arg) {
//        System.out.println("--- AAA");
//        if (o.getClass().equals(SentinelHttpMessage.ObserveResult.ATTACKRESULT)) {
//            System.out.println("--- BBB");
//            this.fireTableDataChanged();
//        }
        if (o.getClass().equals(CategorizerManager.class)) {
            System.out.println("CATEGORIZE!");
        }
    }
    
    @Override
    public int getRowCount() {
        return messages.size();
    }

    @Override
    public int getColumnCount() {
        return 12;
    }
    
        
    @Override
    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0: return Integer.class;
            case 5: return Integer.class;
            case 6: return Integer.class;
            case 7: return Integer.class;
            case 8: return Integer.class;
            default: return String.class;
        }
    }
    
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "#";
            case 1:
                return "Type";
            case 2:
                return "Name";
            case 3:
                return "Original";
            case 4:
                return "Attack";
                
                
            case 5:
                return "Status";
            case 6:
                return "Length";
            case 7:
                return "#TAGS";
            case 8:
                return "Time";
                
            case 9:
                return "Test";
            case 10:
                return "Result";
            case 11:
                return "Trans";
                
            default:
                return "hmm";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SentinelHttpMessage m = messages.get(rowIndex);
        
        switch(columnIndex) {
            case 0: return rowIndex;
            case 1: return m.getReq().getChangeParam().getTypeStr();
            case 2: return m.getReq().getChangeParam().getName();
            case 3: return m.getReq().getOrigParam().getValue();
            case 4: return m.getReq().getChangeParam().getValue();
            case 5: return m.getRes().getHttpCode();
            case 6: return m.getRes().getSize();
            case 7: return m.getRes().getDom();
            case 8: return (int) m.getLoadTime();
            case 9: 
                if (m.getAttackResult() != null) {
                    return m.getAttackResult().getAttackName();
                } else {
                    return "Null";
                }
            case 10:
                if (m.getAttackResult() != null) {
                    boolean successful = m.getAttackResult().getSuccess();
                    if (successful) {
                        return m.getAttackResult().getAttackType();
                    } else {
                        return "-";
                    }
                } else {
                    return "Null";
                }
            case 11:
                return m.getRes().getCategoriesString();
            default: return "AAA";
        }
    }

    void addMessage(SentinelHttpMessage httpMessage) {
        messages.add(httpMessage);
        httpMessage.setTableIndexAttack(messages.size() - 1);
        httpMessage.addObserver(this);
        
        int selected = parent.getSelected();
        this.fireTableDataChanged();
        parent.setSelected(selected);
        
    }

    public SentinelHttpMessage getHttpMessage(int n) {
        return messages.get(n);
    }
    
}
