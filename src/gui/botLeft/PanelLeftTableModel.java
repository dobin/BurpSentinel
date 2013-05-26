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
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class PanelLeftTableModel extends DefaultTableModel implements Observer {
    // The Request belonging to this table
    private SentinelHttpMessageOrig myMessage = null;
    
    // the table data itself 
    //   rows in a linked list
    //   list entry object data are columns
    private LinkedList<PanelLeftTableUIEntry> uiEntries = new LinkedList<PanelLeftTableUIEntry>();

    public PanelLeftTableModel() {
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 4 || column == 5 || column == 6 || column == 7) {
            return true;
        }
        if (column == 3 && isCookieRow(row)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getRowCount() {
        // For UI init
        if (myMessage == null) {
            return 0;
        }

        return myMessage.getReq().getParamCount();
    }

    @Override
    public int getColumnCount() {
        return 8;
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
            case 9:
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
                return "SQL";
            case 6:
                return "Other";
            case 7:
                return "All";

            case 8:
                return "Go";

            case 9:
                return "Vulnerable";

            default:
                return "hmm";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (column) {
            case 4:
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.XSS, (Boolean) value);
                //if ((Boolean)value == true) { 
                //    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                //}
                uiEntries.get(row).isXssEnabled = !uiEntries.get(row).isXssEnabled;
                if (uiEntries.get(row).isXssEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;
            case 5:
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.SQL, (Boolean) value);
                //if ((Boolean)value == true) { 
                //    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                //}
                uiEntries.get(row).isSqlEnabled = !uiEntries.get(row).isSqlEnabled;
                if (uiEntries.get(row).isSqlEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;
            case 6:
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.pXSS, (Boolean) value);
                //if ((Boolean)value == true) { 
                //    myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);
                //}
                uiEntries.get(row).isOtherEnabled = !uiEntries.get(row).isOtherEnabled;
                if (uiEntries.get(row).isOtherEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;

            case 7:
                // Check if attacks are set - if not, set em all!
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.XSS, (Boolean) value);
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.SQL, (Boolean) value);
                //myRequest.getReq().getParam(row).setAttackType(AttackMain.AttackTypes.pXSS, (Boolean) value);
                boolean b = !uiEntries.get(row).isAllEnabled;

                uiEntries.get(row).isXssEnabled = b;
                uiEntries.get(row).isSqlEnabled = b;
                uiEntries.get(row).isOtherEnabled = b;
                uiEntries.get(row).isAllEnabled = b;

                /*
                 SentinelHttpParam param = myRequest.getReq().getParam(row);
                 if (  ! param.getAttackType(AttackMain.AttackTypes.XSS).isActive()
                 || ! param.getAttackType(AttackMain.AttackTypes.pXSS).isActive()
                 || ! param.getAttackType(AttackMain.AttackTypes.SQL).isActive()
                 || ! param.getAttackType(AttackMain.AttackTypes.AUTHORISATION).isActive()) 
                 {
                 param.setAttackType(AttackMain.AttackTypes.ORIGINAL, true);
                 param.setAttackType(AttackMain.AttackTypes.XSS, true);
                 param.setAttackType(AttackMain.AttackTypes.SQL, true);
                 }
                
                 myRequest.getReq().getParam(row).setPerformAttack((Boolean)value);*/
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
                return myMessage.getReq().getParam(rowIndex).getTypeStr();
            case 2:
                return myMessage.getReq().getParam(rowIndex).getName();
            case 3:
                return myMessage.getReq().getParam(rowIndex).getValue();
            case 4:
                return uiEntries.get(rowIndex).isXssEnabled;
            //return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.XSS).isActive();
            case 5:
                return uiEntries.get(rowIndex).isSqlEnabled;
            //return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.pXSS).isActive();
            case 6:
                return uiEntries.get(rowIndex).isOtherEnabled;
            //return myRequest.getReq().getParam(rowIndex).getAttackType(AttackMain.AttackTypes.SQL).isActive();
            case 7:
                return uiEntries.get(rowIndex).isAllEnabled;
            //return myRequest.getReq().getParam(rowIndex).getPerformAttack();
            case 8:
                return "Go";
            case 9:
                //return myRequest.getReq().getParam(rowIndex).hasVulns();
                return hasVulns(myMessage.getReq().getParam(rowIndex));

            default:
                return "bbb";
        }
    }

    private String hasVulns(SentinelHttpParam param) {
        for (SentinelHttpMessageAtk m : myMessage.getHttpMessageChildren()) {
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

    void setMessage(SentinelHttpMessageOrig message) {
        try {
            //this.myRequest = new SentinelHttpMessage(message);
            this.myMessage = message;
            this.myMessage.addObserver(this);
        } catch (Exception ex) {
            BurpCallbacks.getInstance().print(ex.getLocalizedMessage());
        }

        int argc = myMessage.getReq().getParamCount();

        for (int n = 0; n < argc; n++) {
            PanelLeftTableUIEntry entry = new PanelLeftTableUIEntry();
            entry.isOrigEnabled = true; // Active orig attack
            
            uiEntries.add(entry);
        }

        this.fireTableDataChanged();
    }


    
    public LinkedList<SentinelHttpParam> createChangeParam() {
        LinkedList<SentinelHttpParam> list = new LinkedList<SentinelHttpParam>();

        // Check all params of httpmessage if they should be attacked
        // This has been set by the UI
        for (int n = 0; n < myMessage.getReq().getParamCount(); n++) {
            boolean attackThis = false;
            SentinelHttpParam param = myMessage.getReq().getParam(n);
            
           
            if (uiEntries.get(n).isXssEnabled) {
                BurpCallbacks.getInstance().print("Attack XSS because of param #" + n);
                myMessage.getReq().getParam(n).setAttackType(AttackMain.AttackTypes.XSS, (Boolean) true);
                attackThis = true;
            }
            if (uiEntries.get(n).isSqlEnabled) {
                BurpCallbacks.getInstance().print("Attack SQL because of param #" + n);
                myMessage.getReq().getParam(n).setAttackType(AttackMain.AttackTypes.SQL, (Boolean) true);
                attackThis = true;
            }
            if (uiEntries.get(n).isOtherEnabled) {
                BurpCallbacks.getInstance().print("Attack OTHER because of param #" + n);
                myMessage.getReq().getParam(n).setAttackType(AttackMain.AttackTypes.OTHER, (Boolean) true);
                attackThis = true;
            }
            if (uiEntries.get(n).isAuthEnabled) {
                myMessage.getReq().getParam(n).setAttackType(
                    AttackMain.AttackTypes.AUTHORISATION, 
                    true, 
                    uiEntries.get(n).authData);

                attackThis = true;
            }
            
            if (uiEntries.get(n).isOrigEnabled && attackThis) {
                BurpCallbacks.getInstance().print("Attack FIRST because of param #" + n);
                myMessage.getReq().getParam(n).setAttackType(AttackMain.AttackTypes.ORIGINAL, (Boolean) true);
                attackThis = true;
                
                // TODO bad place here - do it it resetAttackSelection()
                uiEntries.get(n).isOrigEnabled = false;
            }

            // Check if we should attack this specific param
            if (attackThis) {
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
        for (int n = 0; n < myMessage.getReq().getParamCount(); n++) {
            uiEntries.get(n).isXssEnabled = false;
            uiEntries.get(n).isSqlEnabled = false;
            uiEntries.get(n).isOtherEnabled = false;
            uiEntries.get(n).isAllEnabled = false;
            
            uiEntries.get(n).isAuthEnabled = false;
            uiEntries.get(n).authData = null;
        }
    }

    // Check if a specific row (param) is the session id
    boolean isCookieRow(int row) {
        SentinelHttpParam param = myMessage.getReq().getParam(row);

        if (param.getType() == IParameter.PARAM_COOKIE
                && param.getName().equals(SessionManager.getInstance().getSessionVarName())) {
            return true;
        } else {
            return false;
        }
    }

    // Called if we want to change cookie with a specific session
    void setSessionAttackMessage(boolean enabled, String selected) {

        for (int n = 0; n < myMessage.getReq().getParamCount(); n++) {
            SentinelHttpParam param = myMessage.getReq().getParam(n);

            if (param.getType() == IParameter.PARAM_COOKIE
                    && param.getName().equals(SessionManager.getInstance().getSessionVarName())) {

                uiEntries.get(n).isAuthEnabled = enabled;
                uiEntries.get(n).authData = selected;

            }
        }
    }
}
