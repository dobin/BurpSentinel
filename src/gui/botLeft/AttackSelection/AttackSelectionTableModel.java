/*
 * Copyright (C) 2016 dobin
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
package gui.botLeft.AttackSelection;

import attacks.model.AttackDescription;
import attacks.model.AttackMain;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import util.SettingsManager;

/**
 *
 * @author dobin
 */
public class AttackSelectionTableModel extends DefaultTableModel {
    
    List<AttackDescription> attackDescriptions;
    
    AttackSelectionTableModel() {
        attackDescriptions = new ArrayList<AttackDescription>();
        this.attackDescriptions = AttackMain.getInstance().getAttackDescriptions();
        SettingsManager.getAttackSelectionConfig(attackDescriptions);
    }
    
    
    public List<AttackDescription> getSelected() {
        List<AttackDescription> activeAttacks = new LinkedList<AttackDescription>();

        for(AttackDescription attack: attackDescriptions) {
            if (attack.isEnabled()) {
                activeAttacks.add(attack);
            }
        }
        
        return activeAttacks;
    }
    
    
    @Override
    public int getRowCount() {
        if (attackDescriptions == null) {
            return 0;
        } else {
            return attackDescriptions.size();
        }    
    }
    
    
    @Override
    public int getColumnCount() {
        return 3;
    }
    
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Name";
            case 1:
                return "Description";
            case 2:
                return "Active";
                
            default:
                return "hmm";
        }
    }
    
    
    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2: 
                return Boolean.class;

            default:
                return String.class;
        }
    }

    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return attackDescriptions.get(rowIndex).getShort();
            case 1:
                return attackDescriptions.get(rowIndex).getDescription();
            case 2:
                return attackDescriptions.get(rowIndex).isEnabled();
            default: return "";
        }
    }
    
    
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == 2) {
            attackDescriptions.get(row).setEnabled( ! attackDescriptions.get(row).isEnabled());
            
            SettingsManager.storeAttackSelectionConfig(attackDescriptions);
        }
    }
}
