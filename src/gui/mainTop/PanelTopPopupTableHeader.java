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
package gui.mainTop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import util.BurpCallbacks;



/**
 * sauce: http://stackoverflow.com/questions/6793257/add-column-to-exiting-tablemodel/6796673#6796673
 * @author DobinRutishauser@broken.ch
 */
public class PanelTopPopupTableHeader extends JPopupMenu implements ActionListener {

    private JTable tableMessages;
    private TableColumnModel tableColumnModel;
    private Map<String, IndexedColumn> hidden = new HashMap<String, IndexedColumn>();

    public PanelTopPopupTableHeader(JTable tableMessages) {
        super();
        this.tableMessages = tableMessages;
        this.tableColumnModel = tableMessages.getColumnModel();

        JMenuItem menuItem;
        menuItem = new JCheckBoxMenuItem("#", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Method", true);
        menuItem.addActionListener(this);
        this.add(menuItem);
        
        menuItem = new JCheckBoxMenuItem("URL", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Comment", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Interesting", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Session", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Vulnerable", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Created", true);
        menuItem.addActionListener(this);
        this.add(menuItem);

        menuItem = new JCheckBoxMenuItem("Modified", true);
        menuItem.addActionListener(this);
        this.add(menuItem);
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem menuItem = (JMenuItem) e.getSource();
        String name = menuItem.getText();
        if (menuItem.isSelected()) {
            showColumn(name);
        } else {
            hideColumn(name);
        }


    }

    public void hideColumn(String columnName) {
        int index = tableColumnModel.getColumnIndex(columnName);
        TableColumn column = tableColumnModel.getColumn(index);

        IndexedColumn ic = new IndexedColumn(index, column);
        if (hidden.put(columnName, ic) != null) {
            BurpCallbacks.getInstance().print("Duplicate column name.");
        }
        tableColumnModel.removeColumn(column);
        /*
         PanelRightModel rightModel = (PanelRightModel) tableMessages.getModel();
         rightModel.fireTableStructureChanged();
         */
    }

    public void showColumn(String columnName) {
        IndexedColumn ic = hidden.remove(columnName);
        if (ic != null) {
            tableColumnModel.addColumn(ic.column);
            int lastColumn = tableColumnModel.getColumnCount() - 1;
            if (ic.index < lastColumn) {
                tableColumnModel.moveColumn(lastColumn, ic.index);
            }
        }
        /*
         PanelRightModel rightModel = (PanelRightModel) tableMessages.getModel();
         rightModel.fireTableStructureChanged();
         */
    }

    private static class IndexedColumn {

        private Integer index;
        private TableColumn column;

        public IndexedColumn(Integer index, TableColumn column) {
            this.index = index;
            this.column = column;
        }
    }
}
