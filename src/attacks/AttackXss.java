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
import gui.viewMessage.ResponseHighlight;
import java.awt.Color;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
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
    
    
    public AttackXss(AttackWorkEntry work) {
        super(work);
    }
    
    @Override
    public boolean init() {
        return true;
    }

    
    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        AttackData data = attackDataXss[state];
        SentinelHttpMessage httpMessage;
        try {
            httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
 
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
    
    private SentinelHttpMessage attack(AttackData data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data.getInput());
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        String response = httpMessage.getRes().getResponseStr();
        if (response == null || response.length() == 0) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        if (response.contains(data.getOutput())) {
            data.setSuccess(true);
            
            AttackResult res = new AttackResult(
                    data.getAttackType(),
                    "XSS" + data.getIndex(), 
                    httpMessage.getReq().getChangeParam(), 
                    true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(data.getOutput(), failColor);
            httpMessage.addHighlight(h);
        } else {
            data.setSuccess(false);
            
            AttackResult res = new AttackResult(
                    AttackData.AttackType.NONE,
                    "XSS" + data.getIndex(), 
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
    public SentinelHttpMessageAtk getLastAttackMessage() {
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
