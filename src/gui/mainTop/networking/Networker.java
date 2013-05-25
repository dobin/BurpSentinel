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
    private NetworkerWorker worker = null;

    public Networker() {
    }

    public NetworkerWorker getWorker() {
        return worker;
    }

    public void init() {
        worker = new NetworkerWorker();
        //worker = new NetworkerWorker(queue);
        worker.execute();
    }

    public NetworkerLogger getLogger() {
        return worker.getLogger();
    }

    public void addNewMessages(
            LinkedList<SentinelHttpParam> attackHttpParams,
            SentinelHttpMessage origHttpMessage,
            PanelLeftUi panelParent,
            boolean followRedirect,
            String mainSessionName) {

        WorkEntry entry = new WorkEntry(
                attackHttpParams,
                origHttpMessage,
                panelParent,
                followRedirect,
                mainSessionName);

        worker.addAttack(entry);
    }

    void cancelAll() {
        worker.cancelAll();
    }
}
