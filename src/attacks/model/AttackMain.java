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
package attacks.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author unreal
 */
public class AttackMain {
 
    public enum AttackTypes  {
        XSS,
        SQL,
        SQLE,
        OTHER,
        AUTHORISATION,
        ORIGINAL,
        LIST,
        XSSLESSTHAN,
    };
    
    static private AttackMain attackMain;
    
    static public AttackMain getInstance() {
        if (attackMain == null) {
            attackMain = new AttackMain();
        }
        
        return attackMain;
    }
    
    
    private ArrayList<AttackDescription> attackList;
    
    private AttackMain() {
        attackList = new ArrayList<AttackDescription>();
        
        attackList.add(new AttackDescription(AttackTypes.XSS, "XSS"));
        attackList.add(new AttackDescription(AttackTypes.SQL, "SQL"));
        attackList.add(new AttackDescription(AttackTypes.SQLE, "SQLe"));
    }
    

    public List<AttackDescription> getAttackDescriptions() {
        return attackList;
    }    
    
    AttackI getAttackClass(AttackTypes attackType) {
        AttackI attack;
        
        switch(attackType) {
            case XSS:
                //return new AttackXss();
                return null;
            default: 
                return null;
        }
    }
    

    
}
