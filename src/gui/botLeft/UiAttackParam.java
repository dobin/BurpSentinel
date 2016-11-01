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
package gui.botLeft;

import attacks.model.AttackMain;
import model.SentinelHttpParam;

/**
 * Only used to collect some information from the UI to perform an attack,
 * so we dont have so many parameters.
 * 
 * @author dobin
 */
public class UiAttackParam {
    SentinelHttpParam param;
    AttackMain.AttackTypes attackType;
    String attackData;

    public UiAttackParam(SentinelHttpParam param, AttackMain.AttackTypes attackType, String attackData) {
        this.param = param;
        this.attackType = attackType;
        this.attackData = attackData;
    }
}
