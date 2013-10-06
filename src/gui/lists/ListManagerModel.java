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
package gui.lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.BurpCallbacks;
import util.UiUtil;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class ListManagerModel {
    
    private LinkedList<ListManagerList> myLists;
    
    public ListManagerModel() {
        myLists = new LinkedList<ListManagerList>();
    }

    void addNewList() {
        ListManagerList list = new ListManagerList();
        myLists.add(list);
    }
    
    public LinkedList<ListManagerList> getList() {
        return (LinkedList<ListManagerList>) myLists.clone();
    }
    
    public ListManagerList getList(int index) {
        return myLists.get(index);
    }
    
    int getCount() {
        return myLists.size();
    }
  
    
    void readConfig() {
        UiUtil.restoreAttackLists(myLists);
    }

    void writeConfig() {
        UiUtil.storeAttackLists(myLists);
    }

    void delList(int currentSelectedRow) {
        myLists.remove(currentSelectedRow);
    }
    
    
      
    public void initFuzzDB() {
        boolean isFresh = true;
        //for(ListManagerList list: myLists) {
        //    if (list.isFuzzDB()) {
        //        isFresh = false;
        //    }
       // }
        for(ListManagerList list: myLists) {
            if (list.getName().contains(".fuzz")) {
                isFresh = false;
            }
        }
            
        
        if (isFresh) {
            loadFuzzDB();
        }
        
    }
    
    private void loadFuzzDB() {
        String[] fileNames = {  "GenericBlind.fuzz.txt", "MSSQL.fuzz.txt", "MSSQL_blind.fuzz.txt",
                                "MySQL.fuzz.txt", "MySQL_MSSQL.fuzz.txt", "oracle.fuzz.txt", "xss-rsnake.txt" };
        
        for(String fileName: fileNames) {
            InputStream is = getClass().getResourceAsStream("/resources/fuzzdb/" + fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            ListManagerList list = new ListManagerList(fileName, "");
            
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    list.addLine(line);
                }
            } catch (IOException ex) {
                BurpCallbacks.getInstance().print(ex.toString());
                Logger.getLogger(ListManagerModel.class.getName()).log(Level.SEVERE, null, ex);
            }
     
            myLists.add(list);
        }
    }
    
    
}
