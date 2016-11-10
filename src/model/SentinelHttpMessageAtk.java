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
package model;

import attacks.model.AttackResult;
import java.io.Serializable;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class SentinelHttpMessageAtk extends SentinelHttpMessage implements Serializable {

    private SentinelHttpMessageOrig parentHttpMessage = null;
    private AttackResult attackResult;
    
    private String atkName;
    private int atkIndex;
    private String atkIdent;
    
     public SentinelHttpMessageAtk(SentinelHttpMessageOrig httpMessage, String atkName, int atkIndex, String atkIdent) {
         super(httpMessage);
         this.parentHttpMessage = httpMessage;
         
         this.atkName = atkName;
         this.atkIndex = atkIndex;
         this.atkIdent = atkIdent;
         
         parentHttpMessage.addChildren(this);
    }

    public enum ObserveResult {
        REQUEST,
        RESPONSE,
        ATTACKRESULT,
        CHILDREN
    };
    
    public String getAtkIdent() {
        return atkIdent;
    }
    
    public void addAttackResult(AttackResult res) {
        // Add result
        this.attackResult = res;
 
        // Fire update event
        this.setChanged();
        this.notifyObservers(ObserveResult.ATTACKRESULT);
        
        this.parentHttpMessage.notifyAttackResult();
    }

    public AttackResult getAttackResult() {
        return attackResult;
    }

    public SentinelHttpMessage getParentHttpMessage() {
        if (parentHttpMessage == null) {
            BurpCallbacks.getInstance().print("getParentHttpMessage: null");
        }
        return parentHttpMessage;
    }
 
}
