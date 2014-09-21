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

import gui.botLeft.PanelLeftInsertions;
import gui.categorizer.model.ResponseCategory;
import gui.networking.AttackWorkEntry;
import model.ResponseHighlight;
import java.awt.Color;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;
import util.Utility;

/**
 *
 * @author unreal
 */
public class AttackSql extends AttackI {

    private int state = 0;
    private boolean doContinue = false;
    private SentinelHttpMessageAtk lastHttpMessage = null;

    private final Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
    private final String[] attackDataSql = {
        // Break
        "'BREAK\"",
        
        // Should return Original Value - variant 1
        " OR 41=42",
        "' OR '41'='42",
        "\" OR \"41\"=\"42",
        "/**/",

        // Should return original value - variant 2
        ") OR (41=42",
        "') OR ('41'='42",
        "\") OR (\"41\"=\"42",       
    };
    
     public AttackSql(AttackWorkEntry work) {
        super(work);
    }

             
    @Override
    public boolean init() {
        return true;
    }
    
    // return attack string
    // Also sets "doContinue"
    // Oh my god this is ugly
    private String getData(AttackWorkEntry attackWorkEntry) {
        String data;

        boolean onlyUrlencoded;
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            onlyUrlencoded = true;
        } else {
            onlyUrlencoded = false;
        }

        if (onlyUrlencoded) {
            data = attackDataSql[state];
            data = Utility.realUrlEncode(data);
                
            if (state < attackDataSql.length - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackDataSql.length) {                
                data = attackDataSql[state];
                data = Utility.realUrlEncode(data);
            
                doContinue = true;
            } else {
                int newState = state - attackDataSql.length;
                            
                data = attackDataSql[newState];
                
                if (newState < attackDataSql.length - 1) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }
            }
        }
        
        return data;
    }
    
    
    @Override
    public boolean performNextAttack() {
        String data;
        
        // Overwrite insert position, as we will always append
        attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.RIGHT;     
        
        data = getData(attackWorkEntry);
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            BurpCallbacks.getInstance().print("Connection timeout");
            state++;
            return false;
        }

        analyzeResponse();
        
        state++;
        return doContinue;
    }

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }

        
    private int responseOnErrorSizeChange = 0;
    private void analyzeResponse() {
        
        // Test: Response size
        if (state == 0) {
            // First request is the "generate error" request
            int origResponseSize = attackWorkEntry.origHttpMessage.getRes().getSize();
            int newResponseSize = lastHttpMessage.getRes().getSize();
            
            responseOnErrorSizeChange = origResponseSize - newResponseSize;
            
            // Whaaat, no difference on error? Check content, and stop if equal
            if (responseOnErrorSizeChange == 0) {
                if (attackWorkEntry.origHttpMessage.getRes().getResponseStrBody().equals(lastHttpMessage.getRes().getResponseStrBody())) {
                    doContinue = false;
                    
                    // Same size as original request!
                    AttackResult res = new AttackResult(
                            AttackData.AttackType.INFO,
                            "SQL" + state,
                            lastHttpMessage.getReq().getChangeParam(),
                            true,
                            "break request identical to original. no chance to identify SQL.");
                    lastHttpMessage.addAttackResult(res);
                }
            }
        } else {
            // Check if first (test) request did produce a change
            if (responseOnErrorSizeChange != 0) {
                // Check if size differs now
                int origResponseSize = attackWorkEntry.origHttpMessage.getRes().getSize();
                int newResponseSize = lastHttpMessage.getRes().getSize();
                if (newResponseSize == origResponseSize) {
                    // Same size as original request!
                    AttackResult res = new AttackResult(
                            AttackData.AttackType.INFO,
                            "SQLE" + state,
                            lastHttpMessage.getReq().getChangeParam(),
                            true,
                            "Same size as original request: " + origResponseSize);
                    lastHttpMessage.addAttackResult(res);
                }
            }
        }
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
