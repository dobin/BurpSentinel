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
package attacks;

import gui.lists.ListManager;
import gui.lists.ListManagerList;
import gui.networking.AttackWorkEntry;
import gui.viewMessage.ResponseHighlight;
import java.awt.Color;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import model.XssIndicator;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class AttackList extends AttackI {

    private SentinelHttpMessageAtk lastHttpMessage = null;
    private int state = 0;
    
    public AttackList(AttackWorkEntry work) {
        super(work);
    }
    
            
    @Override
    public boolean init() {
        return true;
    }

 
    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data);
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);

        String response = httpMessage.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        // Check if input value gets reflected
        if (response.contains(data)) {
            AttackResult res = new AttackResult(
                    AttackData.AttackType.INFO,
                    "LIST",
                    httpMessage.getReq().getChangeParam(), 
                    true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data, Color.ORANGE);
            
            httpMessage.addHighlight(h);
        }
        
        return httpMessage;
    }
    
    @Override
    public boolean performNextAttack() {
       boolean doContinue = false;
        
        if (attackWorkEntry.origHttpMessage == null || attackWorkEntry.origHttpMessage.getRequest() == null) {
            BurpCallbacks.getInstance().print("performNextAttack: no initialmessage");
            return false;
        }
        if (attackWorkEntry.origHttpMessage.getReq().getChangeParam() == null) {
            //BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }
        
        ListManagerList list = ListManager.getInstance().getModel().getList(Integer.parseInt(attackWorkEntry.options));
        if (list == null) {
            BurpCallbacks.getInstance().print("Could not load List: " + attackWorkEntry.options + " State: " + state);
            return false;
        }
        
        String data = list.getContent().get(state);
        if (data == null || data.length() == 0) {
            BurpCallbacks.getInstance().print("List Data error! List: " + attackWorkEntry.options + " State: " + state);
            return false;
        }
        
        // Replace placeholder with our XSS Identifier
        data = data.replace("XSS", XssIndicator.getInstance().getIndicator());
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            BurpCallbacks.getInstance().print("Connection timeout: " + ex.getLocalizedMessage());
            return false;
        }
        
        if (state < list.getContent().size() - 1) {
            doContinue = true;
        } else {
            doContinue = false;
        }
        
        state++;
        return doContinue;
    }

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }
    
}
