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
package util;

import gui.session.SessionUser;
import gui.categorizer.CategoryEntry;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.fife.ui.rsyntaxtextarea.Theme;

/**
 *
 * 
 */
public class UiUtil {

    private static Theme theme = null;
    
    public static Theme getTheme() {
        if (theme == null) {
            try {
                InputStream in = UiUtil.class.getResourceAsStream("/resources/BurpTheme.xml");
                theme = Theme.load(in);
            } catch (IOException ex) {
                BurpCallbacks.getInstance().print("error loading theme: " + ex.getLocalizedMessage());
            }
        }
        return theme;
    }
    
    public static void storeTableDimensions(JTable table, Object o) {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());
        TableColumnModel columns = table.getColumnModel();

        for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
            TableColumn column = columns.getColumn(i);
            int w = column.getPreferredWidth();
            
            pref.putInt(Integer.toString(i), w);
        }
    }

    public static void restoreTableDimensions(JTable table, Object o) {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());
        TableColumnModel columns = table.getColumnModel();
        

        for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
            TableColumn column = columns.getColumn(i);
            int w = pref.getInt(Integer.toString(i), 10);
    

            column.setPreferredWidth(w);
        }
    }
    
    
    /** Store location & size of UI */
    public static void storeFrameDimensions(Frame f, Object o) throws Exception {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());
       
        // restore the frame from 'full screen' first!
        f.setExtendedState(Frame.NORMAL);
        Rectangle r = f.getBounds();
        
        int x = (int)r.getX();
        int y = (int)r.getY();
        int w = (int)r.getWidth();
        int h = (int)r.getHeight();
        
        pref.putInt("x", x);
        pref.putInt("y", y);
        pref.putInt("w", w);
        pref.putInt("h", h);
    }

    /** Restore location & size of UI */
    public static void restoreFrameDimensions(Frame f, Object o) throws IOException {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());

        int x = pref.getInt("x", 0);
        int y = pref.getInt("y", 0);
        int w = pref.getInt("w", 0);
        int h = pref.getInt("h", 0);

        Rectangle r = new Rectangle(x,y,w,h);
        f.setBounds(r);
    }
    
    // http://www.chka.de/swing/table/cell-sizes.html
    public static void calcColumnWidths(JTable table) {
        JTableHeader header = table.getTableHeader();

        TableCellRenderer defaultHeaderRenderer = null;

        if (header != null) {
            defaultHeaderRenderer = header.getDefaultRenderer();
        }

        TableColumnModel columns = table.getColumnModel();
        TableModel data = table.getModel();

        int margin = columns.getColumnMargin(); // only JDK1.3
        int rowCount = data.getRowCount();
        int totalWidth = 0;

        for (int i = columns.getColumnCount() - 1; i >= 0; --i) {
            TableColumn column = columns.getColumn(i);

            int columnIndex = column.getModelIndex();
            int width = -1;

            TableCellRenderer h = column.getHeaderRenderer();
            if (h == null) {
                h = defaultHeaderRenderer;
            }

            if (h != null) // Not explicitly impossible
            {
                Component c = h.getTableCellRendererComponent(table, column.getHeaderValue(),
                        false, false, -1, i);
                width = c.getPreferredSize().width;
            }

            for (int row = rowCount - 1; row >= 0; --row) {
                TableCellRenderer r = table.getCellRenderer(row, i);
                Component c = r.getTableCellRendererComponent(table,
                        data.getValueAt(row, columnIndex),
                        false, false, row, i);
                width = Math.max(width, c.getPreferredSize().width);
            }

            if (width >= 0) {
                column.setPreferredWidth(width + margin); // <1.3: without margin
            } else
            ; // ???

            totalWidth += column.getPreferredWidth();
        }
    }

    public static void storeSplitLocation(JSplitPane jSplitPane1, Object o) {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());
        pref.putInt(("location"), jSplitPane1.getDividerLocation());
    }

    public static void restoreSplitLocation(JSplitPane jSplitPane1, Object o) {
        Preferences pref = Preferences.userRoot().node(o.getClass().getName());
        jSplitPane1.setDividerLocation(pref.getInt(("location"), jSplitPane1.getDividerLocation()));
    }

    public static void storeSessions(LinkedList<SessionUser> sessionUsers) {
        Preferences pref = Preferences.userRoot().node("SessionManagerUsers");
        try {
            pref.clear();
        } catch (BackingStoreException ex) {
            Logger.getLogger(UiUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for(SessionUser u: sessionUsers) {
            pref.put(u.getName(), u.getValue());
        }
    }
    
    public static void restoreSessions(LinkedList<SessionUser> sessionUsers) {
        Preferences pref = Preferences.userRoot().node("SessionManagerUsers");
    
        String[] children = null;
        try {
            children = pref.keys();
            for (String s : children) {
                String value = pref.get(s, "");
                sessionUsers.add(new SessionUser(s, value));
            }            
        } catch (BackingStoreException ex) {
            Logger.getLogger(UiUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public static void storeSessionData(String text) {
        Preferences pref = Preferences.userRoot().node("SessionManagerData");
        pref.put("SessionVarName", text);
    }

    public static void restoreSessionData(JTextField textfieldSession) {
        Preferences pref = Preferences.userRoot().node("SessionManagerData");
        
        String s = pref.get("SessionVarName", "jsessionid");
        textfieldSession.setText(s);
    }

    public static void restoreCategories(LinkedList<CategoryEntry> categoryEntries) {
        Preferences pref = Preferences.userRoot().node("CategoryEntries");
    
        String[] children = null;
        try {
            children = pref.keys();
            for (String s : children) {
                String value = pref.get(s, "");
                categoryEntries.add(new CategoryEntry(s, value));
            }            
        } catch (BackingStoreException ex) {
            Logger.getLogger(UiUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void storeCategories(LinkedList<CategoryEntry> categoryEntries) {
        Preferences pref = Preferences.userRoot().node("CategoryEntries");
        try {
            pref.clear();
        } catch (BackingStoreException ex) {
            Logger.getLogger(UiUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for(CategoryEntry c: categoryEntries) {
            pref.put(c.getTag(), c.getRegex());
        }        
    }


}
