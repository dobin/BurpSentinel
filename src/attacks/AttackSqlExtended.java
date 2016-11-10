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
import gui.botLeft.PanelLeftInsertions;
import gui.networking.AttackWorkEntry;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import org.apache.commons.lang3.StringUtils;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;
import util.Utility;

/**
 *
 * @author unreal
 */
public class AttackSqlExtended extends AttackI {
    private int state = -1;
    private AttackSqlExtendedAnalyzer analyzer;
    private boolean doContinue = false;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    private LinkedList<byte[]> analRes = new LinkedList<byte[]>();
    
    private static String atkName = "SQLE";
    
    private final int attackSqlIntSize = 3;
    private String attackSqlInt(int state, String value) {
        String ret = "";
        
        switch (state) {
            // break test
            case 0:
                ret = value + "BREAK1'2\"3`";
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
            data = Utility.realUrlEncode(data);
                
            if (state < attackSqlIntSize - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackSqlIntSize) {                
                data = attackSqlInt(state, data);
                data = Utility.realUrlEncode(data);
            
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
            data = Utility.realUrlEncode(data);
                
            if (state < attackSqlStrSize - 1) {
                doContinue = true;
            } else {
                doContinue = false;
            }
        } else {
            if (state < attackSqlStrSize) {                
                data = attackSqlStr(state, data);
                data = Utility.realUrlEncode(data);
            
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
        
        analyzer = new AttackSqlExtendedAnalyzer();
    }

    
     
    @Override
    public boolean performNextAttack() {        
        String origParamValue = attackWorkEntry.attackHttpParam.getDecodedValue();
        String data;
        
        // Cant handle empty params
        if (origParamValue.length() == 0) {
            return false;
        }
        
        // Overwrite insert position, as we will always overwrite here
        attackWorkEntry.insertPosition = PanelLeftInsertions.InsertPositions.REPLACE;        
        
        if (state == -1) {
            data = origParamValue;
        } else {
            if (StringUtils.isNumeric(origParamValue)) {
                data = getDataInt(attackWorkEntry, origParamValue);
            } else {
                data = getDataStr(attackWorkEntry, origParamValue);
            }            
        }
        
        try {
            SentinelHttpMessage httpMessage = attack(data);
            if (httpMessage == null) {
                BurpCallbacks.getInstance().getBurp().printOutput("HTTPMESSAGE NULL");
                return false;
            }
            analRes.add(httpMessage.getResponse());
        } catch (ConnectionTimeoutException ex) {
            BurpCallbacks.getInstance().print("Connection timeout");
            state++;
            return false;
        }
        
        analyzeResponse();
        state++;
        
        // End
        if (doContinue == false) {
            BurpCallbacks.getInstance().getBurp().printOutput("End!");
            analyzer.attackEndAnalyzer(analRes);
        } else {
            BurpCallbacks.getInstance().getBurp().printOutput("No End!");
        }
        
        return doContinue;
    }
    
    
    private void analyzeResponse() {
        if (state == -1) {
            analyzeOriginalRequest(lastHttpMessage);
            doContinue = true;
        } else if (state == 0) {
            doContinue = analyzer.analyzeBreak(attackWorkEntry, lastHttpMessage);
        } else {
            analyzer.analyzeAttackResponse(attackWorkEntry, lastHttpMessage);
        }
    }

    
    private SentinelHttpMessage attack(String data) throws ConnectionTimeoutException {
        SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(data, atkName, state);
        if (httpMessage == null) {
            return null;
        }
        lastHttpMessage = httpMessage;
        BurpCallbacks.getInstance().sendRessource(httpMessage, attackWorkEntry.followRedirect);
        
        return httpMessage;
    }
    
             
    @Override
    public boolean init() {
        return true;
    }
    
    
    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return lastHttpMessage;
    }
}
