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

import gui.viewMessage.ResponseHighlight;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackSql extends AttackI {

    private int state = 0;
    private SentinelHttpMessageAtk lastHttpMessage = null;

    private Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
    private String[] attackDataSql = {
        "'",
        "''",
        "'''",
        "%27",
        "%27%27",
        "%27%27%27",
        "\"",
        "\"\"",
        "\"\"\"",
        "%22",
        "%22%22",
        "%22%22%22",
    };
    
    public AttackSql(SentinelHttpMessageOrig origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }

    @Override
    public boolean performNextAttack() {
        boolean doContinue = false;
        
        if (initialMessage == null || initialMessage.getRequest() == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "performNextAttack: no initialmessage");
        }
        if (initialMessage.getReq().getChangeParam() == null) {
            //BurpCallbacks.getInstance().print("performNextAttack: no changeparam");
            //return false;
        }
        
        String data = attackDataSql[state];
        try {
            SentinelHttpMessage httpMessage = attack(data);
        } catch (ConnectionTimeoutException ex) {
            state++;
            return false;
        }
        
        if (state < attackDataSql.length - 1) {
            doContinue = true;
        } else {
            doContinue = false;
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
        BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);

        String response = httpMessage.getRes().getResponseStr();
        if (response.contains("MySQL")) {
            AttackResult res = new AttackResult(AttackData.AttackType.VULN, "SQL" + state, httpMessage.getReq().getChangeParam(), true);
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight("MySQL", failColor);
            httpMessage.addHighlight(h);
        } else {
            AttackResult res = new AttackResult(AttackData.AttackType.NONE, "SQL" + state, httpMessage.getReq().getChangeParam(), false);
            httpMessage.addAttackResult(res);
        }
        
        return httpMessage;
    }

}
