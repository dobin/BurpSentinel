package gui.networking;

import attacks.AttackAuthorisation;
import attacks.AttackI;
import attacks.AttackMain;
import attacks.AttackOriginal;
import attacks.AttackPersistentXss;
import attacks.AttackSql;
import attacks.AttackXss;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import model.AttackTypeData;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;

/**
 *
 * @author dobin
 */
public class NetworkerWorker extends SwingWorker<String, WorkEntry> {

    private final Queue queue = new LinkedBlockingQueue();
    private NetworkerLogger log = new NetworkerLogger();
    private boolean cancel = false;

    public NetworkerWorker() {
    }

    @Override
    protected String doInBackground() {
        WorkEntry work = null;

        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NetworkerWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                log.append("New requests\n");
                work = (WorkEntry) queue.remove();
            }

            doWork(work);
        }
    }

    void addAttack(WorkEntry entry) {
        synchronized (queue) {
            queue.add(entry);
            queue.notify();
        }
    }

    @Override
    protected void process(List<WorkEntry> pairs) {
        for (WorkEntry work : pairs) {
            work.panelParent.addAttackMessage(work.result);
        }
    }

    private void performAttack(AttackI attack, WorkEntry work, SentinelHttpParam attackHttpParam) {
        SentinelHttpMessageAtk attackMessage = null;
        boolean goon = true;

        while (goon) {
            log.append(work.origHttpMessage.getReq().getUrl() + " (" + attackHttpParam.getName() + "=" + attackHttpParam.getValue() + ") ...");
            log.giveSignal(NetworkerLogger.Signal.SEND);
            goon = attack.performNextAttack();
            log.giveSignal(NetworkerLogger.Signal.RECV);
            log.append(" ok\n");
            
            attackMessage = attack.getLastAttackMessage();

            if (attackMessage != null) {
                work.result = attackMessage;
                publish(work);
            } else {
                //BurpCallbacks.getInstance().print("performAttack: attackMessage is null");
            }

            if (cancel) {
                goon = false;
            }
        }
    }

    public NetworkerLogger getLogger() {
        return log;
    }

    void cancelAll() {
        cancel = true;
        log.giveSignal(NetworkerLogger.Signal.CANCEL);
        log.append("\n\nCanceling, please wait... ");
    }

    private void doWork(WorkEntry work) {
        log.newWork();
        log.giveSignal(NetworkerLogger.Signal.START);

        for (SentinelHttpParam attackHttpParam : work.attackHttpParams) {
            doAttack(work, attackHttpParam);
            
            if (cancel) {
                log.append("ok\n");
                cancel = false;
                break;
            }
        }
        
        log.giveSignal(NetworkerLogger.Signal.FINISHED);
    }

    private void doAttack(WorkEntry work, SentinelHttpParam attackHttpParam) {
        SentinelHttpMessageOrig origHttpMessage = work.origHttpMessage;
        boolean followRedirect = work.followRedirect;
        String mainSessionName = work.mainSessionName;

        // Original
        AttackTypeData origAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.ORIGINAL);
        if (origAttackData != null && origAttackData.isActive()) {
            AttackI attack = new AttackOriginal(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work, attackHttpParam);
        }

        if (cancel) {
            return;
        }

        // XSS
        AttackTypeData xssAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.XSS);
        if (xssAttackData != null && xssAttackData.isActive()) {
            AttackI attack = new AttackXss(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work, attackHttpParam);
        }

        if (cancel) {
            return;
        }

        // pXSS
        AttackTypeData pxssAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.OTHER);
        if (pxssAttackData != null && pxssAttackData.isActive()) {
            AttackI attack = new AttackPersistentXss(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work, attackHttpParam);
        }

        if (cancel) {
            return;
        }

        // SQL
        AttackTypeData sqlAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.SQL);
        if (sqlAttackData != null && sqlAttackData.isActive()) {
            AttackI attack = new AttackSql(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work, attackHttpParam);
        }

        if (cancel) {
            return;
        }


        // Authorisation
        AttackTypeData authAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.AUTHORISATION);
        if (authAttackData != null && authAttackData.isActive()) {
            AttackI attack = new AttackAuthorisation(origHttpMessage, mainSessionName, followRedirect, attackHttpParam, authAttackData.getData());
            //performAttack(attack, httpMessages);
            performAttack(attack, work, attackHttpParam);
        }

        attackHttpParam.resetAttackTypes();
    }
}
