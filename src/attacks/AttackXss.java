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
import attacks.model.AttackData;
import gui.networking.AttackWorkEntry;
import model.ResponseHighlight;
import java.awt.Color;
import java.util.LinkedList;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackXss extends AttackI {
    // Static:
    private LinkedList<AttackData> attackData;
    private static final String atkName = "XSS";
    
    // Changes per iteration:
    private int state = -1;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    private final AttackXssAnalyzer analyzer;
    
    
    public AttackXss(AttackWorkEntry work) {
        super(work);
        analyzer = new AttackXssAnalyzer();
        
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
        
        attackData.add(new AttackData(0, indicator, indicator, AttackData.AttackResultType.STATUSGOOD));
        attackData.add(new AttackData(1, indicator + "<p>\"", indicator + "<p>\"", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(2, indicator + "%3Cp%3E%22", indicator + "<p>\"", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(3, indicator + "<p \"=>", indicator + "<p \"=>", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(4, indicator + "%3Cp%20%22%3D%3E", indicator + "<p \"=>", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(5, indicator + "' =", indicator + "' =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(6, indicator + "%27%20%3D", indicator + "' =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(7, indicator + "\" =", indicator + "\" =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(8, indicator + "%22%20%3D", indicator + "\" =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(9, indicator + "%5C%27%5C%22_\\'\\\"", indicator + "", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(10, indicator + "_\\u0022_æ_\\u00E6_", indicator + "", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(11, indicator + "%253Cp%2527%2522%253E", indicator + "<p'\">", AttackData.AttackResultType.VULNSURE));
    }
 

    @Override
    public boolean performNextAttack() {
        boolean doContinue = true;
        AttackData data;
        SentinelHttpMessageAtk httpMessage;
        
        if (state == -1) {
            data = new AttackData(-1, "", "", AttackData.AttackResultType.INFO);
        } else {
            data = attackData.get(state);
        }
        
        try {
            httpMessage = attack(data);
            if (httpMessage == null) {
                return false;
            }
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
        
        analyzeResponse(data, httpMessage);
        
        if (state == 11) {
            doContinue = false;
        }
 
        state++;
        return doContinue;
    }
    
    
    private void analyzeResponse(AttackData data, SentinelHttpMessageAtk httpMessage) {
        // Highlight indicator anyway
        String indicator = XssIndicator.getInstance().getBaseIndicator();
        if (! indicator.equals(data.getOutput())) {
            ResponseHighlight h = new ResponseHighlight(indicator, Color.green);
            httpMessage.getRes().addHighlight(h);
        }
        
        if (state == -1) {
            analyzeOriginalRequest(lastHttpMessage);
        } else if (state == 0) {
            analyzer.analyzeInitialResponse(data, lastHttpMessage);
        } else if (state == 1 || state == 2 || state == 3 || state == 4) {
            analyzer.analyzeAttackResponseNonTag(data, lastHttpMessage);
        } else if (state == 5 || state == 6 || state == 7 || state == 8) {
            analyzer.analyzeAttackResponseTag(data, lastHttpMessage);
        } else {
            // Nothing
        }
    }
    
    
    private SentinelHttpMessageAtk attack(AttackData data) throws ConnectionTimeoutException {
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            data.urlEncode();
        }
        
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data.getInput(), atkName, state);
        if (httpMessage == null) {
            return null;
        }
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        if (! httpMessage.getRes().hasResponse()) {
            BurpCallbacks.getInstance().print("Response error");
            return httpMessage;
        }
        
        return httpMessage;
    }
    
    
    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }

       
    @Override
    public boolean init() {
        return true;
    }
}
