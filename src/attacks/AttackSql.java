/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package attacks;

import gui.viewMessage.ResponseHighlight;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import model.XssIndicator;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackSql extends AttackI {

    private int state = 0;
    private SentinelHttpMessage lastHttpMessage = null;

    private Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
    private String[] attackDataSql = {
        "'",
        "''",
        "'''",
        "%27",
        "%27%27",
        "%27%27%27",
        "\"",
        "\"\"",
        "\"\"\"",
        "%22",
        "%22%22",
        "%22%22%22",
    };
    
     public AttackSql(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }

    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        if (initialMessage == null || initialMessage.getRequest() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "performNextAttack: no initialmessage");
        }
        if (initialMessage.getReq().getChangeParam() == null) {
            //BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }
        
        String data = attackDataSql[state];
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
        
        if (state < attackDataSql.length - 1) {
            doContinue = true;
        } else {
            doContinue = false;
        }
        
        state++;
        return doContinue;
    }

    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        return lastHttpMessage;
    }

    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessage httpMessage = initAttackHttpMessage(data);
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);

        String response = httpMessage.getRes().getResponseStr();
        if (response.contains("MySQL")) {
            AttackResult res = new AttackResult(AttackData.AttackType.VULN, "SQL" + state, httpMessage.getReq().getChangeParam(), true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight("MySQL", failColor);
            httpMessage.addHighlight(h);
        } else {
            AttackResult res = new AttackResult(AttackData.AttackType.NONE, "SQL" + state, httpMessage.getReq().getChangeParam(), false);
            httpMessage.addAttackResult(res);
        }
        
        return httpMessage;
    }

}
