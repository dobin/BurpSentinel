/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package attacks;

import gui.session.SessionManager;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class AttackAuthorisation extends AttackI {
    private boolean attackAsuccess = false;
    private SentinelHttpMessage httpMessageA;

    public AttackAuthorisation(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam, String data) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam, data);
    }

    @Override
    public boolean performNextAttack() {
        if (initialMessage == null || initialMessage.getRequest() == null) {
            BurpCallbacks.getInstance().print("AttackAuthorisation: initialmessage broken");
            return false;
        }

        return attackA();
    }


    private boolean attackA() {
        if (attackData == null) {
            BurpCallbacks.getInstance().print("initHttpMessage: no selectedSessionUser");
            return false;
        }

        String sessionId = SessionManager.getInstance().getValueFor(attackData);

        httpMessageA = initAttackHttpMessage(sessionId);
        BurpCallbacks.getInstance().sendRessource(httpMessageA, followRedirect);

        return false;
    }

    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        return httpMessageA;
    }
}
