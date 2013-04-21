/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.session.categorizer;

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
