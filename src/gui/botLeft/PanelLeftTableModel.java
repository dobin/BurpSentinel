package gui.botLeft;

import attacks.AttackMain;
import burp.IParameter;
import gui.session.SessionManager;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JCheckBox;
import javax.swing.table.DefaultTableModel;
import model.SentinelHttpParam;
import model.SentinelHttpMessage;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class PanelLeftTableModel extends DefaultTableModel implements Observer {
    private SentinelHttpMessage myRequest = null;
    private JCheckBox[] checkBoxAttack;

    public PanelLeftTableModel() {

    }

    @Override
    public int getRowCount() {
        // For UI init
        if (myRequest == null) {
            return 0;
        }

        return myRequest.getReq().getParamCount();
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
                
            case 4: // XSS
                return Boolean.class;
            case 5: // pXSS
                return Boolean.class;
            case 6: // SQL
                return Boolean.class;
            case 7: // Attack
                return Boolean.class;
                
            case 8:
                return String.class;
                
            default:
                return String.class;
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
                return "Value";
                
            case 4:
                return "XSS";
            case 5:
                return "eXSS";
            case 6:
                return "SQL";
                
            case 7:
                return "Atk";
            case 8:
                return "Vulnerable";
                
            default:
                return "hmm";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch(column) {
            case 4:
                myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.XSS, (Boolean) value);
                if ((Boolean)value == true) { 
                    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                }
                break;
            case 5:
                myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.pXSS, (Boolean) value);
                if ((Boolean)value == true) { 
                    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                }
                break;
            case 6:
                myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.SQL, (Boolean) value);
                if ((Boolean)value == true) { 
                    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                }
                break;
            case 7:
                // Check if attacks are set - if not, set em all!
                SentinelHttpParam param = myRequest.getReq().getParam(row);
                if (param.getAttackType(AttackMain.AttackTypes.XSS).isActive()
                   || param.getAttackType(AttackMain.AttackTypes.pXSS).isActive()
                   || param.getAttackType(AttackMain.AttackTypes.SQL).isActive()
                   || param.getAttackType(AttackMain.AttackTypes.AUTHORISATION).isActive()) 
                {
                    param.setAttackType(AttackMain.AttackTypes.XSS, true);
                    param.setAttackType(AttackMain.AttackTypes.SQL, true);
                }
                
                myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                break;
        }
        
        this.fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return myRequest.getReq().getParam(rowIndex).getTypeStr();
            case 2:
                return myRequest.getReq().getParam(rowIndex).getName();
            case 3:
                return myRequest.getReq().getParam(rowIndex).getValue();
            case 4:
                return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.XSS).isActive();
            case 5:
                return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.pXSS).isActive();
            case 6:
                return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.SQL).isActive();
            case 7:
                return myRequest.getReq().getParam(rowIndex).getPerformAttack();
            case 8:
                return hasVulns(myRequest.getReq().getParam(rowIndex));
                
            default:
                return "bbb";
        }
    }
    
    private String hasVulns(SentinelHttpParam param) {
        for(SentinelHttpMessage m: myRequest.getHttpMessageChildren()) {
            if (m.getAttackResult() == null) {
                continue;
            }
            
            String childrenName = m.getAttackResult().getAttackParam().getName();
            String thisName = param.getName();
            
            if (m.getAttackResult().isSuccess() && childrenName.equals(thisName)) {
                return "VULN";
            }
        }
        
        return "-";
    }

    void setMessage(SentinelHttpMessage message) {
        try {
            //this.myRequest = new SentinelHttpMessage(message);
            this.myRequest = message;
            this.myRequest.addObserver(this);
        } catch (Exception ex) {
            BurpCallbacks.getInstance().print(ex.getLocalizedMessage());
        }

        int argc = myRequest.getReq().getParamCount();
        checkBoxAttack = new JCheckBox[argc];
        for (int n = 0; n < argc; n++) {
            checkBoxAttack[n] = new JCheckBox("", myRequest.getReq().getParam(n).getPerformAttack());
        }

        this.fireTableDataChanged();
    }

    /*
    void userClick(int selectedRowIndex, int selectedColumnIndex) {
        if (selectedColumnIndex == 4) {
            //boolean isSelected = ! checkBoxAttack[selectedRowIndex].isSelected();
            //checkBoxAttack[selectedRowIndex].setSelected(isSelected);
            //myRequest.getParam(selectedRowIndex).setAttack(isSelected);
        }
    }*/

    public LinkedList<SentinelHttpParam> createChangeParam() {
        LinkedList<SentinelHttpParam> list = new LinkedList<SentinelHttpParam>();
        
        // Check all params of httpmessage if they should be attacked
        // This has been set by the UI
        for(int n=0; n<myRequest.getReq().getParamCount(); n++) {
            SentinelHttpParam param = myRequest.getReq().getParam(n);
            
            // Check if we should attack this specific param
            if (param.getPerformAttack()) {
                list.add(param);
            }
        }
        
        return list;
    }

    @Override
    public void update(Observable o, Object arg) {
        this.fireTableDataChanged();
    }

    void resetAttackSelection() {
        for(int n=0; n < myRequest.getReq().getParamCount(); n++) {
            myRequest.getReq().getParam(n).setPerformAttack(false);
        }
    }

    
    // Check if a specific row (param) is the session id
    boolean isCookieRow(int row) {
       SentinelHttpParam param = myRequest.getReq().getParam(row);
        
       if (param.getType() == IParameter.PARAM_COOKIE  
                && param.getName().equals(SessionManager.getInstance().getSessionVarName())) {
            return true;
        } else {
            return false;
        }
    }

    // Called if we want to change cookie with a specific session
    void setSessionAttackMessage(int selectedRow, boolean enabled, String selected) {
        myRequest.getReq().getParam(selectedRow).setAttackType(
                AttackMain.AttackTypes.AUTHORISATION, enabled, selected);
    }
}
