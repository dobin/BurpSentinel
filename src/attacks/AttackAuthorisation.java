/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package attacks;

import gui.session.SessionManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

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
        try {
            BurpCallbacks.getInstance().sendRessource(httpMessageA, followRedirect);
        } catch (ConnectionTimeoutException ex) {
            Logger.getLogger(AttackAuthorisation.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        return httpMessageA;
    }
}
