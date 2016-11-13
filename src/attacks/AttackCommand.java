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

import attacks.model.AttackI;
import attacks.model.AttackResult;
import attacks.model.AttackData;
import gui.botLeft.PanelLeftInsertions;
import gui.networking.AttackWorkEntry;
import java.util.LinkedList;
import model.SentinelHttpMessageAtk;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;
import util.Utility;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class AttackCommand extends AttackI {
    private boolean doContinue = false;
    private int state = 0;
    private LinkedList<AttackData> attackData = new LinkedList<AttackData>();;

    
    public AttackCommand(AttackWorkEntry work) {
        super(work);
        
        attackData.add(new AttackData(0, "() { :;}; sleep 10", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(1, ";sleep 10", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(2, "\";sleep 10", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(3, "';sleep 10", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(4, "|sleep 10", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(5, "& ping -c 10 127.0.0.1", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(6, "\" & ping -c 10 127.0.0.1", "", AttackData.AttackResultType.VULNUNSURE));
        attackData.add(new AttackData(7,  "' & ping -c 10 127.0.0.1", "", AttackData.AttackResultType.VULNUNSURE));
    }
    
    @Override
    protected String getAtkName() {
        return "CMD";
    }
    
    @Override
    protected int getState() {
        return state;
    }
    
    // return attack string
    // Also sets "doContinue"
    // Oh my god this is ugly
    private AttackData getData(AttackWorkEntry attackWorkEntry) {
        AttackData atkData;

        boolean onlyUrlencoded;
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            onlyUrlencoded = true;
        } else {
            onlyUrlencoded = false;
        }

        if (onlyUrlencoded) {
            atkData = attackData.get(state);
            //data = Utility.realUrlEncode(data);
                
            if (state < attackData.size() - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackData.size()) {
                atkData = attackData.get(state);
                //data = Utility.realUrlEncode(data);
            
                doContinue = true;
            } else {
                int newState = state - attackData.size();
                            
                atkData = attackData.get(newState);
                
                if (newState < attackData.size() - 1) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }
            }
        }
        
        return atkData;
    }

    
    @Override
    public boolean performNextAttack() {
        AttackData atkData;
        SentinelHttpMessageAtk httpMessage;
        
        attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.RIGHT;
        
        atkData = getData(attackWorkEntry);
        
        try {
            httpMessage = attack(atkData);
            if (httpMessage == null) {
                return false;
            }
            analyzeResponse(httpMessage);
        } catch (ConnectionTimeoutException ex) {
            BurpCallbacks.getInstance().print("Connection timeout");
            state++;
            return false;
        }
        
        state++;
        return doContinue;    
    }
    
    private void analyzeResponse(SentinelHttpMessageAtk httpMessage) {
        
        if (httpMessage.getLoadTime() > 10000 && httpMessage.getLoadTime() < 13000) {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.VULNUNSURE, 
                "CMD" + state, 
                httpMessage.getReq().getChangeParam(), 
                false,
                "Possible command injection",
                "Response took approximatly 10 seconds, as the sleep time.");
            httpMessage.addAttackResult(res);
        }
    }


    @Override
    public boolean init() {
        return true;
    }
    
}
