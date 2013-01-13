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

/**
 *
 * @author unreal
 */
public class AttackSql extends AttackI {

    private int state = 0;
    private SentinelHttpMessage lastHttpMessage = null;

    private Color failColor = new Color(0xffcccc);
    
    private String[] attackDataSql = {
        "'",
        "''",
        "%27",
        "%27%27"
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
            BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }
        
        String data = attackDataSql[state];
        SentinelHttpMessage httpMessage = attack(data);
        
        if (state < 4) {
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

    private SentinelHttpMessage attack(String data) {
        SentinelHttpMessage httpMessage = initAttackHttpMessage(data);
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);

        String response = httpMessage.getRes().getResponseStr();
        if (response.contains("SQL")) {

            // We found XSS - add attack result
            AttackResult res = new AttackResult("SQL0", "VLUN", httpMessage.getReq().getChangeParam(), true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight("SQL", failColor);
            httpMessage.addHighlight(h);

        } else {
            AttackResult res = new AttackResult("SQL0", "-", httpMessage.getReq().getChangeParam(), false);
            httpMessage.addAttackResult(res);
        }
        
        return httpMessage;
    }

}
