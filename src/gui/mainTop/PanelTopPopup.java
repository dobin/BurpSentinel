/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.mainTop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class PanelTopPopup implements ActionListener {

    private JMenuItem menuDelete;
    private JPopupMenu menu;
    private PanelTopUi parent;

    public PanelTopPopup(PanelTopUi parent) {
        this.parent = parent;

        menu = new JPopupMenu("Message");

        menuDelete = new JMenuItem("Delete");
        menuDelete.addActionListener(this);
        
        menu.add(menuDelete);
    }

    public JPopupMenu getPopup() {
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        BurpCallbacks.getInstance().print("JOOO");
        
        
        if (o == menuDelete) {
            parent.removeMessage();
        }
    }
}
