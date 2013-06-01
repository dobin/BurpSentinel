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

import attacks.AttackData.AttackType;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import util.BurpCallbacks;
import util.ConnectionTimeoutException;

/**
 *
 * @author unreal
 */
public class AttackOriginal extends AttackI {

    private SentinelHttpMessageAtk message;
    
    public AttackOriginal(SentinelHttpMessageOrig origHttpMessage, String mainSessionName, boolean followRedirect, SentinelHttpParam origParam) {
        super(origHttpMessage, mainSessionName, followRedirect, origParam);
    }
    
    @Override
    public boolean performNextAttack() {
        try {
            if (initialMessage == null || initialMessage.getRequest() == null) {
                BurpCallbacks.getInstance().print("performNextAttack: no initialmessage");
            }

            SentinelHttpMessageAtk httpMessage = initAttackHttpMessage(null);
            BurpCallbacks.getInstance().sendRessource(httpMessage, followRedirect);
            this.message = httpMessage;
            
            AttackResult res = new AttackResult(
                    AttackType.INFO,
                    "ORIG",
                    httpMessage.getReq().getChangeParam(),
                    false);
            httpMessage.addAttackResult(res);

        } catch (ConnectionTimeoutException ex) {
            Logger.getLogger(AttackOriginal.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public SentinelHttpMessageAtk getLastAttackMessage() {
        return message;
    }
    
}
