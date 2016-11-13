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
package gui.mainTop.options;

import gui.botLeft.PanelLeftComboBoxModel;
import gui.botLeft.PanelLeftInsertions;
import gui.categorizer.CategorizerManager;
import gui.categorizer.CategorizerUi;
import gui.lists.ListManagerUi;
import gui.networking.NetworkerInfoUi;
import gui.reporter.ReporterUi;
import gui.session.SessionManager;
import gui.session.SessionManagerUi;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author dobin
 */
public class PanelTopOptionsPopup implements ActionListener {
    private JPopupMenu menuMain;

    private JMenuItem menuItemSession;
    private JMenuItem menuItemReporter;
    private JMenuItem menuItemCategorizer;
    private JMenuItem menuItemLists;
    private JMenuItem menuItemOptions;
    
    private ReporterUi reporterUi;
    private ListManagerUi listManagerUi;
    private CategorizerUi categorizerUi;
    private SessionManagerUi sessionUi;
    private FrameOptionsOrig frameOptions;

    
    public PanelTopOptionsPopup() {
        initMenu();
        initWindows();
    }
    
    public FrameOptionsOrig getOptionsOrig() {
        return frameOptions;
    }
    
    private void initMenu() {
        menuMain = new JPopupMenu("Options");
        
        menuItemSession = new JMenuItem("Session");
        menuItemReporter = new JMenuItem("Reporter");
        menuItemCategorizer = new JMenuItem("Categorizer");
        menuItemLists = new JMenuItem("Lists");
        menuItemOptions = new JMenuItem("Options");
        
        menuItemSession.addActionListener(this);
        menuItemReporter.addActionListener(this);
        menuItemCategorizer.addActionListener(this);
        menuItemLists.addActionListener(this);
        menuItemOptions.addActionListener(this);
        
        menuMain.add(menuItemSession);
        menuMain.add(menuItemReporter);
        menuMain.add(menuItemCategorizer);
        menuMain.add(menuItemLists);
        menuMain.add(menuItemOptions);
    }
    
    private void initWindows() {
        // Options
        frameOptions = new FrameOptionsOrig();
        WindowListener optionsWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frameOptions.setVisible(false);
            }
        };
        frameOptions.addWindowListener(optionsWindowListener);
        
        
        // Categorizer
        categorizerUi = CategorizerManager.getInstance().getCategorizerUi();
        WindowListener categorizerWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                categorizerUi.setVisible(false);
                categorizerUi.save();
            }
        };
        categorizerUi.addWindowListener(categorizerWindowListener);
        
        // SessionUi
        sessionUi = SessionManager.getInstance().getSessionManagerUi();
        WindowListener sessionUiWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sessionUi.setVisible(false);
            }
        };
        sessionUi.addWindowListener(sessionUiWindowListener);
        

        
        // Reporter
        reporterUi = new ReporterUi();
        WindowListener reporterUiWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                reporterUi.setVisible(false);
            }
        };
        reporterUi.addWindowListener(reporterUiWindowListener);
        
        // Listmanager
        listManagerUi = new ListManagerUi();
        WindowListener listUiWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                listManagerUi.save();
                listManagerUi.setVisible(false);
            }
        };
        listManagerUi.addWindowListener(listUiWindowListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemSession) {
            sessionUi.setVisible(true);
        } else if (e.getSource() == menuItemReporter) {
            reporterUi.setVisible(true);
        } else if (e.getSource() == menuItemCategorizer) {
            categorizerUi.setVisible(true);
        } else if (e.getSource() == menuItemLists) {
            listManagerUi.setVisible(true);
        } else if (e.getSource() == menuItemOptions) {
            frameOptions.setVisible(true);
        }
    }
    
    public JPopupMenu getPopupMenu() {
        return menuMain;
    }
}
