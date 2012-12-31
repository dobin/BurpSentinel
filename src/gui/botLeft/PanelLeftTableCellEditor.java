/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.botLeft;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author unreal
 */
public class PanelLeftTableCellEditor extends DefaultCellEditor  {

    private JComboBox c;

    public PanelLeftTableCellEditor(JComboBox c) {
        super(c);
        this.c = c;
    }
    
    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        if (column != 3) {
            return null;
        }

        if (! (table.getModel() instanceof PanelLeftTableModel)) {
            System.out.println("NO: ");
        }
        
        PanelLeftTableModel m = (PanelLeftTableModel) table.getModel();
        if (m.isCookieRow(row)) {
            return c;
        } else {
            return null;
        }
    }
}
