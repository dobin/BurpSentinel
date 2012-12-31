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
public class AttackXss extends AttackI {
    private int state = 0;

    private boolean attackAsuccess = false;
    private SentinelHttpMessage httpMessageA = null;
    private boolean messageAisInTag = false;
    
    private boolean attackBsuccess = false;
    private SentinelHttpMessage httpMessageB = null;
    private boolean attackCsuccess = false;
    private SentinelHttpMessage httpMessageC = null;
    private boolean attackDsuccess = false;
    private SentinelHttpMessage httpMessageD = null;

    
    private Color failColor = new Color(0xffcccc);
    
    public AttackXss(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }
    
    @Override
    public boolean performNextAttack() {
        if (initialMessage == null || initialMessage.getRequest() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "performNextAttack: no initialmessage");
        }
/*
        if (initialMessage.getReq().getChangeParam() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "performNextAttack: getChangeParam = null");
            return false;
        }
*/
     
        switch (state) {
            case 0:
                state++;
                return attackA();
            case 1:
                state++;
                return attackB();
            case 2:
                state++;
                return attackC();
//            case 3:
//                state++;
//                return attackD();
            default:
                return false;
        }
    }
    

    private boolean attackA() {
        String xssIndicatorStr = XssIndicator.getInstance().getIndicator();
        httpMessageA = initAttackHttpMessage(xssIndicatorStr);
        BurpCallbacks.getInstance().sendRessource(httpMessageA, followRedirect);

        String response = httpMessageA.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "attackA: response");
            return true; // TODO: reissue
        }
        
        if (response.contains(xssIndicatorStr)) {
            if (checkTag(response, xssIndicatorStr)) {
                messageAisInTag = true;
            } else {
                messageAisInTag = false;
            }
            
            // We found XSS - add attack result
            AttackResult res = new AttackResult("XSS0", "SUCCESS", httpMessageA.getReq().getChangeParam(), true);
            httpMessageA.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(xssIndicatorStr, failColor);
            httpMessageA.addHighlight(h);

            attackAsuccess = true;

            // go on
            return true;
        } else {
            AttackResult res = new AttackResult("XSS0", "FAIL", httpMessageA.getReq().getChangeParam(), false);
            httpMessageA.addAttackResult(res);

            attackAsuccess = false;

            // dont go on
            return false;
        }
    }

    private boolean attackB() {
        String xssIndicatorStr = XssIndicator.getInstance().getIndicator();

        httpMessageB = initAttackHttpMessage(xssIndicatorStr + "%3Cp%3E%22");
        BurpCallbacks.getInstance().sendRessource(httpMessageB, followRedirect);

        String response = httpMessageB.getRes().getResponseStr();
        if (response.contains(xssIndicatorStr + "<p>\"")) {
            // We found XSS - add attack result
            AttackResult res = new AttackResult("XSS1", "SUCCESS", httpMessageB.getReq().getChangeParam(), true);
            httpMessageB.addAttackResult(res);

            ResponseHighlight h;
            h = new ResponseHighlight(xssIndicatorStr, Color.yellow);
            httpMessageB.addHighlight(h);
            h = new ResponseHighlight(xssIndicatorStr + "<p>\"", failColor);
            httpMessageB.addHighlight(h);
            h = new ResponseHighlight(xssIndicatorStr + "%3Cp%3E%22", Color.green);
            httpMessageB.addHighlight(h);

            // Dont go on
            return false;
        } else {
            AttackResult res = new AttackResult("XSS1", "FAIL", httpMessageB.getReq().getChangeParam(), false);
            httpMessageB.addAttackResult(res);
            
            ResponseHighlight h;
            h = new ResponseHighlight(xssIndicatorStr, Color.yellow);
            httpMessageB.addHighlight(h);

            if (messageAisInTag) {
                // go on
                return true;
            } else {
                return false;
            }
        }
    }


    public SentinelHttpMessage getLastAttackMessage() {
        switch (state) {
            case 1:
                return httpMessageA;
            case 2:
                return httpMessageB;
            case 3:
                return httpMessageC;
//            case 3:
//                state++;
//                return attackD();
            default:
                return null;
        }

    }

    private boolean checkTag(String str, String findStr) {
        //String str = response;
        //String findStr = xssIndicatorStr + "\"=";
        int lastIndex = 0;
//        boolean isInTag = false;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                if (checkIfInTag(str, lastIndex)) {
                    return true;
                }
                lastIndex += findStr.length();
            }
        }

        return false;
    }

    private boolean checkIfInTag(String res, int lastIndex) {
        if ((res.lastIndexOf('>', lastIndex) < res.lastIndexOf('<', lastIndex))
                && (res.indexOf('>', lastIndex) < res.indexOf('<', lastIndex))) {
            return true;
        }

        return false;
    }

    private boolean attackC() {
        String xssIndicatorStr = XssIndicator.getInstance().getIndicator();

        httpMessageC = initAttackHttpMessage(xssIndicatorStr + "%22%3D");
        BurpCallbacks.getInstance().sendRessource(httpMessageC, followRedirect);

        String response = httpMessageC.getRes().getResponseStr();
        if (response.contains(xssIndicatorStr + "\"=") && checkTag(response, xssIndicatorStr + "\"=")) {
            // We found XSS - add attack result
            AttackResult res = new AttackResult("XSS2", "SUCCESS", httpMessageC.getReq().getChangeParam(), true);
            httpMessageC.addAttackResult(res);

            ResponseHighlight h;
            h = new ResponseHighlight(xssIndicatorStr, Color.yellow);
            httpMessageB.addHighlight(h);
            h = new ResponseHighlight(xssIndicatorStr + "\"=", failColor);
            httpMessageC.addHighlight(h);

            // Dont go on
            return false;
        } else {
            AttackResult res = new AttackResult("XSS2", "FAIL", httpMessageC.getReq().getChangeParam(), false);
            httpMessageC.addAttackResult(res);

            ResponseHighlight h;
            h = new ResponseHighlight(xssIndicatorStr, Color.yellow);
            httpMessageB.addHighlight(h);
            
            // go on
            return false;
        }

    }
}
