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

import gui.networking.AttackWorkEntry;
import gui.session.SessionManager;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpParam;
import model.SentinelHttpParamVirt;
import util.BurpCallbacks;

/**
 * Interface for all attack classes
 * 
 * initialMessage: the message we want to attack
 * origParam:      the param we want to attack
 * attackData:     additional data from the user for this attack
 * 
 * @author Dobin
 */
public abstract class AttackI {
    protected AttackWorkEntry attackWorkEntry;
    
    public AttackI(AttackWorkEntry work) {
        this.attackWorkEntry = work;
    }

    /* Will execute the next (or initial) attack
     * Returns true if more attacks are necessary/available
     */
    abstract public boolean performNextAttack();
    
    /* Get the last http message sent by performNextAttack()
     */
    abstract public SentinelHttpMessageAtk getLastAttackMessage();

    abstract public boolean init();
    
    /*
     * 
     */
    protected SentinelHttpMessageAtk initAttackHttpMessage(String attackVectorString) {
        // Copy httpmessage
        SentinelHttpMessageAtk newHttpMessage = new SentinelHttpMessageAtk(attackWorkEntry.origHttpMessage);

        // Set orig param
        newHttpMessage.getReq().setOrigParam(attackWorkEntry.attackHttpParam);
    
        // Set change param
        SentinelHttpParam changeParam = null;
        if (attackWorkEntry.attackHttpParam instanceof SentinelHttpParamVirt) {
            changeParam = new SentinelHttpParamVirt( (SentinelHttpParamVirt) attackWorkEntry.attackHttpParam);
        } else if (attackWorkEntry.attackHttpParam instanceof SentinelHttpParam) {
            changeParam = new SentinelHttpParam(attackWorkEntry.attackHttpParam);
        }        
        
        if (attackVectorString != null) {
            changeParam.changeValue(attackVectorString);
        } else {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: changeValue: attack is null");
        }
        newHttpMessage.getReq().setChangeParam(changeParam);
        if (attackVectorString != null) {
            newHttpMessage.getReq().applyChangeParam();
        } else {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: ApplyChange: attack is null");
        }
        
        // Set parent
        newHttpMessage.setParentHttpMessage(attackWorkEntry.origHttpMessage);
        
        // Apply new session
        if (attackWorkEntry.mainSessionName != null) {
            if (! attackWorkEntry.mainSessionName.equals("<default>") && ! attackWorkEntry.mainSessionName.startsWith("<")) {
                String sessionVarName = SessionManager.getInstance().getSessionVarName();
                String sessionVarValue = SessionManager.getInstance().getValueFor(attackWorkEntry.mainSessionName);

                // Dont do it if we already modified the session parameter
                if (!sessionVarName.equals(changeParam.getName())) {
//                BurpCallbacks.getInstance().print("Change session: " + sessionVarName + " " + sessionVarValue);
                    newHttpMessage.getReq().changeSession(sessionVarName, sessionVarValue);
                }
            }
        }
        
        //BurpCallbacks.getInstance().print("\n\nAfter: \n" + newHttpMessage.getReq().getRequestStr());
        return newHttpMessage;
    }
    
}
