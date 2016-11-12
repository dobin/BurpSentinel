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

package attacks.model;

import burp.IResponseVariations;
import gui.networking.AttackWorkEntry;
import gui.session.SessionManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpParam;
import model.SentinelHttpParamVirt;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 * Interface for all attack classes
 * 
 * AttackWorkEntry should have all data we need to send an attack request:
 *   - original http message
 *   - param to change
 *   - which attack to perform
 *   - additional options
 * 
 * init() is called first.
 * Then, performNextAttack() will be called as long as it returns true.
 * getLastAttackMessage() should return the last sent HTTP Message by the attack
 * class (for logging/monitoring purposes). 
 * initAttackHttpMessage() with the attack vector string should preferably be
 * called to create the attack http message.
 * 
 * @author Dobin
 */
public abstract class AttackI {
    protected AttackWorkEntry attackWorkEntry;
    private SentinelHttpMessageAtk lastHttpMessage;
    
    abstract protected String getAtkName();
    abstract protected int getState();
    
    public AttackI(AttackWorkEntry work) {
        this.attackWorkEntry = work;
    }

    /* Will execute the next (or initial) attack
     * Returns true if more attacks are necessary/available
     * Called by the networksender thread
     */
    abstract public boolean performNextAttack();
    
    /* 
     * Get the last http message sent by performNextAttack()
     */
    //abstract public SentinelHttpMessageAtk getLastAttackMessage();

    /*
     * Called before performNextAttack()
     */
    abstract public boolean init();
    
    /*
     * Init a http message for an attack
     * This involves:
     *   - create a new httpmessage
     *   - add attack vector as changeparam
     *   - set parent
     * 
     */
    protected SentinelHttpMessageAtk initAttackHttpMessage(String attackVectorString, String atkName, int state) {
        if (attackWorkEntry == null) {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: work entry is null");
            return null;
        }
        if (attackVectorString == null) {
             BurpCallbacks.getInstance().print("initAttackHttpMessage: attackVectorString error");
             return null;
        }
        
        // Copy httpmessage
        SentinelHttpMessageAtk newHttpMessage = new SentinelHttpMessageAtk(
            attackWorkEntry.origHttpMessage,
            atkName,
            state,
            atkName + Integer.toString(state));

        // Set orig param
        newHttpMessage.getReq().setOrigParam(attackWorkEntry.attackHttpParam);
    
        // Set change param (by copying original param)
        SentinelHttpParam changeParam = null;
        if (attackWorkEntry.attackHttpParam instanceof SentinelHttpParamVirt) {
            changeParam = new SentinelHttpParamVirt( (SentinelHttpParamVirt) attackWorkEntry.attackHttpParam);
        } else if (attackWorkEntry.attackHttpParam instanceof SentinelHttpParam) {
            changeParam = new SentinelHttpParam(attackWorkEntry.attackHttpParam);
        } else {
             BurpCallbacks.getInstance().print("initAttackHttpMessage: changeValue: error");
             return null;
        }
        switch (attackWorkEntry.insertPosition) {
            case LEFT:
                changeParam.changeValue(attackVectorString + changeParam.getValue());
                break;
            case RIGHT:
                changeParam.changeValue(changeParam.getValue() + attackVectorString);
                break;
            case REPLACE:
                changeParam.changeValue(attackVectorString);
                break;
            default:
                return null;
        }

        // Apply changeparam
        newHttpMessage.getReq().setChangeParam(changeParam);
        boolean ret = newHttpMessage.getReq().applyChangeParam();
        if (ret == false) {
            BurpCallbacks.getInstance().print("initAttackHttpMessage: problem applying change param");
            return null;
        }
        
        // Apply new session
        if (attackWorkEntry.mainSessionName != null) {
            if (! attackWorkEntry.mainSessionName.equals("<default>") && ! attackWorkEntry.mainSessionName.startsWith("<")) {
                String sessionVarName = SessionManager.getInstance().getSessionVarName();
                String sessionVarValue = SessionManager.getInstance().getValueFor(attackWorkEntry.mainSessionName);

                // Dont do it if we already modified the session parameter
                if (!sessionVarName.equals(changeParam.getName())) {
                    newHttpMessage.getReq().changeSession(sessionVarName, sessionVarValue);
                }
            }
        }

        return newHttpMessage;
    }
    
    
    protected SentinelHttpMessageAtk attack(AttackData data) throws ConnectionTimeoutException {
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            data.urlEncode();
        }
        
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data.getInput(), getAtkName(), getState());
        if (httpMessage == null) {
            BurpCallbacks.getInstance().print("attack: Httpmessage is null");
            return null;
        }
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        if (! httpMessage.getRes().hasResponse()) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        return httpMessage;
    }
    
    
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }
    
    
    protected void analyzeOriginalRequest(SentinelHttpMessageAtk origAtkMessage) {
        int sizeDiff = 0;
        int origResponseSize;
        int newResponseSize;

        // Check if response sizes are identical
        origResponseSize = attackWorkEntry.origHttpMessage.getRes().getSize();
        newResponseSize = origAtkMessage.getRes().getSize();
        sizeDiff = origResponseSize - newResponseSize;

        if (sizeDiff == 0) {
            if (attackWorkEntry.origHttpMessage.getRes().getResponseStrBody().equals(origAtkMessage.getRes().getResponseStrBody())) {
                // They are 100% identical - that's how it should be!
                
                // origMsgComparer.isIdentical = true;
                AttackResult res = new AttackResult(
                    AttackData.AttackResultType.STATUSGOOD,
                    "ORIG",
                    origAtkMessage.getReq().getChangeParam(),
                    true,
                    "Request is optimal",
                    "The request behaves the same every time is is sent (it generates identical output). Ideal conditions.");
                origAtkMessage.addAttackResult(res);
                return;
            }
        }

        // OK messages are not identical
        // But maybe very similar?
        byte[][] a = new byte[2][];
        a[0] = attackWorkEntry.origHttpMessage.getResponse();
        a[1] = origAtkMessage.getResponse();
               
        IResponseVariations responseVariant = BurpCallbacks.getInstance().getBurp().getHelpers().analyzeResponseVariations(a);
        List<String> variantList = responseVariant.getVariantAttributes();
        List<String> invariantList = responseVariant.getInvariantAttributes();
        
        if (invariantList.contains("tag_ids")) {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.STATUSGOOD,
                "ORIG",
                origAtkMessage.getReq().getChangeParam(),
                true,
                "Request is ok.",
                "The request behaves similarly every time it is sent (it has not identical responses, but identical tags in the response). Good conditions.");
            origAtkMessage.addAttackResult(res);
            
            return;
        }
        
        // Very different
        AttackResult res = new AttackResult(
            AttackData.AttackResultType.STATUSBAD,
            "ORIG",
            origAtkMessage.getReq().getChangeParam(),
            true,
            "Request is bad.",
            "The request behaves differently every time it is sent (the reponses differ vastly). Bad conditions."); // Too much difference
        origAtkMessage.addAttackResult(res);
    }
    
    
}
