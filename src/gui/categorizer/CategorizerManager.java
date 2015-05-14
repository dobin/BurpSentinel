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

import gui.categorizer.model.Category;
import gui.categorizer.model.CategoryEntry;
import gui.categorizer.model.ResponseCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import util.BurpCallbacks;
import util.SettingsManager;

/**
 *
 * @author dobin
 */
public class CategorizerManager extends Observable {
    static CategorizerManager categorizerManager;
    static public CategorizerManager getInstance() {
        if (categorizerManager == null) {
            categorizerManager = new CategorizerManager();
        }

        return categorizerManager;
    }
    
    private class StaticCategoriesIndexEntries {
        private String fileName;
        private String tagName;
        
        public StaticCategoriesIndexEntries(String fileName, String tagName) {
            this.fileName = fileName;
            this.tagName = tagName;
        }
                
        public String getFileName() {
            return fileName;
        }
        public String getTagName() {
            return tagName;
        }
    }

    private CategorizerUi categorizerManagerUi;
    private LinkedList<Category> categories;
    
    public CategorizerManager() {
        categories = new LinkedList<Category>();
        
        initFileCategories();
        loadCategories();
        
        categorizerManagerUi = new CategorizerUi(this);
    }
    
    public CategorizerUi getCategorizerUi() {
        return categorizerManagerUi;
    }

    public void show() {
        categorizerManagerUi.setVisible(true);
    }
    
    
    public Category getCategoryList(String name) {
        for(Category c: categories) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }
    
    public LinkedList<ResponseCategory> categorize(byte[] input) {
        LinkedList<ResponseCategory> newCategories = new LinkedList<ResponseCategory>();
        
        if (input == null || input.length <= 0) {
            return newCategories;
        }
    
        for(Category c: categories) {
            for(CategoryEntry entry: c.getCategoryEntries()) {
                newCategories.addAll(scanForRegex(entry, input));
            }
        }
      
        return newCategories;
    }

    
    private LinkedList<ResponseCategory> scanForRegex(CategoryEntry entry, byte[] input) {
        LinkedList<ResponseCategory> categories = new LinkedList<ResponseCategory>();

        String pattern = entry.getRegex();
        byte[] bytePattern = BurpCallbacks.getInstance().getBurp().getHelpers().stringToBytes(pattern);
        
        int idx = BurpCallbacks.getInstance().getBurp().getHelpers().indexOf(input, bytePattern, true, 0, input.length);
        if (idx >= 0) {
            ResponseCategory c = new ResponseCategory(entry, pattern, "Found: " + pattern);
            categories.add(c);            
        }

        return categories;
    }
    
    public void saveCategory(Category c) {
        SettingsManager.storeCategory(c);
    }
    
    private void loadCategories() {
        LinkedList<String> categoryNames = new LinkedList<String>();
        categoryNames.push("user");
        categoryNames.push("error");
        categoryNames.push("sqlerr");
        
        for (String categoryName: categoryNames) {
            Category category = new Category(categoryName);
            SettingsManager.restoreCategories(category);
            categories.push(category);
        }
        
    }
    
    private void initFileCategories() {
        // FIXME Removed
        //if (SettingsManager.getListInitState()) {
        //    return;
        //}
        
        List<StaticCategoriesIndexEntries> staticCategoriesIndex = new ArrayList<StaticCategoriesIndexEntries>();
        staticCategoriesIndex.add(new StaticCategoriesIndexEntries("errors.txt", "error"));
        staticCategoriesIndex.add(new StaticCategoriesIndexEntries("sqlerrors.txt", "sqlerr"));
        
        Category uc = new Category("user");
        SettingsManager.storeCategory(uc);
        
        for(StaticCategoriesIndexEntries staticCategory: staticCategoriesIndex) {
            Category c = new Category(staticCategory.tagName);
            LinkedList<CategoryEntry> staticCategoryList = new LinkedList<CategoryEntry>();
            InputStream is = getClass().getResourceAsStream("/resources/categories/" + staticCategory.getFileName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    String regex = line;
                    CategoryEntry categoryEntry = new CategoryEntry(staticCategory.getTagName(), regex);
                    
                    staticCategoryList.add(categoryEntry);
                }
            } catch (IOException ex) {
                BurpCallbacks.getInstance().print(ex.toString());
            } 
     
            c.setCategoryEntries(staticCategoryList);
            SettingsManager.storeCategory(c);
        }
    }
    

    void signalModelUpdate() {
        this.setChanged();
        this.notifyObservers();
    }
     
}
