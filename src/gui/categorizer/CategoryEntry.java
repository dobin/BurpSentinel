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
package gui.categorizer;

import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

/**
 *
 * @author dobin
 */
public class CategoryEntry {
    private String name;
    private String regex;
        
    private Boolean checkboxEnabled;
    private Color myColor = Color.red;
    
    public CategoryEntry(String name, String regex) {
        this.name = name;
        this.regex = regex;
        
        checkboxEnabled = true;
    }
    
    public String getTag() {
        return name;
    }
    
    public String getRegex() {
        return regex;
    }
    
    public void setTag(String tag) {
        this.name = tag;
    }
    
    public void setRegex(String regex) {
        this.regex = regex;
    }


    Object isEnabled() {
        return checkboxEnabled;
    }


    Object getColor() {
        return myColor;
    }
    
}
