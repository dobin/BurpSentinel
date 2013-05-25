/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.mainTop.networking;

import gui.botLeft.PanelLeftUi;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;

/**
 *
 * @author dobin
 */
public class WorkEntry {
    LinkedList<SentinelHttpParam> attackHttpParams;
    SentinelHttpMessage origHttpMessage;
    PanelLeftUi panelParent;
    boolean followRedirect;
    String mainSessionName;
    
    SentinelHttpMessage result;
    
    public WorkEntry(LinkedList<SentinelHttpParam> attackHttpParams, 
            SentinelHttpMessage origHttpMessage, 
            PanelLeftUi panelParent, 
            boolean followRedirect, 
            String mainSessionName) {
     
        this.attackHttpParams = attackHttpParams;
        this.origHttpMessage = origHttpMessage;
        this.panelParent = panelParent;
        this.followRedirect = followRedirect;
        this.mainSessionName = mainSessionName;
    }

}
