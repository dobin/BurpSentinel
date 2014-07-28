/*
 * Copyright (C) 2014 DobinRutishauser@broken.ch
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
package gui.categorizer.model;

import java.util.LinkedList;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class Category {
    private String name;
    private LinkedList<CategoryEntry> categoryEntries;
    
    public Category() {
        name = "";
        this.categoryEntries = new LinkedList<CategoryEntry>();
    }
    
    public Category(String name) {
        this.name = name;
        this.categoryEntries = new LinkedList<CategoryEntry>();
    }
    
    public Category(String name, LinkedList<CategoryEntry> categoryEntries) {
        this.name = name;
        this.categoryEntries = categoryEntries;
    }
    
    public String getName() {
        return name;
    }
    
    public LinkedList<CategoryEntry> getCategoryEntries() {
        return categoryEntries;
    }
    
    
    // Todo: remove
    public void setCategoryEntries(LinkedList<CategoryEntry> entries) {
        this.categoryEntries = entries;
    }
}
