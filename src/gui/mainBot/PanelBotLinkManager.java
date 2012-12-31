package gui.mainBot;

import gui.viewMessage.PanelViewMessageUi;
import java.util.LinkedList;

/**
 *
 * @author unreal
 */
public class PanelBotLinkManager {

    private LinkedList<PanelViewMessageUi> messageUiList = new LinkedList<PanelViewMessageUi>();
    
    public void registerViewMessage(PanelViewMessageUi viewMessageUi) {
        messageUiList.add(viewMessageUi);
    }
    
    public void setPosition(int n, PanelViewMessageUi orig) {
        for (PanelViewMessageUi messageUi: messageUiList) {
            if (messageUi != orig) {
                messageUi.setPosition(n);
            }
        }
    }
    
}
