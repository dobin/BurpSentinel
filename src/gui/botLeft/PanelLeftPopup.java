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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import model.SentinelHttpParam;
import model.SentinelHttpParamVirt;
import util.BurpCallbacks;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class PanelLeftPopup implements ActionListener {
    private JPopupMenu menu;
    private JMenu attackSubmenu;
    private JMenu decodeSubmenu;
    private PanelLeftUi parent;
    
    private LinkedList<JMenuItem> items;
    

    public PanelLeftPopup(PanelLeftUi parent) {
        this.parent = parent;

        menu = new JPopupMenu("Message");
        
        attackSubmenu = new JMenu("Attack with");
        menu.add(attackSubmenu);
        
        decodeSubmenu = new JMenu("Decode with");
        initDecodeSubmenu();
        menu.add(decodeSubmenu);
        
        items = new LinkedList<JMenuItem>();
    }

    public JPopupMenu getPopup() {
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        // Test Attack
        int n = items.indexOf(o);
        if (n >= 0) {
            attack(n);
        }
        
        // Test decode
        if (o == decodeBase64) {
            decodeIt(SentinelHttpParamVirt.EncoderType.Base64);
        } else if (o == decodeHTML) {
            decodeIt(SentinelHttpParamVirt.EncoderType.HTML);
        } else if (o == decodeURL) {
            decodeIt(SentinelHttpParamVirt.EncoderType.URL);
        }
    }
    
    private void decodeIt(SentinelHttpParamVirt.EncoderType encoderType) {
        // get current param
        SentinelHttpParam httpParam = parent.getSelectedHttpParam();
        
        // Create new virt param
        SentinelHttpParamVirt virtParam = new SentinelHttpParamVirt(httpParam, encoderType);
        
        parent.getOrigHttpMessage().getReq().addParamVirt(virtParam);
        
        // TODO: remove all old selections
        parent.updateModel();
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

    void refreshAttackListIndex() {
        for(JMenuItem item: items) {
            item.removeActionListener(this);
        }
        items = new LinkedList<JMenuItem>();
        attackSubmenu.removeAll();
        
        JMenuItem title = new JMenuItem("Attack with:");
        title.setEnabled(false);
        attackSubmenu.add(title);
        
        for(ListManagerList list: ListManager.getInstance().getModel().getList()) {
            JMenuItem menuItem = new JMenuItem(list.getName());
            items.add(menuItem);
            attackSubmenu.add(menuItem);
            menuItem.addActionListener(this);
        }
        
    }

    
    private JMenuItem decodeURL;
    private JMenuItem decodeHTML;
    private JMenuItem decodeBase64;
    
    private void initDecodeSubmenu() {
        decodeBase64 = new JMenuItem("Decode Base64");
        decodeHTML = new JMenuItem("Decode HTML");
        decodeURL = new JMenuItem("Decode URL");
        
        decodeURL.addActionListener(this);
        decodeHTML.addActionListener(this);
        decodeBase64.addActionListener(this);
        
        decodeSubmenu.add(decodeURL);
        decodeSubmenu.add(decodeHTML);
        decodeSubmenu.add(decodeBase64);
    }
    
    
}
