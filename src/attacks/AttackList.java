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

import attacks.model.AttackI;
import attacks.model.AttackResult;
import attacks.model.AttackData;
import gui.lists.ListManager;
import gui.lists.ListManagerList;
import gui.networking.AttackWorkEntry;
import java.awt.Color;
import model.ResponseHighlight;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class AttackList extends AttackI {
    private int state = 0;

    public AttackList(AttackWorkEntry work) {
        super(work);
    }
    
    @Override
    protected String getAtkName() {
        return "LIST";
    }
    
    @Override
    protected int getState() {
        return state;
    }

    @Override
    public boolean init() {
        return true;
    }


    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        SentinelHttpMessageAtk httpMessage;
        AttackData atkData;

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

        // Prepare attackData
        atkData = new AttackData(state, data, XssIndicator.getInstance().getIndicator(), AttackData.AttackResultType.INFO);
                
        try {
            httpMessage = attack(atkData);
            if (httpMessage == null) {
                return false;
            }
            analyzeResponse(httpMessage, atkData);
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
    
    
    private void analyzeResponse(SentinelHttpMessageAtk httpMessage, AttackData atkData) {
        String response = httpMessage.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            BurpCallbacks.getInstance().print("Response error");
            return;
        }
        
        // Check if input value gets reflected
        if (response.contains(atkData.getOutput())) {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.INFO,
                    "LST" + attackWorkEntry.options + "." + state,
                    httpMessage.getReq().getChangeParam(),
                    true,
                    "Found: " + atkData.getOutput(),
                    "");
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(atkData.getOutput(), Color.ORANGE);

            httpMessage.getRes().addHighlight(h);
        } else {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.NONE, 
                    "LST" + attackWorkEntry.options + "." + state, 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null,
                    "");
            httpMessage.addAttackResult(res);
        }
    }


}
