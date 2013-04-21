/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.session.categorizer;

import gui.session.SessionManagerUi;
import gui.session.SessionUser;
import java.util.LinkedList;
import java.util.Observable;

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

    public CategorizerManager() {
        categorizerManagerUi = new CategorizerUi();
    }

    public void show() {
        categorizerManagerUi.setVisible(true);
    }
    
    
    public LinkedList<CategoryEntry> getCategories() {
        return categorizerManagerUi.getCategories();
    }
    
}
