/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.mainTop.networking;

import gui.botLeft.PanelLeftUi;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class Networker {
    private static Networker myself;
    
    public static Networker getInstance() {
        if (myself == null) {
            myself = new Networker();
            myself.init();
        }
        return myself;
    }
    
    private final Queue<WorkEntry> queue = new LinkedBlockingQueue();
    private NetworkerWorker worker = null;
    
    public Networker() {
        
    }
    
    public void init() {
        worker = new NetworkerWorker(queue);
        worker.execute();
    }
    
    public int getQueueLen() {
        int a = 0;
        
        synchronized(queue) {
            a = queue.size();
        }
        
        
        BurpCallbacks.getInstance().print("Q: " + a);
        return a;
    }
    
    public String getLog() {
        return worker.getLog();
    }
    
    
    public void addNewMessages(
            LinkedList<SentinelHttpParam> attackHttpParams, 
            SentinelHttpMessage origHttpMessage, 
            PanelLeftUi panelParent, 
            boolean followRedirect, 
            String mainSessionName) 
    {
        
        synchronized(queue) {
            for(SentinelHttpParam attackHttpParam: attackHttpParams) {
                WorkEntry entry = new WorkEntry(
                        attackHttpParam,
                        origHttpMessage,
                        panelParent, 
                        followRedirect, 
                        mainSessionName);
                
                queue.add(entry);
            }
            
            BurpCallbacks.getInstance().print("QQ: " + queue.size());
            queue.notify();
        }
    }

    void cancelAll() {
        worker.cancelAll();
    }
    
}
