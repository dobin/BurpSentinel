package gui.mainTop.networking;

import gui.botLeft.PanelLeftUi;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;

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
    
    public NetworkerWorker getWorker() {
        return worker;
    }

    public void init() {
        worker = new NetworkerWorker();
        worker.execute();
    }

    public NetworkerLogger getLogger() {
        return worker.getLogger();
    }

    public void addNewMessages(
            LinkedList<SentinelHttpParam> attackHttpParams,
            SentinelHttpMessageOrig origHttpMessage,
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
