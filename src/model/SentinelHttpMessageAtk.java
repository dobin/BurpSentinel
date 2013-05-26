package model;

import attacks.AttackResult;
import burp.IHttpRequestResponse;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class SentinelHttpMessageAtk extends SentinelHttpMessage {

    private SentinelHttpMessageOrig parentHttpMessage = null;
    private AttackResult attackResult;
    
     public SentinelHttpMessageAtk(SentinelHttpMessage httpMessage) {
         super(httpMessage);
    }

    public enum ObserveResult {
        REQUEST,
        RESPONSE,
        ATTACKRESULT,
        CHILDREN
    };
    
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

    
    // Parenting
    public void setParentHttpMessage(SentinelHttpMessageOrig httpMessage) {
        this.parentHttpMessage = httpMessage;
        
        parentHttpMessage.addChildren(this);
        
        this.setChanged();
        this.notifyObservers(ObserveResult.CHILDREN);
    }

    public SentinelHttpMessage getParentHttpMessage() {
        if (parentHttpMessage == null) {
            BurpCallbacks.getInstance().print("getParentHttpMessage: null");
        }
        return parentHttpMessage;
    }
 
}
