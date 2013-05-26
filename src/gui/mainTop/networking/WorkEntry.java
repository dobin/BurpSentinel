/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.mainTop.networking;

import gui.botLeft.PanelLeftUi;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;

/**
 *
 * @author dobin
 */
public class WorkEntry {
    LinkedList<SentinelHttpParam> attackHttpParams;
    SentinelHttpMessageOrig origHttpMessage;
    PanelLeftUi panelParent;
    boolean followRedirect;
    String mainSessionName;
    
    SentinelHttpMessageAtk result;
    
    public WorkEntry(LinkedList<SentinelHttpParam> attackHttpParams, 
            SentinelHttpMessageOrig origHttpMessage, 
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
