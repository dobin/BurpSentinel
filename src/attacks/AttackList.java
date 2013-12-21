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
import gui.viewMessage.ResponseHighlight;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
    public AttackList(SentinelHttpMessageOrig origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam, String data) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam, data);
    }
    
    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data);
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);

        String response = httpMessage.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
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
        
        if (initialMessage == null || initialMessage.getRequest() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "performNextAttack: no initialmessage");
        }
        if (initialMessage.getReq().getChangeParam() == null) {
            //BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }
        
        ListManagerList list = ListManager.getInstance().getModel().getList(Integer.parseInt(attackData));
        String data = list.getContent().get(state);
        
        // Replace placeholder with our XSS Identifier
        data = data.replace("XSS", XssIndicator.getInstance().getIndicator());
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            state++;
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
