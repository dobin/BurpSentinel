package attacks;

import gui.session.SessionManager;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
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
    protected SentinelHttpMessage initialMessage;
    protected SentinelHttpParam origParam;
    protected String attackData;
    protected String mainSessionName;
    protected boolean followRedirect = false;
    
    public AttackI(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        this.initialMessage = origHttpMessage;
        this.mainSessionName = mainSessionName;
        this.followRedirect = followRedirect;
        this.origParam = origParam;
    }
    
    public AttackI(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam, String data) {
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
    abstract public SentinelHttpMessage getLastAttackMessage();

    /*
     * 
     */
    protected SentinelHttpMessage initAttackHttpMessage(String attack) {
        // Copy httpmessage
        SentinelHttpMessage newHttpMessage = new SentinelHttpMessage(initialMessage);

        // BurpCallbacks.getInstance().print("Before: \n" + messageA.getReq().getRequestStr());

        // Set orig param
        newHttpMessage.getReq().setOrigParam(origParam);

        // Set change param
        SentinelHttpParam changeParam = new SentinelHttpParam(origParam);
        if (attack != null) {
            changeParam.changeValue(attack);
        }
        newHttpMessage.getReq().setChangeParam(changeParam);
        if (attack != null) {
            newHttpMessage.getReq().applyChangeParam();
        }
        
        // Set parent
        newHttpMessage.setParentHttpMessage(initialMessage);
        
        // Apply new session
        if (mainSessionName != null && !mainSessionName.equals("<default>")) {
            String sessionVarName = SessionManager.getInstance().getSessionVarName();
            String sessionVarValue = SessionManager.getInstance().getValueFor(mainSessionName);

            // Dont do it if we already modified the session parameter
            if (! sessionVarName.equals(changeParam.getName())) {
//                BurpCallbacks.getInstance().print("Change session: " + sessionVarName + " " + sessionVarValue);
                newHttpMessage.getReq().changeSession(sessionVarName, sessionVarValue);
            }
        }
        
        //BurpCallbacks.getInstance().print("\n\nAfter: \n" + newHttpMessage.getReq().getRequestStr());
        return newHttpMessage;
    }
    
}
