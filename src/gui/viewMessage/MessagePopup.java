package gui.viewMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author unreal
 */
public class MessagePopup implements ActionListener {
    private JPopupMenu menu;
    private JMenuItem menuRepeater;
    private JMenuItem menuReissue;
    private PanelViewMessageUi panelMessage;
    
    public MessagePopup(PanelViewMessageUi panelMessage) {
        this.panelMessage = panelMessage;
        
        menu = new JPopupMenu("Message");
        
        menuRepeater = new JMenuItem("Send message to: burp reapeater");
        menuRepeater.addActionListener(this);
        menu.add(menuRepeater);
        
        menuReissue = new JMenuItem("Send message.request: again");
        menuReissue.addActionListener(this);
        menu.add(menuReissue);
        
        
    }
    
    public JPopupMenu getPopup() {
        return menu;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        
        if (o == menuRepeater) {
            panelMessage.c_sendToRepeater();
        }
        if (o == menuReissue) {
            panelMessage.c_sendAgain();
        }
    }
    
}
