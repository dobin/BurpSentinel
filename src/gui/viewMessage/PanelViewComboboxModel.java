/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.viewMessage;

import java.util.LinkedList;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author unreal
 */
public class PanelViewComboboxModel extends AbstractListModel implements ComboBoxModel {

    private boolean hasParent = false;
    
    private LinkedList<String> values = new LinkedList<String>();
    
    public PanelViewComboboxModel() {
        values.add("Default");
        values.add("Beautify");
        values.add("Diff");
    }
    
    public void hasParent(boolean hasParent) {
        this.hasParent = hasParent;
    }
    
    
    @Override
    public int getSize() {
        if (hasParent) {
            return 3;
        } else {
            return 2;
        }
    }

    @Override
    public Object getElementAt(int index) {
        if (index < values.size()) {
            return values.get(index);
        } else {
            return "";
        }
    }

    private int selected = 0;
    
    @Override
    public void setSelectedItem(Object anItem) {
        for(int n=0; n<values.size(); n++) {
            if (values.get(n).equals(anItem)) {
                selected = n;
            }
        }
    }

    @Override
    public Object getSelectedItem() {
        return values.get(selected);
    }
    
}
