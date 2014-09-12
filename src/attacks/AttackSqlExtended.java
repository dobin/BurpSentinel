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
import org.apache.commons.lang3.StringUtils;
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
    
    private final int attackSqlIntSize = 3;
    private String attackSqlInt(int state, String value) {
        String ret = "";
        
        switch (state) {
            // break test
            case 0:
                ret = value + "'%20BREAK";
                break;
                
            // pure int based (no quotes)
            // ID = INT
            // select ... WHERE id = 1
            case 1:   
                ret = value + "+1-1";
                break;
                
            // integer in db, but string in params
            // ID = INT
            // select ... WHERE id = '1'
            case 2: // 
                ret = value + "' + 0 + '0";
                break;
        }
        
        return ret;
    }

    private final int attackSqlStrSize = 5;
    private String attackSqlStr(int state, String value) {
        String ret = "";
        
        // origstr: aaa
        // SELECT ... WHERE name = 'aaa'
        switch(state) {
            case 0: // a'BREAK"aa
                ret = value.substring(0, 1) + "'BREAK\"" + value.substring(1, value.length());
                break;
            case 1: // a'||'aa
                ret = value.substring(0, 1) + "' || '" + value.substring(1, value.length());
                break;
            case 2: // a' + 'aa
                ret = value.substring(0, 1) + "' + '" + value.substring(1, value.length());
                break;
            case 3: // a' 'aa
                ret = value.substring(0, 1) + "' '" + value.substring(1, value.length());
                break;
            case 4: // /**/aaa
                ret = "/**/" + value;
                break;
        }
        
        return ret;
    }
    
    private boolean doContinue = false;
    
    // return attack string
    // Also sets "doContinue"
    // Oh my god this is ugly
    private String getDataInt(AttackWorkEntry attackWorkEntry, String data) {
        boolean onlyUrlencoded;
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            onlyUrlencoded = true;
        } else {
            onlyUrlencoded = false;
        }

        if (onlyUrlencoded) {
            data = attackSqlInt(state, data);
            data = BurpCallbacks.getInstance().getBurp().getHelpers().urlEncode(data);
                
            if (state < attackSqlIntSize - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackSqlIntSize) {                
            data = attackSqlInt(state, data);
                data = BurpCallbacks.getInstance().getBurp().getHelpers().urlEncode(data);
            
                doContinue = true;
            } else {
                int newState = state - attackSqlIntSize;
                            
                data = attackSqlInt(newState, data);
                
                if (newState < attackSqlIntSize - 1) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }
            }
        }
        
        return data;
    }
    
        // return attack string
    // Also sets "doContinue"
    // Oh my god this is ugly
    private String getDataStr(AttackWorkEntry attackWorkEntry, String data) {
        boolean onlyUrlencoded;
        if (attackWorkEntry.attackHttpParam.getTypeStr().equals("GET") 
                || attackWorkEntry.attackHttpParam.getTypeStr().equals("PATH")) 
        {
            onlyUrlencoded = true;
        } else {
            onlyUrlencoded = false;
        }

        if (onlyUrlencoded) {
            data = attackSqlStr(state, data);
            data = BurpCallbacks.getInstance().getBurp().getHelpers().urlEncode(data);
                
            if (state < attackSqlStrSize - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackSqlStrSize) {                
                data = attackSqlStr(state, data);
                data = BurpCallbacks.getInstance().getBurp().getHelpers().urlEncode(data);
            
                doContinue = true;
            } else {
                int newState = state - attackSqlStrSize;
                            
                data = attackSqlStr(newState, data);
                
                if (newState < attackSqlStrSize - 1) {
                    doContinue = true;
                } else {
                    doContinue = false;
                }
            }
        }
        
        return data;
    }
    
     public AttackSqlExtended(AttackWorkEntry work) {
        super(work);
    }

             
    @Override
    public boolean init() {
        return true;
    }
     
    @Override
    public boolean performNextAttack() {        
        String origParamValue = attackWorkEntry.attackHttpParam.getDecodedValue();
        String data;
        
        // Overwrite insert position, as we will always overwrite here
        attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.REPLACE;        
        
        if (StringUtils.isNumeric(origParamValue)) {
            data = getDataInt(attackWorkEntry, origParamValue);
        } else {            
            data = getDataStr(attackWorkEntry, origParamValue);
        }
        
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
                    "SQLE" + state, 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    "Error message: " + sqlResponseCategory.getIndicator());
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(sqlResponseCategory.getIndicator(), failColor);
            httpMessage.getRes().addHighlight(h);
        } else {
            AttackResult res = new AttackResult(
                    AttackData.AttackType.NONE, 
                    "SQLE" + state, 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null);
            httpMessage.addAttackResult(res);
        }
        
        return httpMessage;
    }

}
