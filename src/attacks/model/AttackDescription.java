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
package attacks.model;

import attacks.model.AttackMain.AttackTypes;

/**
 *
 * @author dobin
 */
public class AttackDescription {
    private AttackTypes attackType;
    private String description;
    private Boolean enabled;
    
    AttackDescription(AttackTypes attackType, String description) {
        this.attackType = attackType;
        this.description = description;
        enabled = false;
    }
    
    public String getShort() {
        return attackType.toString();
    }
    
    public AttackTypes getAttackType() {
        return attackType;
    }
    
    public String getDescription() {
        return description;
    }
    
    
    public Boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
}
