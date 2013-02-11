/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replayer.gui.ReplayerMain;

import gui.mainTop.PanelTopUi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class ReplayerMainPopup implements ActionListener {

    private JMenuItem menuDelete;
    private JPopupMenu menu;
    private ReplayerMainUi parent;

    public ReplayerMainPopup(ReplayerMainUi parent) {
        this.parent = parent;

        menu = new JPopupMenu("Message");

        menuDelete = new JMenuItem("Set as Original");
        menuDelete.addActionListener(this);
        
        menu.add(menuDelete);
    }

    public JPopupMenu getPopup() {
        return menu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == menuDelete) {
            parent.setSelectedMessageAsOriginal();
        }
    }
    
}
