/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package attacks;

import attacks.model.AttackI;
import attacks.model.AttackResult;
import attacks.model.AttackData;
import gui.networking.AttackWorkEntry;
import model.ResponseHighlight;
import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import org.w3c.tidy.TidyMessage;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackXss extends AttackI {
    // Static:
    private final Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    private LinkedList<AttackData> attackData;
    
    // Changes per iteration:
    private int state = 0;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    
    // Information from state 0 - may be used in state >0
    private boolean inputReflectedInTag = false;
    private LinkedList<TidyMessage> origTidyMsgs = null;
    
    public AttackXss(AttackWorkEntry work) {
        super(work);
        
        attackData = new LinkedList<AttackData>();
        String indicator;
        
        indicator = XssIndicator.getInstance().getIndicator();
        /*
         1  <p>"
         2  %3Cp%3E%22
           
         3  <p "=>
         4  %3Cp%20%22%3D%3E
          
         5  ' =                 t
         6  %27%20%3D           t
           
         7  " =                 t
         8  %20%22%3D           t
          
         9  %5C%5C%27%5C%5C%22_\'\"
        10  _\u0022a_æ_\u00e6
        11  %253Ca%2527%2522%253E
        */
        
        attackData.add(new AttackData(0, indicator, indicator, AttackData.AttackResultType.INFO));
        attackData.add(new AttackData(1, indicator + "<p>\"", indicator + "<p>\"", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(2, indicator + "%3Cp%3E%22", indicator + "<p>\"", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(3, indicator + "<p \"=>", indicator + "<p \"=>", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(4, indicator + "%3Cp%20%22%3D%3E", indicator + "<p \"=>", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(5, indicator + "' =", indicator + "' =", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(6, indicator + "%27%20%3D", indicator + "' =", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(7, indicator + "\" =", indicator + "\" =", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(8, indicator + "%22%20%3D", indicator + "\" =", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(9, indicator + "%5C%27%5C%22_\\'\\\"", indicator + "", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(10, indicator + "_\\u0022_æ_\\u00E6_", indicator + "", AttackData.AttackResultType.VULN));
        attackData.add(new AttackData(11, indicator + "%253Cp%2527%2522%253E", indicator + "<p'\">", AttackData.AttackResultType.VULN));
    }
    
    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        AttackData data = attackData.get(state);
        SentinelHttpMessage httpMessage;
        try {
            httpMessage = attack(data);
            if (httpMessage == null) {
                return false;
            }
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
 
        switch (state) {
            case 0:
                doContinue = true;

                if (checkTag(httpMessage.getRes().getResponseStr(), XssIndicator.getInstance().getBaseIndicator())) {
                    inputReflectedInTag = true;
                } else {
                    inputReflectedInTag = false;
                }
                break;
            case 11:
                doContinue = false;
                break;
            default:
                doContinue = true;
                break;
        }
        
        state++;
        return doContinue;
    }
    
    private SentinelHttpMessage attack(AttackData data) throws ConnectionTimeoutException {
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            data.urlEncode();
        }
        
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data.getInput());
        if (httpMessage == null) {
            return null;
        }
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        if (! httpMessage.getRes().hasResponse()) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        try {
            analyzeResponse(data, httpMessage);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AttackXss.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        // Highlight indicator anyway
        String indicator = XssIndicator.getInstance().getBaseIndicator();
        if (! indicator.equals(data.getOutput())) {
            ResponseHighlight h = new ResponseHighlight(indicator, Color.green);
            httpMessage.getRes().addHighlight(h);
        }
        
        return httpMessage;
    }
    
    
    private void analyzeResponse(AttackData data, SentinelHttpMessageAtk httpMessage) throws UnsupportedEncodingException {
        boolean hasXss = false;
        boolean hasInput = false;
        String message = "<html>";
        switch(state) {
            case 0:
                origTidyMsgs = util.Beautifier.getInstance().analyze(httpMessage.getRes().extractBody());
                
                if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                    message += "Found decoded attack string: " + URLEncoder.encode(data.getOutput(), "UTF-8");
                }

                break;
                
                // ! tag
            case 1:
            case 2:
            case 3:
            case 4:
                // Check for decoded string in response
                if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                    message += "Found decoded attack string: " + URLEncoder.encode(data.getOutput(), "UTF-8");
                }
                
                // Check if decoded string is at the right place
                if (hasInput && ! inputReflectedInTag) {
                    hasXss = true;
                }
                
                break;
            
                // tag
            case 7:
            case 8:
                if (httpMessage.getRes().extractBody().contains(data.getOutput()) && inputReflectedInTag) {
                    hasInput = true;
                    message += "Found decoded attack string: " + URLEncoder.encode(data.getOutput(), "UTF-8");
                }
                break;
            case 11:
                 if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                    message += "Found decoded attack string: " + URLEncoder.encode(data.getOutput(), "UTF-8");
                }
        }
        
        if (state > 0) {
            // Alternativ: Check for html syntax error
            LinkedList<TidyMessage> msgs = util.Beautifier.getInstance().analyze(httpMessage.getRes().extractBody());
            
            if (util.Beautifier.getInstance().hasHtmlSyntaxError(origTidyMsgs, msgs)) {
                hasXss = true;
                if (! message.equals("<html>")) {
                    message += "<br><br>";
                }
                message += "Found HTML errors: <br>" + util.Beautifier.getInstance().getMessageDiffString(origTidyMsgs, msgs);
            }
        }
        message += "</html>";
        
        if (hasXss) {
            data.setSuccess(true);
            
            AttackResult res = new AttackResult(
                    data.getAttackType(),
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    message);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else if (hasInput) {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.INFO,
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    message);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else {
            data.setSuccess(false);
            
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.NONE,
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null);
            httpMessage.addAttackResult(res);
        }
    }
    
    
    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }

    private boolean checkTag(String str, String findStr) {
        int lastIndex = 0;

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
         && (res.indexOf('>', lastIndex) < res.indexOf('<', lastIndex))) 
        {
            return true;
        }

        return false;
    }
}
