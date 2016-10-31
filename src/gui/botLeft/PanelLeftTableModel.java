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

import attacks.model.AttackDescription;
import attacks.model.AttackMain;
import burp.IParameter;
import gui.botLeft.AttackSelection.AttackSelectionTableModel;
import gui.session.SessionManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.table.DefaultTableModel;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import model.SentinelHttpParamVirt;
import util.BurpCallbacks;

/**
 * The table model for panel left.
 * 
 * Observes:
 *   HttpMessage for changes. Will update its content when change is observed.
 * 
 * @author unreal
 */
public class PanelLeftTableModel extends DefaultTableModel implements Observer {
    // The Request belonging to this table
    private SentinelHttpMessageOrig myMessage = null;
    
    // The attack selection table model
    // So we know which attacks the user want to perform
    private AttackSelectionTableModel attackSelectionTableModel;
    
    // the table data itself 
    //   rows in a linked list
    //   list entry object data are columns
    private final LinkedList<PanelLeftTableUIEntry> uiEntries = new LinkedList<PanelLeftTableUIEntry>();

    public PanelLeftTableModel(AttackSelectionTableModel attackSelectionTableModel) {
        this.attackSelectionTableModel = attackSelectionTableModel;
    }
    

    @Override
    public boolean isCellEditable(int row, int column) {
        // For checkboxes
        if (column == 3 || column == 4) {
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
        return 4;
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

            case 3: // Attack
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
                return "Attack";

            default:
                return "hmm";
        }
    }
    

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (column) {
            case 3:
                uiEntries.get(row).performAttack = !uiEntries.get(row).performAttack;
                break;
        }

        // Just a single cell updated
        this.fireTableCellUpdated(row, column);
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
                return uiEntries.get(rowIndex).performAttack;

            default:
                return "";
        }
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

        // complete table new
        this.fireTableDataChanged();
    }
    

    public void createChangeParam(PanelLeftUi parent) {
        // Check all params of httpmessage if they should be attacked
        // This has been set by AttackSelectionUi

        for (PanelLeftTableUIEntry entry : uiEntries) {
            SentinelHttpParam param = entry.sourceHttpParam;

            if (entry.performAttack) {
                List<AttackDescription> attackDescriptions = attackSelectionTableModel.getSelected();
                for(AttackDescription attack: attackDescriptions) {
                    parent.attackSelectedParam(param, attack.getAttackType(), null);
                }
            }

            if (entry.isAuthEnabled) {
                parent.attackSelectedParam(param, AttackMain.AttackTypes.AUTHORISATION, entry.authData);
            }
        }
    }
    

    public void intentInvertSelection(int column) {
        for (PanelLeftTableUIEntry entry : uiEntries) {
            // UI: Skip path
            if (entry.sourceHttpParam.getTypeStr().equals("PATH") ) {
                continue;
            }
            
            switch(column) {
                case 3:
                    entry.performAttack = ! entry.performAttack;
                    break;
            }
        }
        
        // Can affect several rows... just update all of them
        this.fireTableDataChanged();
    }
    
    
    public void intentSelectAll(int column) {
        for(PanelLeftTableUIEntry entry: uiEntries) {
            // UI: Skip path
            if (entry.sourceHttpParam.getTypeStr().equals("PATH") ) {
                continue;
            }
            
            switch(column) {
                case 3:
                    entry.performAttack = true;
                    break;
            }
        }

        // Can affect several rows... just update all of them
        this.fireTableDataChanged();
    }

    
    @Override
    public void update(Observable o, Object arg) {
        // We dont know what changed FIXME
        this.fireTableDataChanged();
    }
    

    void resetAttackSelection() {
        for(PanelLeftTableUIEntry entry: uiEntries) {
            entry.performAttack = false;
            
            entry.isAuthEnabled = false;
            entry.authData = null;
        }
        
        // Can affect several rows... just update all of them
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
