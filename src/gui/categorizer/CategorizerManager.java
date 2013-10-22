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

import gui.lists.ListManagerModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.BurpCallbacks;

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

    private CategorizerUi categorizerManagerUi;
    //private HashMap<String, LinkedList<CategoryEntry>> staticCategories = new HashMap<String, LinkedList<CategoryEntry>>();
    private LinkedList< LinkedList<CategoryEntry>> staticCategories = new LinkedList<LinkedList<CategoryEntry>>();

    public CategorizerManager() {
        categorizerManagerUi = new CategorizerUi();
        
        loadStaticCategories();
    }
    
    public CategorizerUi getCategorizerUi() {
        return categorizerManagerUi;
    }

    public void show() {
        categorizerManagerUi.setVisible(true);
    }
    
    
    public LinkedList<CategoryEntry> getCategories() {
        LinkedList<CategoryEntry> categories = new LinkedList<CategoryEntry>();
        
        categories.addAll(categorizerManagerUi.getCategories());

        for(LinkedList<CategoryEntry> list: staticCategories) {
            categories.addAll(list);
        }
        
        /*for(Map.Entry entry: staticCategories.entrySet()) {
            BurpCallbacks.getInstance().print(entry.getValue().toString());
            LinkedList<CategoryEntry> staticCategoriesEntry = (LinkedList<CategoryEntry>) entry.getValue();
            categories.addAll(staticCategoriesEntry);
        }*/
        
        return categories;
    }
    
    public LinkedList<ResponseCategory> categorize(String input) {
        LinkedList<ResponseCategory> categories = new LinkedList<ResponseCategory>();
        
        if (input == null || input.length() <= 0) {
            return categories;
        }
        
        LinkedList<CategoryEntry> categoryEntries = getCategories();
        for(CategoryEntry entries: categoryEntries) {
            Pattern pattern = Pattern.compile(entries.getRegex());
            Matcher matcher = pattern.matcher(input);
            
            if (matcher.find()) {
                BurpCallbacks.getInstance().print("A: " + matcher.group());
                ResponseCategory c = new ResponseCategory(entries.getTag(), matcher.group());
                categories.add(c);
            }
        }
        
        return categories;
    }
    
    private void loadStaticCategories() {
        String[] fileNames = { "errors", "sqlerrors"  };
        
        LinkedList<CategoryEntry> staticCategoryList;
        
        for(String fileName: fileNames) {
            staticCategoryList = new LinkedList<CategoryEntry>();
            InputStream is = getClass().getResourceAsStream("/resources/categories/" + fileName + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    //BurpCallbacks.getInstance().print("ADD: " + line + " as " + fileName);
                    String regex = line;
                    CategoryEntry categoryEntry = new CategoryEntry(fileName, Pattern.quote(regex));
                    
                    staticCategoryList.add(categoryEntry);
                }
            } catch (IOException ex) {
                BurpCallbacks.getInstance().print(ex.toString());
                Logger.getLogger(ListManagerModel.class.getName()).log(Level.SEVERE, null, ex);
            }
     
            staticCategories.add(staticCategoryList);
            //staticCategories.put(fileName, staticCategoryList);
        }
        
        
    }
     
}
