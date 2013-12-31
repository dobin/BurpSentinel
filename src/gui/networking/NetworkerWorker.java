/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gui.networking;

import attacks.AttackAuthorisation;
import attacks.AttackI;
import attacks.AttackList;
import attacks.AttackMain;
import attacks.AttackOriginal;
import attacks.AttackPersistentXss;
import attacks.AttackSql;
import attacks.AttackXss;
import attacks.AttackXssLessThan;
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
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class NetworkerWorker extends SwingWorker<String, AttackWorkEntry> {

    private final Queue queue = new LinkedBlockingQueue();
    private NetworkerLogger log = new NetworkerLogger();
    private boolean cancel = false;

    public NetworkerWorker() {
    }

    @Override
    protected String doInBackground() {
        AttackWorkEntry work = null;

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
                work = (AttackWorkEntry) queue.remove();
            }

            doWork(work);
        }
    }

    void addAttack(AttackWorkEntry entry) {
        synchronized (queue) {
            queue.add(entry);
            queue.notify();
        }
    }

    @Override
    protected void process(List<AttackWorkEntry> pairs) {
        for (AttackWorkEntry work : pairs) {
            work.panelParent.addAttackMessage(work.result);
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

    private void doWork(AttackWorkEntry work) {
        log.newWork();
        log.giveSignal(NetworkerLogger.Signal.START);

        AttackMain.AttackTypes attackType = work.attackType;
        AttackI attack = null;
        
        // Some basic integrity checks
        if (work.origHttpMessage == null || work.origHttpMessage.getRequest() == null) {
            BurpCallbacks.getInstance().print("initialmessage broken");
            return;
        }
        
        BurpCallbacks.getInstance().print("doAttack: " + work.attackType);
        
        switch (attackType) {
            case ORIGINAL:
                attack = new AttackOriginal(work);
                break;
            case XSS:
                attack = new AttackXss(work);
                break;
            case OTHER:
                attack = new AttackPersistentXss(work);
                break;
            case SQL:
                attack = new AttackSql(work);
                break;
            case AUTHORISATION:
                attack = new AttackAuthorisation(work);
                break;
            case LIST:
                attack = new AttackList(work);
                break;
            case XSSLESSTHAN:
                attack = new AttackXssLessThan(work);
                break;
            default:
                BurpCallbacks.getInstance().print("Error, unknown attack type: " + attackType);
                return;
        }
        
        if (attack.init() == true) {
            performAttack(attack, work);
        }
        
        log.giveSignal(NetworkerLogger.Signal.FINISHED);
    }


    private void performAttack(AttackI attack, AttackWorkEntry work) {
        SentinelHttpMessageAtk attackMessage = null;
        boolean goon = true;

        while (goon) {
            log.append(work.origHttpMessage.getReq().getUrl() + " (" + work.attackHttpParam.getName() + "=" + work.attackHttpParam.getValue() + ") ...");
            log.giveSignal(NetworkerLogger.Signal.SEND);
            goon = attack.performNextAttack();
            log.giveSignal(NetworkerLogger.Signal.RECV);
            log.append(" ok\n");
            
            attackMessage = attack.getLastAttackMessage();

            if (attackMessage != null) {
                work.result = attackMessage;
                publish(work);
            } else {
                BurpCallbacks.getInstance().print("performAttack: attackMessage is null");
            }

            if (cancel) {
                goon = false;
            }
        }
    }
}
