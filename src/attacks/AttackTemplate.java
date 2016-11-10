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
import attacks.model.AttackI;
import gui.networking.AttackWorkEntry;
import java.awt.Color;
import java.util.LinkedList;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.XssIndicator;
import org.w3c.tidy.TidyMessage;
import util.ConnectionTimeoutException;

/**
 *
 * @author dobin
 */
public class AttackTemplate extends AttackI {
    // Static:
    private final Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    private LinkedList<AttackData> attackData;
    
    // Changes per iteration:
    private int state = 0;
    private SentinelHttpMessageAtk lastHttpMessage = null;
    
    // Information from state 0 - may be used in state >0
    private boolean inputReflectedInTag = false;
    
    public AttackTemplate(AttackWorkEntry work) {
        super(work);
        
        attackData = new LinkedList<AttackData>();
        String indicator;
        
        indicator = XssIndicator.getInstance().getIndicator();
        
        attackData.add(new AttackData(0, indicator, indicator, AttackData.AttackResultType.STATUSGOOD));
        attackData.add(new AttackData(1, indicator + "{{7*7}}", indicator + "49", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(2, indicator + "${7*7}", indicator + "49", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(3, indicator + "{{7*'7'}}", indicator + "49", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(4, indicator + "a{*comment*}", indicator + "<p \"=>", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(5, indicator + "${\"z\".join(\"ab\")", indicator + "' =", AttackData.AttackResultType.VULNSURE));
        
        attackData.add(new AttackData(6, indicator + "%27%20%3D", indicator + "' =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(7, indicator + "\" =", indicator + "\" =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(8, indicator + "%22%20%3D", indicator + "\" =", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(9, indicator + "%5C%27%5C%22_\\'\\\"", indicator + "", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(10, indicator + "_\\u0022_æ_\\u00E6_", indicator + "", AttackData.AttackResultType.VULNSURE));
        attackData.add(new AttackData(11, indicator + "%253Cp%2527%2522%253E", indicator + "<p'\">", AttackData.AttackResultType.VULNSURE));
    }
    
    @Override
    public boolean init() {
        return true;
    }
    
    private SentinelHttpMessage attack(AttackData data) throws ConnectionTimeoutException {
        return null;
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

/*                if (checkTag(httpMessage.getRes().getResponseStr(), XssIndicator.getInstance().getBaseIndicator())) {
                    inputReflectedInTag = true;
                } else {
                    inputReflectedInTag = false;
                }*/
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

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
