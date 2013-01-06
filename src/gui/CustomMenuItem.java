package gui;

import burp.IHttpRequestResponse;
import burp.IMenuItemHandler;
import burp.MainUiInterface;
import javax.swing.SwingUtilities;

/**
 * Creates the menu entry for burp to send requests to sentinel
 *
 * @author Dobin
 */
public class CustomMenuItem implements IMenuItemHandler {

    // Link to parent MainUi to add messages
    private MainUiInterface mainGui;

    public CustomMenuItem(MainUiInterface mainGui) {
        this.mainGui = mainGui;
    }

    @Override
    public void menuItemClicked(String menuItemCaption, final IHttpRequestResponse[] messageInfo) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < messageInfo.length; i++) {
                    mainGui.addNewMessage(messageInfo[i]);
                }
            }
        });
    }
}
