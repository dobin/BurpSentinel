/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replayer.gui.ReplayerMain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author unreal
 */
public class ReplayerMainTablePopup implements ActionListener {
    private JPopupMenu popupMenu;
    
    private JMenuItem menuItemOrig;
    
    private ReplayerMainUi parent;
    
    public ReplayerMainTablePopup(ReplayerMainUi parent) {
        this.parent = parent;
        popupMenu = new JPopupMenu();
        
        menuItemOrig = new JMenuItem("Set as original");
        menuItemOrig.addActionListener(this);
        
        popupMenu.add(menuItemOrig);
    }
    
    public JPopupMenu getPopup() {
        return popupMenu;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();        
        if (o == menuItemOrig) {
            parent.setSelectedMessageAsOriginal();
        }
    }
}
