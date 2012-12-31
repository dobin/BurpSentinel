/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.botLeft;

import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import util.BurpCallbacks;

/**
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
        if (column == 3) {
            TableModel model = table.getModel();
        
            // Those columns (2, 3) are always string
            String s = (String) model.getValueAt(row, column);
            try {
                //s = BurpCallbacks.getInstance().getBurp().getHelpers().urlDecode(s);
                s = URLDecoder.decode(s, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(PanelLeftTableCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
            setToolTipText(s);
        }
                
        if (column != 3) {
            return this;
        }

        if (!(table.getModel() instanceof PanelLeftTableModel)) {
            System.out.println("NO: ");
        }


        PanelLeftTableModel m = (PanelLeftTableModel) table.getModel();
        if (m.isCookieRow(row)) {
            return myComboBox;
        } else {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            return c;
        }
    }
}
