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

import gui.botLeft.PanelLeftUi;
import java.util.LinkedList;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class Networker {
    private static Networker myself = null;
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
        BurpCallbacks.getInstance().print("ATTACK! PPRE");
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
