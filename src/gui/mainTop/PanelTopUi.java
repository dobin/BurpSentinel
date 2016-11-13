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
package gui.mainTop;

import gui.SentinelMainUi;
import gui.botLeft.PanelLeftComboBoxModel;
import gui.mainTop.options.PanelTopOptionsPopup;
import gui.networking.NetworkerInfoUi;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.ComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import model.SentinelHttpMessageOrig;
import util.SettingsManager;

/**
 * Display all HttpMessage the user added to sentinel
 * 
 * @author Dobin
 */
public class PanelTopUi extends javax.swing.JPanel {
    private final PanelTopTableModel tableTopModel;
    private final PanelTopPopupTableHeader popupTableModel;
    
    private int currentSelectedRow = -1;
    
    private final PanelTopPopup popup;
    private NetworkerInfoUi networkerInfoUi;
    private final PanelTopOptionsPopup panelTopOptionsPopup;
    

    
    /**
     * Creates new form PanelTop
     */
    public PanelTopUi() {
        tableTopModel = new PanelTopTableModel(this);
        popup = new PanelTopPopup(this);
        panelTopOptionsPopup = new PanelTopOptionsPopup();
        initComponents();
        
        // TODO not necessary if in restoreTableDimensions default?
        tableAllMessages.setAutoCreateRowSorter(true);
        int width = 140;
        tableAllMessages.getColumnModel().getColumn(0).setMaxWidth(40);
        tableAllMessages.getColumnModel().getColumn(0).setMinWidth(40);
        
        tableAllMessages.getColumnModel().getColumn(1).setMaxWidth(60);
        tableAllMessages.getColumnModel().getColumn(1).setMinWidth(60);
        
        tableAllMessages.getColumnModel().getColumn(5).setMaxWidth(width);
        tableAllMessages.getColumnModel().getColumn(5).setMinWidth(width);
        
        tableAllMessages.getColumnModel().getColumn(6).setMaxWidth(width);
        tableAllMessages.getColumnModel().getColumn(6).setMinWidth(width);
        
        tableAllMessages.getColumnModel().getColumn(7).setMaxWidth(140);
        tableAllMessages.getColumnModel().getColumn(7).setMinWidth(140);
        
        tableAllMessages.getColumnModel().getColumn(8).setMaxWidth(140);
        tableAllMessages.getColumnModel().getColumn(8).setMinWidth(140);
        
        SettingsManager.restoreTableDimensions(tableAllMessages, this);
        
        // hide some rows
        popupTableModel = new PanelTopPopupTableHeader(tableAllMessages);
        popupTableModel.hideColumn("Comment");
        popupTableModel.hideColumn("Interesting");
        popupTableModel.hideColumn("Session");
        popupTableModel.hideColumn("Created");
        
    }
    
    
    public void init() {
        ((PanelTopNetworkBtn)btnNetworking).init();
        
        // networker
        networkerInfoUi = new NetworkerInfoUi();
        WindowListener networkerInfoWindowListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                btnNetworking.setSelected(false);
                networkerInfoUi.setVisible(false);
            }
        };
        networkerInfoUi.addWindowListener(networkerInfoWindowListener);
        
        // Add row selection listener
        ListSelectionModel lsm = tableAllMessages.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            @Override
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
 
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                       //
                    } else {
                        // Get selected row and tell the main frame to show it 
                        // in the bottom frame
                        int oldSelected = currentSelectedRow;
                        currentSelectedRow = lsm.getMinSelectionIndex();
                        
                        if (currentSelectedRow != oldSelected) {
                            //setSelected();
                            SentinelMainUi.getMainUi().showMessage(tableTopModel.getMessageForRow(currentSelectedRow));
                        }
                    }
                }});
        
        // Add mouse listener for on-row popup menu
        tableAllMessages.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (popup.getPopup().isPopupTrigger(e)) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }

                    popup.getPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });    
    }
    
    
    public SentinelHttpMessageOrig getFirstMessage() {
        return tableTopModel.getMessageForRow(0);
    }
    
    void removeMessageAction() {
        SentinelMainUi.getMainUi().removeMessage(tableTopModel.getMessage(currentSelectedRow));
    }
    
    public void removeMessage(SentinelHttpMessageOrig removeMessage) {
        tableTopModel.removeMessage(removeMessage);
    }
    
    // This gets called from MainGui
    // If the user sends a new HttpMessage from Burp to Sentinel
    public void addMessage(SentinelHttpMessageOrig httpMessage) {
        tableTopModel.addMessage(httpMessage);
        tableAllMessages.scrollRectToVisible(tableAllMessages.getCellRect(tableTopModel.getRowCount() - 1, 0, true));
    }

    // Used for swing
    private TableModel getMessageTableModel() {
        return tableTopModel;
    }
    
    public void setSelected(SentinelHttpMessageOrig selectedMsg) {
        int msgIndex = -1;
        
        msgIndex = tableTopModel.getRowForMessage(selectedMsg);
        tableAllMessages.getSelectionModel().setSelectionInterval(msgIndex, msgIndex);
        this.currentSelectedRow = msgIndex;
    }
    
    public void setUpdateCurrentSelected() {
        tableAllMessages.getSelectionModel().setSelectionInterval(currentSelectedRow, currentSelectedRow);
    }    

    public PanelTopOptionsPopup getOptionsPopup() {
        return panelTopOptionsPopup;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tableAllMessages = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        btnNetworking = new PanelTopNetworkBtn();
        buttonOptions = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        tableAllMessages.setModel(getMessageTableModel());
        tableAllMessages.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        tableAllMessages.setRowHeight(20);
        tableAllMessages.setSelectionBackground(new java.awt.Color(255, 205, 129));
        tableAllMessages.setSelectionForeground(new java.awt.Color(0, 0, 0));
        tableAllMessages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tableAllMessages);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnNetworking.setText("Network");
        btnNetworking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNetworkingActionPerformed(evt);
            }
        });

        buttonOptions.setText("Options");
        buttonOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOptionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(buttonOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNetworking, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNetworking)
                    .addComponent(buttonOptions))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 204, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 24, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
   
    private void setRelativePos(JToggleButton btn, JFrame frame) {
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(btn);
        Point winPos = frame.getLocation();
        
        winPos.x -= (frame.getWidth() / 2);
        winPos.y += (frame.getHeight() / 2);
        frame.setLocation(winPos);     
    }
    
    private void btnNetworkingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNetworkingActionPerformed
        if (btnNetworking.isSelected()) {
            networkerInfoUi.start();
            setRelativePos(btnNetworking, networkerInfoUi);
            networkerInfoUi.setVisible(true);
        } else {
            networkerInfoUi.stop();
            networkerInfoUi.setVisible(false);
        }
    }//GEN-LAST:event_btnNetworkingActionPerformed

    private void buttonOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOptionsActionPerformed
        JPopupMenu menu = panelTopOptionsPopup.getPopupMenu();
        menu.show(buttonOptions, buttonOptions.getBounds().width, 0);
        menu.setVisible(true);
    }//GEN-LAST:event_buttonOptionsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnNetworking;
    private javax.swing.JButton buttonOptions;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tableAllMessages;
    // End of variables declaration//GEN-END:variables

    
    public void storeUiPrefs() {
        SettingsManager.storeTableDimensions(tableAllMessages, this);
    }

    public void reset() {
        tableTopModel.reset();
    }
    
}
