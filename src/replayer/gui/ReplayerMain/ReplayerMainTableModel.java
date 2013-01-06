package replayer.gui.ReplayerMain;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.AbstractTableModel;
import model.SentinelHttpMessage;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class ReplayerMainTableModel extends AbstractTableModel implements Observer {

    private LinkedList<SentinelHttpMessage> httpMessages;
    
    public ReplayerMainTableModel() {
        httpMessages = new LinkedList<SentinelHttpMessage>();
    }
    
    public void addHttpMessage(SentinelHttpMessage httpMessage) {
        httpMessages.add(httpMessage);
        this.fireTableDataChanged();
    }
            
    
    @Override
    public int getRowCount() {
        //BurpCallbacks.getInstance().print("CNT: " + httpMessages.size());
        return httpMessages.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SentinelHttpMessage httpMessage = httpMessages.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                String s = "";
                SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss dd.MM");
                s = ft.format(httpMessage.getCreateTime());
                
                if (s == null) {
                    s = "ASDF";
                }
                BurpCallbacks.getInstance().print("S: " + s);
                return s;
            default:
                return "";
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    SentinelHttpMessage getHttpMessage(int row) {
        return httpMessages.get(row);
    }

    int getMessageCount() {
        return httpMessages.size();
    }

    SentinelHttpMessage getMessage(int index) {
        return httpMessages.get(index);
    }
    
}
