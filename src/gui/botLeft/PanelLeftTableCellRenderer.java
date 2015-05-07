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

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import util.BurpCallbacks;

/**
 * Individual cell rednderer for panel left param table.
 * 
 * To add cookie dropdown box.
 * 
 * @author unreal
 */
public class PanelLeftTableCellRenderer extends DefaultTableCellRenderer {

    private JComboBox myComboBox;

    public PanelLeftTableCellRenderer(JComboBox myComboBox) {
        super();
        this.myComboBox = myComboBox;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // Check if we should set tooltip
        if (column == 1 || column == 2) {
            TableModel model = table.getModel();
        
            // Those columns (1, 2) are always string
            String s = (String) model.getValueAt(row, column);
            //s = BurpCallbacks.getInstance().getBurp().getHelpers().urlDecode(s);
            setToolTipText(s);
            //BurpCallbacks.getInstance().print("Tooltip: " + s + " Column: " + column);
        }
        
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);        
            return c;
/*        
        // FIXME Removed
        // Cookie selector
        if (column != 2) {
            return this;
        }
        //if (!(table.getModel() instanceof PanelLeftTableModel)) {
        //    System.out.println("NO: ");
        //}
        PanelLeftTableModel m = (PanelLeftTableModel) table.getModel();
        if (m.isCookieRow(row)) {
            return myComboBox;
        } else {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            return c;
        }*/
    }
}
