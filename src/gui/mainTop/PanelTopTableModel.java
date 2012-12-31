package gui.mainTop;

import gui.session.SessionManager;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import model.SentinelHttpMessage;
import util.BurpCallbacks;

/**
 * The table model corresponding to PanelTopUi
 * 
 * PanelTopUi will display all HttpMessage from user/burp in a table
 * in the top part of the window.
 * 
 * @author unreal
 */
public class PanelTopTableModel extends AbstractTableModel implements Observer {
    private PanelTopUi parent;
    
    public PanelTopTableModel(PanelTopUi parent) {
        this.parent = parent;
    }
    
    public void reset() {
        myMessages = new LinkedList<SentinelHttpMessage>();
        this.fireTableDataChanged();
    }

    private LinkedList<SentinelHttpMessage> myMessages = new LinkedList<SentinelHttpMessage>();


    
    public void addMessage(SentinelHttpMessage message) {
        myMessages.add(message);
        message.addObserver(this);
        message.setTableIndexMain(myMessages.size() - 1);
        
        // TODO
        //this.fireTableRowsInserted(myMessages.size() - 1, myMessages.size());
        this.fireTableDataChanged();
        parent.setSelected(myMessages.size() - 1);
    }

    public SentinelHttpMessage getMessage(int rowIndex) {
        return myMessages.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return myMessages.size();
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    
    @Override
    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0: return Integer.class;
            default: return String.class;
        }
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "#";
            case 1: 
                return "Method";
            case 2:
                return "URL";
            case 3: 
                return "Comment";
            case 4:
                return "Interesting";
            case 5:
                return "Session";
            case 6:
                return "Vulnerable";
            case 7:
                return "Created";
            case 8:
                return "Modified";
            default:
                return "hmm";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        //if (rowIndex > myMessages.size()) {
        //    return "asdf";
        //}
        SentinelHttpMessage httpMessage = myMessages.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return httpMessage.getReq().getMethod();
            case 2:
                URL url = null;
                try {
                    url = httpMessage.getReq().getUrl();
                } catch (Exception ex) {
                    BurpCallbacks.getInstance().print("getValueAt(): getUrl() failed on index: " + rowIndex);
                    return "<error getting url>";
                }
                return url.toString();
            case 3:
                if (httpMessage.getComment() == null) {
                    return "";
                } else {
                    return httpMessage.getComment();
                }
            case 4:
                return httpMessage.getInterestingFact();
            case 5:
                return httpMessage.getReq().getSessionValueTranslated();
            case 6:
                return hasVulns(httpMessage);
            case 7:
                String s = "";
                SimpleDateFormat ft = new SimpleDateFormat ("HH:mm:ss dd.MM");
                s = ft.format(httpMessage.getCreateTime());
                return  s;
            case 8:
                String ss = "";
                SimpleDateFormat fft = new SimpleDateFormat ("HH:mm:ss dd.MM");
                Date d = httpMessage.getModifyTime();
                if (d == null) {
                    return "-";
                } else {
                    ss = fft.format(d);
                    return  ss;
                }
            default:
                return "";
        }
    }

    
    private String hasVulns(SentinelHttpMessage httpMessage) {
        
        for(SentinelHttpMessage m: httpMessage.getHttpMessageChildren()) {
            if (m.getAttackResult() == null) {
                System.out.println("PanelTopModel: message has no attack results!");
                continue;
            }
            
            if (m.getAttackResult().isSuccess()) {
                return "VULN";
            }
        }
        return "-";
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 3 && aValue instanceof String) {
            String s = (String) aValue;
            myMessages.get(rowIndex).setComment(s);
        }
        
        //System.out.println("Allmessage table SET (not supported)");
    }

    @Override
    public void update(Observable o, Object arg) {
        int selected = parent.getSelected();
        this.fireTableDataChanged();
        parent.setSelected(selected);
    }
}
