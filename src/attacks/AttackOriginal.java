package attacks;

import attacks.AttackData.AttackType;
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
public class AttackOriginal extends AttackI {

    private SentinelHttpMessage message;
    
    public AttackOriginal(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }
    
    @Override
    public boolean performNextAttack() {
        try {
            if (initialMessage == null || initialMessage.getRequest() == null) {
                BurpCallbacks.getInstance().print("performNextAttack: no initialmessage");
            }

            SentinelHttpMessage httpMessage = initAttackHttpMessage(null);
            BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);
            this.message = httpMessage;
            
            AttackResult res = new AttackResult(
                    AttackType.INFO,
                    "ORIG",
                    httpMessage.getReq().getChangeParam(),
                    false);
            httpMessage.addAttackResult(res);

        } catch (ConnectionTimeoutException ex) {
            Logger.getLogger(AttackOriginal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        return message;
    }
    
}
