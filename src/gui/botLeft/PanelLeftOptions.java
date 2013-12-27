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
package gui.botLeft;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import util.UiUtil;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class PanelLeftOptions implements ActionListener {

    private JPopupMenu menuMain;
    // Menus
    private JMenu menuFollowRedirects;
    private JMenuItem menuItemEnableRedirect;
    private JMenuItem menuItemDisableRedirect;
    private JMenu menuInsert;
    private JMenuItem menuItemReplace;
    private JMenuItem menuItemInsertLeft;
    private JMenuItem menuItemInsertRight;
    // Options
    private boolean optionEnableRedirect;


    public static enum InsertPositions {
        REPLACE,
        LEFT,
        RIGHT,};
    private InsertPositions optionInsertPosition;

    public PanelLeftOptions() {
        init();
    }

    private void init() {
        // Options
        optionEnableRedirect = UiUtil.restorePanelLeftOptionRedirect();
        optionInsertPosition = UiUtil.restorePanelLeftOptionPosition();

        // Menu
        menuMain = new JPopupMenu("Options");

        menuFollowRedirects = new JMenu("Follow Redirects: ");
        menuItemDisableRedirect = new JMenuItem("Disable");
        menuItemEnableRedirect = new JMenuItem("Enable");
        menuItemDisableRedirect.addActionListener(this);
        menuItemEnableRedirect.addActionListener(this);
        menuFollowRedirects.add(menuItemEnableRedirect);
        menuFollowRedirects.add(menuItemDisableRedirect);

        menuInsert = new JMenu("Insert payloads at: ");
        menuItemReplace = new JMenuItem("Replace");
        menuItemInsertLeft = new JMenuItem("Insert Left");
        menuItemInsertRight = new JMenuItem("Insert Right");
        menuItemReplace.addActionListener(this);
        menuItemInsertLeft.addActionListener(this);
        menuItemInsertRight.addActionListener(this);
        menuInsert.add(menuItemReplace);
        menuInsert.add(menuItemInsertLeft);
        menuInsert.add(menuItemInsertRight);

        menuMain.add(menuFollowRedirects);
        menuMain.add(menuInsert);

        refresh();
    }

    private void refresh() {
        if (optionEnableRedirect) {
            menuItemEnableRedirect.setEnabled(false);
            menuItemDisableRedirect.setEnabled(true);
        } else {
            menuItemEnableRedirect.setEnabled(true);
            menuItemDisableRedirect.setEnabled(false);
        }


        switch (optionInsertPosition) {
            case REPLACE:
                menuItemReplace.setEnabled(false);
                menuItemInsertLeft.setEnabled(true);
                menuItemInsertRight.setEnabled(true);
                break;
            case LEFT:
                menuItemReplace.setEnabled(true);
                menuItemInsertLeft.setEnabled(false);
                menuItemInsertRight.setEnabled(true);
                break;
            case RIGHT:
                menuItemReplace.setEnabled(true);
                menuItemInsertLeft.setEnabled(true);
                menuItemInsertRight.setEnabled(false);
                break;
        }
    }

    void storeUiPrefs() {
        UiUtil.storePanelLeftOptionRedirect(optionEnableRedirect);
        UiUtil.storePanelLeftOptionPosition(optionInsertPosition);
    }

    public JPopupMenu getPopupMenu() {
        return menuMain;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemEnableRedirect) {
            optionEnableRedirect = true;
        } else if (e.getSource() == menuItemDisableRedirect) {
            optionEnableRedirect = false;
        }

        if (e.getSource() == menuItemReplace) {
            optionInsertPosition = InsertPositions.REPLACE;
        } else if (e.getSource() == menuItemInsertLeft) {
            optionInsertPosition = InsertPositions.LEFT;
        } else if (e.getSource() == menuItemInsertRight) {
            optionInsertPosition = InsertPositions.RIGHT;
        }

        refresh();
    }

    boolean getOptionRedirect() {
        return optionEnableRedirect;
    }
    
    InsertPositions getOptionInsertPosition() {
        return optionInsertPosition;
    }
}
