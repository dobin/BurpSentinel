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
package gui.categorizer;

import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;
import util.UiUtil;

/**
 *
 * @author dobin
 */
public class CategorizerTableModel extends AbstractTableModel {

    private LinkedList<CategoryEntry> categoryEntries = new LinkedList<CategoryEntry>();

    public CategorizerTableModel() {
        UiUtil.restoreCategories(categoryEntries);
    }
    
    @Override
    public int getRowCount() {
        return categoryEntries.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return categoryEntries.get(rowIndex).getTag();
            case 1:
                return categoryEntries.get(rowIndex).getRegex();
            default:
                return "";
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Tag";
            case 1:
                return "Regex";
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                categoryEntries.get(rowIndex).setTag((String) aValue);
                break;
            case 1:
                categoryEntries.get(rowIndex).setRegex((String) aValue);
                break;
            default:
                break;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    void storeUiPrefs() {
        UiUtil.storeCategories(categoryEntries);
    }

    void addEmptyLine() {
        categoryEntries.add( new CategoryEntry("<tag>", "<regex>"));
        this.fireTableDataChanged();
    }

    LinkedList<CategoryEntry> getCategories() {
        return categoryEntries;
    }
}
