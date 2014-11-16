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
package gui.botLeft;

import java.awt.event.MouseEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class TableHeaderTooltip extends JTableHeader {

    public TableHeaderTooltip(TableColumnModel model) {
        super(model);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int col = columnAtPoint(e.getPoint());
        int modelCol = getTable().convertColumnIndexToModel(col);
        
        switch(modelCol) {
            case 0:
                return "Parameter Type (GET, POST, Cookie, Path)";
            case 1:
                return "Name of parameter";
            case 2:
                return "Value of parameter";
            case 3:
                return "Attack XSS";
            case 4:
                return "Attack SQL-unsafe (with or, and, ...)";
            case 5:
                return "Attack SQL-safe";
            case 6:
                return "Attack other stuff";
        }
        
        return "";
    }
}
