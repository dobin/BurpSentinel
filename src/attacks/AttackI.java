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

import gui.session.SessionManager;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
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
    protected SentinelHttpMessageOrig initialMessage;
    protected SentinelHttpParam origParam;
    protected String attackData;
    protected String mainSessionName;
    protected boolean followRedirect = false;
    
    public AttackI(SentinelHttpMessageOrig origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        this.initialMessage = origHttpMessage;
        this.mainSessionName = mainSessionName;
        this.followRedirect = followRedirect;
        this.origParam = origParam;
    }
    
    public AttackI(SentinelHttpMessageOrig origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam, String data) {
        this.initialMessage = origHttpMessage;
        this.mainSessionName = mainSessionName;
        this.followRedirect = followRedirect;
        this.origParam = origParam;
        this.attackData = data;
    }
    
    
    /* Will execute the next (or initial) attack
     * Returns true if more attacks are necessary/available
     */
    abstract public boolean performNextAttack();
    
    /* Get the last http message sent by performNextAttack()
     */
    abstract public SentinelHttpMessageAtk getLastAttackMessage();

    /*
     * 
     */
    protected SentinelHttpMessageAtk initAttackHttpMessage(String attack) {
        // Copy httpmessage
        SentinelHttpMessageAtk newHttpMessage = new SentinelHttpMessageAtk(initialMessage);

        // Set orig param
        newHttpMessage.getReq().setOrigParam(origParam);
    
        // Set change param
        SentinelHttpParam changeParam = null;
        if (origParam instanceof SentinelHttpParamVirt) {
            changeParam = new SentinelHttpParamVirt( (SentinelHttpParamVirt) origParam);
        } else if (origParam instanceof SentinelHttpParam) {
            changeParam = new SentinelHttpParam(origParam);
        }        
        
        if (attack != null) {
            changeParam.changeValue(attack);
        } else {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: changeValue: attack is null");
        }
        newHttpMessage.getReq().setChangeParam(changeParam);
        if (attack != null) {
            newHttpMessage.getReq().applyChangeParam();
        } else {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: ApplyChange: attack is null");
        }
        
        // Set parent
        newHttpMessage.setParentHttpMessage(initialMessage);
        
        // Apply new session
        if (mainSessionName != null) {
            if (! mainSessionName.equals("<default>") && ! mainSessionName.startsWith("<")) {
                String sessionVarName = SessionManager.getInstance().getSessionVarName();
                String sessionVarValue = SessionManager.getInstance().getValueFor(mainSessionName);

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
