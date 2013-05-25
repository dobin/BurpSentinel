package gui.mainTop.networking;

import attacks.AttackAuthorisation;
import attacks.AttackI;
import attacks.AttackMain;
import attacks.AttackOriginal;
import attacks.AttackPersistentXss;
import attacks.AttackSql;
import attacks.AttackXss;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import model.AttackTypeData;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class NetworkerWorker extends SwingWorker<String, WorkEntry> {

    private final Queue queue;
    private String log;

    public NetworkerWorker(Queue queue) {
        this.queue = queue;
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

                work = (WorkEntry) queue.remove();
            }
            
            BurpCallbacks.getInstance().print("QQQ: " + queue.size());
            doWork(work);

        }
    }

    @Override
    protected void process(List<WorkEntry> pairs) {
        for(WorkEntry work: pairs) {
            work.panelParent.addAttackMessage(work.result);
        }
    }
    
    private void performAttack(AttackI attack, WorkEntry work) {
        SentinelHttpMessage attackMessage = null;
        boolean goon = true;

        while (goon) {
            log += work.origHttpMessage.getReq().getUrl() + " - " + work.attackHttpParam.getName() + "\n";
            goon = attack.performNextAttack();
            attackMessage = attack.getLastAttackMessage();
            
            if (attackMessage != null) {
                work.result = attackMessage;
                publish(work);
            } else {
                //BurpCallbacks.getInstance().print("performAttack: attackMessage is null");
            }

            //if (panelProgress.isCanceled()) {
            //    goon = false;
            //}
        }
    }
    
    public String getLog() {
        return log;
    }
    
    void cancelAll() {
         synchronized (queue) {
             queue.clear();
         }
    }

    private void doWork(WorkEntry work) {
        log = "Startingz";
        SentinelHttpParam attackHttpParam = work.attackHttpParam;
        SentinelHttpMessage origHttpMessage = work.origHttpMessage;
        boolean followRedirect = work.followRedirect;
        String mainSessionName = work.mainSessionName;

        // Original
        AttackTypeData origAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.ORIGINAL);
        if (origAttackData != null && origAttackData.isActive()) {
            AttackI attack = new AttackOriginal(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work);
        }

        // XSS
        AttackTypeData xssAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.XSS);
        if (xssAttackData != null && xssAttackData.isActive()) {
            AttackI attack = new AttackXss(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work);
        }

        // pXSS
        AttackTypeData pxssAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.OTHER);
        if (pxssAttackData != null && pxssAttackData.isActive()) {
            AttackI attack = new AttackPersistentXss(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work);
        }

        // SQL
        AttackTypeData sqlAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.SQL);
        if (sqlAttackData != null && sqlAttackData.isActive()) {
            AttackI attack = new AttackSql(origHttpMessage, mainSessionName, followRedirect, attackHttpParam);
            //performAttack(attack, httpMessages);
            performAttack(attack, work);
        }

        // Authorisation
        AttackTypeData authAttackData = attackHttpParam.getAttackType(AttackMain.AttackTypes.AUTHORISATION);
        if (authAttackData != null && authAttackData.isActive()) {
            AttackI attack = new AttackAuthorisation(origHttpMessage, mainSessionName, followRedirect, attackHttpParam, authAttackData.getData());
            //performAttack(attack, httpMessages);
            performAttack(attack, work);
        }

        attackHttpParam.resetAttackTypes();
    }

}
