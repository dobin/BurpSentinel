package gui.viewMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RecordableTextAction;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class MessagePopup implements ActionListener {
    private JPopupMenu menu;
    
    private JMenuItem menuRepeater;
    private JMenuItem menuReissue;
    private JMenuItem menuCopy;
    
    private PanelViewMessageUi panelMessage;
    
    public MessagePopup(PanelViewMessageUi panelMessage) {
        this.panelMessage = panelMessage;
        
        menu = new JPopupMenu("Message");
        
//        RecordableTextAction a = RSyntaxTextArea.getAction(RTextArea.COPY_ACTION);
//        a.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
//        a.setEnabled(true);
//        RSyntaxTextArea.setA
//          BurpCallbacks.getInstance().print(a.getAccelerator().toString());
        
        menuCopy = new JMenuItem(RSyntaxTextArea.getAction(RTextArea.COPY_ACTION));
        menu.add(menuCopy);
        
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
