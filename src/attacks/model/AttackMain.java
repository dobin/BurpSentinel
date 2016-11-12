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
        CMD,
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
    
    
    private final ArrayList<AttackDescription> attackList;
    
    private final AttackTypes displayTypes[] = {
            AttackMain.AttackTypes.XSS,
            AttackMain.AttackTypes.XSSLESSTHAN,
            AttackMain.AttackTypes.SQLE,
            AttackMain.AttackTypes.CMD,
            AttackMain.AttackTypes.SQL,
        };
    
    private AttackMain() {
        attackList = new ArrayList<AttackDescription>();
        
        attackList.add(new AttackDescription(AttackTypes.XSS, "Cross-Site Scripting with small and smart payloads"));
        attackList.add(new AttackDescription(AttackTypes.XSSLESSTHAN, "Different encodings for '>' to check for XSS"));
        attackList.add(new AttackDescription(AttackTypes.SQLE, "SQL injections with small and smart payloads"));
        attackList.add(new AttackDescription(AttackTypes.CMD, "Comamnd injection with delays"));
    }
    

    public AttackTypes[] getAttackTypes() {
        return displayTypes;
    }
    
    public List<AttackDescription> getAttackDescriptions() {
        return attackList;
    }    
    
}
