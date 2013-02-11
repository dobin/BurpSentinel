/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replayer.gui.ReplayerMain;

import burp.IHttpService;
import burp.IMessageEditorController;
import model.SentinelHttpMessage;

/**
 *
 * @author dobin
 */
public class MainMessageEditorController implements IMessageEditorController {

    private SentinelHttpMessage message;
    
    public MainMessageEditorController(SentinelHttpMessage message) {
        this.message = message;
    }
    
    @Override
    public IHttpService getHttpService() {
        return message.getHttpService();
    }

    @Override
    public byte[] getRequest() {
        return message.getRequest();
    }

    @Override
    public byte[] getResponse() {
        return message.getResponse();
    }

    void setHttpMessage(SentinelHttpMessage m) {
        this.message = m;
        this.notifyAll();
    }
    
}
