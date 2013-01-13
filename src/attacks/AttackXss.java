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
    private boolean inputReflectedInTag = false;
    private SentinelHttpMessage lastHttpMessage = null;
    
    private Color failColor = new Color(0xffcccc);
    
    private AttackData[] attackDataXss = {
        new AttackData(0, 
            XssIndicator.getInstance().getIndicator(), 
            XssIndicator.getInstance().getIndicator(),
            AttackData.AttackType.INFO),
        new AttackData(1, XssIndicator.getInstance().getIndicator() + "%3Cp%3E%22", XssIndicator.getInstance().getIndicator() + "<p>\"", AttackData.AttackType.VULN),
        new AttackData(2, XssIndicator.getInstance().getIndicator() + "<p>\"", XssIndicator.getInstance().getIndicator() + "<p>\"", AttackData.AttackType.VULN),
        new AttackData(3, XssIndicator.getInstance().getIndicator() + "%22%3D", XssIndicator.getInstance().getIndicator() + "\"=", AttackData.AttackType.VULN),
        new AttackData(4, XssIndicator.getInstance().getIndicator() + "\"=", XssIndicator.getInstance().getIndicator() + "\"=", AttackData.AttackType.VULN),
    };
    
    
    public AttackXss(SentinelHttpMessage origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }
    
    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        if (initialMessage == null || initialMessage.getRequest() == null) {
            BurpCallbacks.getInstance().print("performNextAttack: no initialmessage");
        }
        if (initialMessage.getReq().getChangeParam() == null) {
            BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }

        
        AttackData data = attackDataXss[state];
        SentinelHttpMessage httpMessage = attack(data);
 
        switch (state) {
            case 0:
                // Goon if: reflected
                if (data.getSuccess()) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }

                if (checkTag(httpMessage.getRes().getResponseStr(), XssIndicator.getInstance().getIndicator())) {
                    inputReflectedInTag = true;
                } else {
                    inputReflectedInTag = false;
                }
                break;
            case 1:
                // Goon if: not successful
                if (data.getSuccess()) {
                    doContinue = false;
                } else {
                    doContinue = true;
                }
                break;
            case 2:
                // Goon if: not successful
                if (data.getSuccess()) {
                    doContinue = false;
                } else {
                    doContinue = true;
                }
                break;
            case 3:
                // Goon if: not successful or in tag
                if (data.getSuccess() || inputReflectedInTag) {
                    doContinue = false;
                } else {
                    doContinue = true;
                }
                break;
            case 4:
                // Finito
                doContinue = false;
                break;
        }
        
        state++;
        return doContinue;
    }
    
    private SentinelHttpMessage attack(AttackData data) {
        SentinelHttpMessage httpMessage = initAttackHttpMessage(data.getInput());
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);
        
        String response = httpMessage.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        if (response.contains(data.getOutput())) {
            data.setSuccess(true);
            
            AttackResult res = new AttackResult(
                    "XSS" + data.getIndex(), 
                    data.getAttackType().toString(),
                    httpMessage.getReq().getChangeParam(), 
                    true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.addHighlight(h);
        } else {
            data.setSuccess(false);
            
            AttackResult res = new AttackResult(
                    "XSS" + data.getIndex(), 
                    "-", 
                    httpMessage.getReq().getChangeParam(), 
                    false);
            httpMessage.addAttackResult(res);
        }

        // Highlight indicator anyway
        String indicator = XssIndicator.getInstance().getIndicator();
        if (! indicator.equals(data.getOutput())) {
            ResponseHighlight h = new ResponseHighlight(indicator, Color.green);
            httpMessage.addHighlight(h);
        }
        
        return httpMessage;
    }
    
    @Override
    public SentinelHttpMessage getLastAttackMessage() {
        return lastHttpMessage;
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
}
