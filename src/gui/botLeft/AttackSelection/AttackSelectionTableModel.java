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

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author dobin
 */
public class AttackSelectionTableModel extends DefaultTableModel {
    
    
    @Override
    public int getRowCount() {
        return 2;
    }
    
    @Override
    public int getColumnCount() {
        return 2;
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        return "Test";
    }
}
