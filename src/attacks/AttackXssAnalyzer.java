/*
 * Copyright (C) 2016 dobin
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

import attacks.model.AttackData;
import attacks.model.AttackResult;
import gui.networking.AttackWorkEntry;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import model.ResponseHighlight;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import org.w3c.tidy.TidyMessage;

/**
 *
 * @author dobin
 */
public class AttackXssAnalyzer {
    private final Color failColor = new Color(0xff, 0xcc, 0xcc, 100);

    // Information from state 0 - may be used in state >0
    private boolean inputReflectedInTag = false;
    
    private LinkedList<TidyMessage> origTidyMsgs = null;


    public void attackEndAnalyzer(List<byte[]> analRes) {
    }

    public void analyzeInitialResponse(AttackData data, SentinelHttpMessageAtk httpMessage) {

        if (checkTag(httpMessage.getRes().getResponseStr(), XssIndicator.getInstance().getBaseIndicator())) {
            inputReflectedInTag = true;
        } else {
            inputReflectedInTag = false;
        }

        origTidyMsgs = util.Beautifier.getInstance().analyze(httpMessage.getRes().extractBody());

        if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.INFO,
                    "XSS" + data.getIndex(),
                    httpMessage.getReq().getChangeParam(),
                    true,
                    "Good. Found input param",
                    "The response to the test request contained the input parameter. Reflected XSS is possible.");
            httpMessage.addAttackResult(res);
            data.setSuccess(true);
        } else {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.INFO,
                    "XSS" + data.getIndex(),
                    httpMessage.getReq().getChangeParam(),
                    true,
                    "Bad. Didnt find input param",
                    "The response to the test request does not contain the input parameter. Reflected XSS probably not possible.");
            httpMessage.addAttackResult(res);
        }
    }

    
    public void analyzeAttackResponseTag(AttackData data, SentinelHttpMessageAtk httpMessage) {
        String message = "";
        
        ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
        httpMessage.getRes().addHighlight(h);

        
        if (httpMessage.getRes().extractBody().contains(data.getOutput()) && inputReflectedInTag) {
            data.setSuccess(true);
            message += "Found in tag: " + data.getOutput();
             
            AttackResult res = new AttackResult(
                data.getAttackType(),
                "XSS" + data.getIndex(),
                httpMessage.getReq().getChangeParam(),
                true,
                message,
                "Found special chars in response which allows to break out of a tag.");
            httpMessage.addAttackResult(res);
         }
    }
    
    
    public void analyzeAttackResponseNonTag(AttackData data, SentinelHttpMessageAtk httpMessage) {
        String message = "";

        ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
        httpMessage.getRes().addHighlight(h);

        
        // Check for decoded string in response
        if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
            data.setSuccess(true);
            message += "Found: " + data.getOutput();
            
            AttackResult res = new AttackResult(
                data.getAttackType(),
                "XSS" + data.getIndex(),
                httpMessage.getReq().getChangeParam(),
                true,
                message,
                "Found special chars in response which allows to create new tags.");
            httpMessage.addAttackResult(res);
        }

        // Alternativ: Check for html syntax error
        LinkedList<TidyMessage> msgs = util.Beautifier.getInstance().analyze(httpMessage.getRes().extractBody());

        if (util.Beautifier.getInstance().hasHtmlSyntaxError(origTidyMsgs, msgs)) {
            if (message.equals("")) {
                message += "Found HTML errors: <br>" + util.Beautifier.getInstance().getMessageDiffString(origTidyMsgs, msgs);
            }
        }
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
