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
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class AttackSql extends AttackI {

    private int state = 0;
    private boolean attackAsuccess = false;
    private SentinelHttpMessage httpMessageA = null;
    private boolean attackBsuccess = false;
    private SentinelHttpMessage httpMessageB = null;
    
    
    private Color failColor = new Color(0xffcccc);
    

    public AttackSql(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }

    @Override
    public boolean performNextAttack() {
        if (initialMessage == null || initialMessage.getRequest() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "performNextAttack: no initialmessage");
        }

        if (initialMessage.getReq().getChangeParam() == null) {
            //Logger.getLogger(this.getClass().getName()).log(Level.INFO, "performNextAttack: getChangeParam = null");
        }
        
        switch (state) {
            case 0:
                state++;
                return attackA();
            case 1:
                state++;
                return attackB();
//            case 2:
//                state++;
//                return attackC();
//            case 3:
//                state++;
//                return attackD();
            default:
                return false;
        }
    }

    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        switch (state) {
            case 1:
                return httpMessageA;
            case 2:
                return httpMessageB;
//            case 3:
//                return httpMessageC;
//            case 3:
//                state++;
//                return attackD();
            default:
                return null;
        }
    }

    private boolean attackA() {
        httpMessageA = initAttackHttpMessage("%27");
        BurpCallbacks.getInstance().sendRessource(httpMessageA, followRedirect);

        String response = httpMessageA.getRes().getResponseStr();
        if (response.contains("SQL")) {

            // We found XSS - add attack result
            AttackResult res = new AttackResult("SQL0", "SUCCESS", httpMessageA.getReq().getChangeParam(), true);
            httpMessageA.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight("SQL", failColor);
            httpMessageA.addHighlight(h);

            attackAsuccess = true;

            // dont go on
            return false;
        } else {
            AttackResult res = new AttackResult("SQL0", "FAIL", httpMessageA.getReq().getChangeParam(), false);
            httpMessageA.addAttackResult(res);

            attackAsuccess = false;

            // go on
            return true;
        }
    }

    private boolean attackB() {
        httpMessageB = initAttackHttpMessage("'");
        BurpCallbacks.getInstance().sendRessource(httpMessageB, followRedirect);

        String response = httpMessageB.getRes().getResponseStr();
        if (response.contains("SQL")) {

            // We found XSS - add attack result
            AttackResult res = new AttackResult("SQL1", "SUCCESS", httpMessageB.getReq().getChangeParam(), true);
            httpMessageB.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight("SQL", failColor);
            httpMessageB.addHighlight(h);

            attackAsuccess = true;

            // dont go on
            return false;
        } else {
            AttackResult res = new AttackResult("SQL1", "FAIL", httpMessageA.getReq().getChangeParam(), false);
            httpMessageB.addAttackResult(res);

            attackBsuccess = false;

            // dont go on
            return false;
        }
    }


}
