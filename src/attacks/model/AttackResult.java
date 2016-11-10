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

import java.io.Serializable;
import model.SentinelHttpParam;

/**
 * Is serializable, because it is used by model
 * 
 * @author unreal
 */
public class AttackResult implements Serializable {
    private String attackName = "";
    private AttackData.AttackResultType attackType = null;
    private SentinelHttpParam attackParam = null;
    private boolean success;
    private String resultDescription;
    private String explanation;

    /*
     * ResultDescription can be null
     */
    public AttackResult(AttackData.AttackResultType attackType, String attackName, SentinelHttpParam attackParam, boolean success, String resultDescription, String explanation) {
        this.attackName = attackName;
        this.attackType = attackType;
        this.attackParam = attackParam;
        this.success = success;
        this.resultDescription = resultDescription;
        this.explanation = explanation;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public SentinelHttpParam getAttackParam() {
        return attackParam;
    }
   
    public String getAttackName() {
        return attackName;
    }

    public AttackData.AttackResultType getAttackType() {
        return attackType;
    }

    public String getResultDescription() {
        return resultDescription;
    }
    
    public String getExplanation() {
        return explanation;
    }
}
