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

import gui.networking.AttackWorkEntry;
import model.ResponseHighlight;
import java.awt.Color;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackXss extends AttackI {
    private int state = 0;
    private boolean inputReflectedInTag = false;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    
    private Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
private LinkedList<AttackData> attackData;
    
    
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
          
         9  ;alert(1)
        10  %3Balert(1)
          
        11  \\'\\"
        12  %5C%5C%27%5C%5C%22
        
        13  \u0022a
        14  %253Ca%2527%2522%253E
        */
        
        
        attackData.add(new AttackData(0, indicator, indicator, AttackData.AttackType.INFO));
        attackData.add(new AttackData(1, indicator + "<p>\"", indicator + "<p>\"", AttackData.AttackType.VULN));
        attackData.add(new AttackData(2, indicator + "%3Cp%3E%22", indicator + "<p>\"", AttackData.AttackType.VULN));
        attackData.add(new AttackData(3, indicator + "<p \"=>", indicator + "<p \"=>", AttackData.AttackType.VULN));
        attackData.add(new AttackData(4, indicator + "%3Cp%20%22%3D%3E", indicator + "<p \"=>", AttackData.AttackType.VULN));
        attackData.add(new AttackData(5, indicator + "' =", indicator + "' =", AttackData.AttackType.VULN));
        attackData.add(new AttackData(6, indicator + "%27%20%3D", indicator + "' =", AttackData.AttackType.VULN));
        attackData.add(new AttackData(7, indicator + "\" =", indicator + "\" =", AttackData.AttackType.VULN));
        attackData.add(new AttackData(8, indicator + "%22%20%3D", indicator + "\" =", AttackData.AttackType.VULN));
        attackData.add(new AttackData(9, indicator + ";alert(1)", indicator + ";alert(1)", AttackData.AttackType.VULN));
        attackData.add(new AttackData(10, indicator + "%3Balert(1)", indicator + ";alert(1)", AttackData.AttackType.VULN));
        attackData.add(new AttackData(11, indicator + "\\'\\\"", indicator + "\\'\\\"", AttackData.AttackType.VULN));
        attackData.add(new AttackData(12, indicator + "%5C%5C%27%5C%5C%22", indicator + "\\'\\\"", AttackData.AttackType.VULN));
        attackData.add(new AttackData(13, indicator + "\\u0022a", indicator + "", AttackData.AttackType.VULN));
        attackData.add(new AttackData(14, indicator + "%253Cp%2527%2522%253E", indicator + "<p'\">", AttackData.AttackType.VULN));
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
            case 14:
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
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        if (! httpMessage.getRes().hasResponse()) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        
        boolean hasXss = false;
        boolean hasInput = false;
        switch(state) {
            case 0:
                
                // ! tag
            case 1:
            case 2:
            case 3:
            case 4:
                if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                }
                if (hasInput && ! inputReflectedInTag) {
                    hasXss = true;
                }
                break;
            
                // tag
            case 5:
            case 6:
            case 7:
            case 8:
                if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                }
                if (hasInput && inputReflectedInTag) {
                    hasXss = true;
                }
                break;
                
                // whatever
            case 11:
            case 12:
            case 14:
                if (httpMessage.getRes().extractBody().contains(data.getOutput())) {
                    hasInput = true;
                }
        }
        
        if (hasXss) {
            data.setSuccess(true);
            
            AttackResult res = new AttackResult(
                    data.getAttackType(),
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    "Found: " + data.getOutput());
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else if (hasInput) {
            AttackResult res = new AttackResult(
                    AttackData.AttackType.INFO,
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    "Found: " + data.getOutput());
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else {
            data.setSuccess(false);
            
            AttackResult res = new AttackResult(
                    AttackData.AttackType.NONE,
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null);
            httpMessage.addAttackResult(res);
        }

        // Highlight indicator anyway
        String indicator = XssIndicator.getInstance().getBaseIndicator();
        if (! indicator.equals(data.getOutput())) {
            ResponseHighlight h = new ResponseHighlight(indicator, Color.green);
            httpMessage.getRes().addHighlight(h);
        }
        
        return httpMessage;
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
