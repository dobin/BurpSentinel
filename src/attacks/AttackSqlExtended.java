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

import gui.categorizer.model.ResponseCategory;
import gui.networking.AttackWorkEntry;
import model.ResponseHighlight;
import java.awt.Color;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackSqlExtended extends AttackI {

    private int state = 0;
    private SentinelHttpMessageAtk lastHttpMessage = null;

    final private Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
    private int atkSqlIntMax = 2;
    private String attackSqlInt(int state, String value) {
        String ret = "";
        
        switch (state) {
            case 0:
                ret = value + "a";
                break;
            case 1:   
                ret = value + "+1";
                break;
            case 2:
                ret = value + "+1-1";
        }
        
        return ret;
    }

    private int atkSqlStrMax = 5;
    private String attackSqlStr(int state, String value) {
        String ret = "";
        
        // origstr: aaa
        switch(state) {
            case 0:
                ret = value.substring(0, 1) + "'" + value.substring(1, value.length());
                break;
            case 1: // a'||'aa
                ret = value.substring(0, 1) + "'||'" + value.substring(1, value.length());
                break;
            case 2: // concat('a', 'aa')
                ret = "concat('" + value.substring(0, 1) + "', '" + value.substring(1, value.length()) + "')";
                break;
            case 3: // a'+'aa
                ret = value.substring(0, 1) + "'+'" + value.substring(1, value.length());
                break;
            case 4: // a'%2b'aa
                ret = value.substring(0, 1) + "'%2b'" + value.substring(1, value.length());
                break;
            case 5: // a'/**/'aa
                ret = value.substring(0, 1) + "'/**/'" + value.substring(1, value.length());
                break;
        }
        
        
        return ret;
    }
    
    /*
        // INT SELECT exists
        " OR 1=1",
        " OR 1=2",
        " OR 1=1 -- ",
        " OR 1=1 #",
        " OR 1=1 /*",
        ") OR 1=1",
        ") OR 1=2",
        ") OR 1=1 -- ",
        ") OR 1=1 #",
        ") OR 1=1 /*",
*/

    
     public AttackSqlExtended(AttackWorkEntry work) {
        super(work);
    }

             
    @Override
    public boolean init() {
        return true;
    }
     
    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        String data;
        if (/* str*/) {
            data = attackSqlStr(state);
            
            if (state < atkSqlStrMax - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            data = attackSqlInt(state);
            
            if (state < atkSqlIntMax - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        }
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
        
        state++;
        return doContinue;
    }

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }

    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data);
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);

        boolean hasError = false;
        ResponseCategory sqlResponseCategory = null;
        for(ResponseCategory rc: httpMessage.getRes().getCategories()) {
            if (rc.getCategoryEntry().getTag().equals("sqlerr")) {
                hasError = true;
                sqlResponseCategory = rc;
                break;
            }
        }
        
        if (hasError) {
            AttackResult res = new AttackResult(
                    AttackData.AttackType.VULN, 
                    "SQL" + state, 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    "Error message: " + sqlResponseCategory.getIndicator());
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(sqlResponseCategory.getIndicator(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else {
            AttackResult res = new AttackResult(
                    AttackData.AttackType.NONE, 
                    "SQL" + state, 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null);
            httpMessage.addAttackResult(res);
        }
        
        return httpMessage;
    }

}
