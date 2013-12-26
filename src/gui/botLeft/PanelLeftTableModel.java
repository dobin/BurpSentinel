/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui.botLeft;

import attacks.AttackMain;
import burp.IParameter;
import gui.session.SessionManager;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.DefaultTableModel;
import model.SentinelHttpParam;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParamVirt;
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
        // For checkboxes
        if (column == 3 || column == 4 || column == 5) {
            return true;
        }
        
        if (column == 2 && isCookieRow(row)) {
            return true;
        } 
        
        return false;
    }
    

    @Override
    public int getRowCount() {
        // For UI init
        if (myMessage == null) {
            return 0;
        }

        return uiEntries.size();
    }

    
    @Override
    public int getColumnCount() {
        return 6;
    }
    

    @Override
    public Class getColumnClass(int columnIndex) {

        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;

            case 3: // XSS
                return Boolean.class;
            case 4: // SQL
                return Boolean.class;
            case 5: // Other
                return Boolean.class;

            default:
                return String.class;
        }
    }
    

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Type";
            case 1:
                return "Name";
            case 2:
                return "Value";

            case 3:
                return "XSS";
            case 4:
                return "SQL";
            case 5:
                return "Other";

            default:
                return "hmm";
        }
    }
    

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (column) {
            case 3:
                uiEntries.get(row).isXssEnabled = !uiEntries.get(row).isXssEnabled;
                if (uiEntries.get(row).isXssEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;
            case 4:
                uiEntries.get(row).isSqlEnabled = !uiEntries.get(row).isSqlEnabled;
                if (uiEntries.get(row).isSqlEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;
            case 5:
                uiEntries.get(row).isOtherEnabled = !uiEntries.get(row).isOtherEnabled;
                if (uiEntries.get(row).isOtherEnabled == false) {
                    uiEntries.get(row).isAllEnabled = false;
                }
                break;
        }

        this.fireTableDataChanged();
    }

    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return uiEntries.get(rowIndex).sourceHttpParam.getTypeStr();
            case 1:
                return uiEntries.get(rowIndex).sourceHttpParam.getName();
            case 2:
                return uiEntries.get(rowIndex).sourceHttpParam.getDecodedValue();
            case 3:
                return uiEntries.get(rowIndex).isXssEnabled;
            case 4:
                return uiEntries.get(rowIndex).isSqlEnabled;
            case 5:
                return uiEntries.get(rowIndex).isOtherEnabled;
            default:
                return "";
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
            this.myMessage = message;
            this.myMessage.addObserver(this);
        } catch (Exception ex) {
            BurpCallbacks.getInstance().print(ex.getLocalizedMessage());
        }

        reinit();
    }
    
    void reinit() {
        uiEntries.clear();
        

        for(SentinelHttpParamVirt httpParam: myMessage.getReq().getParamsVirt()) {
            PanelLeftTableUIEntry entry = new PanelLeftTableUIEntry();
            //entry.isOrigEnabled = true; // Active orig attack
            entry.sourceHttpParam = httpParam;
            uiEntries.add(entry);
        }
        
        for(SentinelHttpParam httpParam: myMessage.getReq().getParams()) {
            PanelLeftTableUIEntry entry = new PanelLeftTableUIEntry();
            //entry.isOrigEnabled = true; // Active orig attack
            entry.sourceHttpParam = httpParam;
            uiEntries.add(entry);
        }

        this.fireTableDataChanged();
    }

    
    public void createChangeParam(PanelLeftUi parent) {
        // Check all params of httpmessage if they should be attacked
        // This has been set by the UI
        
        for(PanelLeftTableUIEntry entry: uiEntries) {
            SentinelHttpParam param = entry.sourceHttpParam;

            if (entry.isXssEnabled) {
                parent.attackSelectedParam(param, AttackMain.AttackTypes.XSS, null);
            }
            
            if (entry.isSqlEnabled) {
                parent.attackSelectedParam(param, AttackMain.AttackTypes.SQL, null);
            }
            
            if (entry.isOtherEnabled) {
                parent.attackSelectedParam(param, AttackMain.AttackTypes.OTHER, null);
            }
            
            if (entry.isAuthEnabled) {
                parent.attackSelectedParam(param, AttackMain.AttackTypes.AUTHORISATION, entry.authData);
            }
            /*
            if (entry.isOrigEnabled && attackThis) {
                param.setAttackType(AttackMain.AttackTypes.ORIGINAL, (Boolean) true);
                attackThis = true;
                
                // TODO bad place here - do it it resetAttackSelection()
                entry.isOrigEnabled = false;
            }*/
        }
    }

    
    @Override
    public void update(Observable o, Object arg) {
        this.fireTableDataChanged();
    }
    

    void resetAttackSelection() {
        for(PanelLeftTableUIEntry entry: uiEntries) {
            entry.isAllEnabled = false;
            entry.isSqlEnabled = false;
            entry.isOtherEnabled = false;
            entry.isAllEnabled = false;

            entry.isAuthEnabled = false;
            entry.authData = null;
        }
        this.fireTableDataChanged();
    }

    
    // Check if a specific row (param) is the session id
    boolean isCookieRow(int row) {
        SentinelHttpParam param = uiEntries.get(row).sourceHttpParam;

        if (param.getType() == IParameter.PARAM_COOKIE
                && param.getName().equals(SessionManager.getInstance().getSessionVarName())) {
            return true;
        } else {
            return false;
        }
    }

    // Called if we want to change cookie with a specific session
    void setSessionAttackMessage(boolean enabled, String selected) {
        for(PanelLeftTableUIEntry entry: uiEntries) {
            SentinelHttpParam param = entry.sourceHttpParam;

            if (param.getType() == IParameter.PARAM_COOKIE
                    && param.getName().equals(SessionManager.getInstance().getSessionVarName())) 
            {
                entry.isAuthEnabled = enabled;
                entry.authData = selected;

            }
        }
    }

    SentinelHttpParam getHttpParamAt(int selectedRow) {
        return uiEntries.get(selectedRow).sourceHttpParam;
    }


}
