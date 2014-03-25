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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class NetworkerWorker extends SwingWorker<String, AttackWorkEntry> {

    //private final LinkedBlockingQueue queue = new LinkedBlockingQueue();
    private LinkedList queue = new LinkedList();
    private NetworkerSender networkerSender;
    private boolean isCanceled = false;
    
    public NetworkerWorker() {
        networkerSender = new NetworkerSender();
        
        isCanceled = false;
    }

    @Override
    protected String doInBackground() {
        AttackWorkEntry work = null;
        boolean goon;

        while (true) {
            synchronized (queue) {
                if (isCanceled) {
                    BurpCallbacks.getInstance().print("[A.1] doInBackground Canceling in while - clear queue");
                    isCanceled = false;
                    if (queue.size() > 0) {
                        queue.clear();
                    }
                    //this.isCancelled();
                }
                
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NetworkerWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                work = (AttackWorkEntry) queue.remove();
                
                /*
                try {
                    work = (AttackWorkEntry) queue.take();
                } catch (InterruptedException ex) {
                    BurpCallbacks.getInstance().print(ex.getLocalizedMessage());
                }*/

                BurpCallbacks.getInstance().print("[A.1] doInBackground Got new work! Doing it");
            }

            if (networkerSender.init(work) == true) {
                BurpCallbacks.getInstance().print("[A.2] doInBackground Init ok");

                goon = true;
                while(goon) {
                    goon = networkerSender.sendRequest();
                    BurpCallbacks.getInstance().print("[A.3] doInBackground Publishing");
                    
                    
                    if (isCanceled) {
                        getLogger().giveSignal(NetworkerLogger.Signal.CANCEL);
                        getLogger().append("\n\nCanceling ok");
                        BurpCallbacks.getInstance().print("[B.4] doInBackground Canceled... ! (inloop)");
                        goon = false;
                    } else {
                        publish(networkerSender.getResult());
                    }
                }
                
                getLogger().giveSignal(NetworkerLogger.Signal.FINISHED);
            }
        }
    }
    
    @Override
    public void done() {
        BurpCallbacks.getInstance().print("[A.5] Done!");
    }

    void addAttack(AttackWorkEntry entry) {
        synchronized (queue) {
            /*
            try {
                queue.put(entry);
            } catch (InterruptedException ex) {
                BurpCallbacks.getInstance().print(ex.getLocalizedMessage());
            }*/
            queue.add(entry);
            queue.notify();
        }
    }

    @Override
    protected void process(List<AttackWorkEntry> pairs) {
        BurpCallbacks.getInstance().print("[A.4] process Handle publish");
        for (AttackWorkEntry work : pairs) {
            work.panelParent.addAttackMessage(work.result);
        }
    }


    public NetworkerLogger getLogger() {
        return networkerSender.getLog();
    }

    void cancelAll() {
        BurpCallbacks.getInstance().print("[B.6] cancelAll Canceling!");
        getLogger().giveSignal(NetworkerLogger.Signal.CANCEL);
        getLogger().append("\n\nCanceling, please wait... ");
        
        isCanceled = true;

    }

}
