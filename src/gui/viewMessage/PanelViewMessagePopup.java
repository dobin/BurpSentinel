package gui.viewMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;

/**
 *
 * @author unreal
 */
public class PanelViewMessagePopup implements ActionListener {
    private JPopupMenu menu;
    
    private JMenuItem menuRepeater;
    private JMenuItem menuReissue;
    private JMenuItem menuCopy;
//    private JMenuItem menuDiff;
    
    private PanelViewMessageUi panelMessage;
    
    public PanelViewMessagePopup(PanelViewMessageUi panelMessage) {
        this.panelMessage = panelMessage;
        
        menu = new JPopupMenu("Message");
        
//        RecordableTextAction a = RSyntaxTextArea.getAction(RTextArea.COPY_ACTION);
//        a.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
//        a.setEnabled(true);
//        RSyntaxTextArea.setA
//          BurpCallbacks.getInstance().print(a.getAccelerator().toString());
        
        menuCopy = new JMenuItem(RSyntaxTextArea.getAction(RTextArea.COPY_ACTION));
        menu.add(menuCopy);
        
        menuRepeater = new JMenuItem("Send to Repeater");
        menuRepeater.addActionListener(this);
        menu.add(menuRepeater);
        
        menuReissue = new JMenuItem("Send again");
        menuReissue.addActionListener(this);
        menu.add(menuReissue);

//        menuDiff = new JMenuItem("Diff");
//        menuDiff.addActionListener(this);
//        menu.add(menuDiff);
        
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
 //       if (o == menuDiff) {
 //           panelMessage.c_diff();
 //       }
    }
    
}
