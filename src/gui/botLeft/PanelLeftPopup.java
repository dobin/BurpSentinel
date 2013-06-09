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

import attacks.AttackMain;
import gui.lists.ListManager;
import gui.lists.ListManagerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import model.SentinelHttpParam;
import util.BurpCallbacks;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class PanelLeftPopup implements ActionListener {
    private JPopupMenu menu;
    private PanelLeftUi parent;
    
    private LinkedList<JMenuItem> items;
    

    public PanelLeftPopup(PanelLeftUi parent) {
        this.parent = parent;

        menu = new JPopupMenu("Message");
        items = new LinkedList<JMenuItem>();
    }

    public JPopupMenu getPopup() {
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        int n = items.indexOf(o);
        if (n >= 0) {
            attack(n);
        }
    }
    
    private void attack(int n) {
        // get current param
        SentinelHttpParam httpParam = parent.getSelectedHttpParam();
        
        // add attack to param
        httpParam.setAttackType(AttackMain.AttackTypes.LIST, true, Integer.toString(n));
        
        // attack it
        LinkedList<SentinelHttpParam> attackParams = new LinkedList<SentinelHttpParam>();
        attackParams.add(httpParam);
        
        parent.attackSelectedParam(attackParams);
    }

    void refreshIndex() {
        for(JMenuItem item: items) {
            item.removeActionListener(this);
        }
        items = new LinkedList<JMenuItem>();
        menu.removeAll();
        
        for(ListManagerList list: ListManager.getInstance().getModel().getList()) {
            JMenuItem menuItem = new JMenuItem(list.getName());
            items.add(menuItem);
            menu.add(menuItem);
            menuItem.addActionListener(this);
        }
        
    }
    
    
}
