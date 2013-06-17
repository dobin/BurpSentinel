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

import gui.SentinelMainUi;
import gui.lists.ListManagerList;
import gui.mainBot.PanelBotUi;
import gui.mainTop.PanelTopPopup;
import gui.networking.Networker;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import model.SentinelHttpMessage;
import model.SentinelHttpMessageAtk;
import model.SentinelHttpMessageOrig;
import model.SentinelHttpParam;
import util.BurpCallbacks;
import util.UiUtil;

/**
 *
 * @author unreal
 */
public class PanelLeftUi extends javax.swing.JPanel  {
    private PanelBotUi panelParent;
    private SentinelHttpMessageOrig origHttpMessage;
    private PanelLeftTableModel tableModel;
    private PanelLeftComboBoxModel sessionComboBoxModel;
    private JComboBox comboBoxSession;
    private PanelLeftPopup popup;
    private int selectedRow = -1;
    
    /**
     * Creates new form RequestConfigForm
     */
    public PanelLeftUi() {
        tableModel = new PanelLeftTableModel();
        sessionComboBoxModel = new PanelLeftComboBoxModel();
        comboBoxSession = new JComboBox();
        comboBoxSession.setModel(sessionComboBoxModel);
        
        initComponents();

        int width = 50;
        tableMessages.getColumnModel().getColumn(0).setMaxWidth(40);
        tableMessages.getColumnModel().getColumn(0).setMinWidth(40);
        
        tableMessages.getColumnModel().getColumn(1).setMaxWidth(90);
        tableMessages.getColumnModel().getColumn(1).setMinWidth(90);
        
        tableMessages.getColumnModel().getColumn(4).setMaxWidth(width);
        tableMessages.getColumnModel().getColumn(4).setMinWidth(width);
        tableMessages.getColumnModel().getColumn(5).setMaxWidth(width);
        tableMessages.getColumnModel().getColumn(5).setMinWidth(width);
        tableMessages.getColumnModel().getColumn(6).setMaxWidth(width);
        tableMessages.getColumnModel().getColumn(6).setMinWidth(width);
        tableMessages.getColumnModel().getColumn(7).setMaxWidth(width);
        tableMessages.getColumnModel().getColumn(7).setMinWidth(width);

        
        tableMessages.setAutoCreateRowSorter(true);
        UiUtil.restoreSplitLocation(jSplitPane1, this);
        UiUtil.restoreTableDimensions(tableMessages, this);

        // 
        TableColumn sportColumn = tableMessages.getColumnModel().getColumn(3);
        PanelLeftTableCellRenderer renderer = new PanelLeftTableCellRenderer(comboBoxSession);
        sportColumn.setCellRenderer(renderer);
        sportColumn.setCellEditor(new PanelLeftTableCellEditor(comboBoxSession));
        
        popup = new PanelLeftPopup(this);
        
        // Add mouse listener for on-row popup menu
        tableMessages.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                BurpCallbacks.getInstance().print("BBB1");
                if (popup.getPopup().isPopupTrigger(e)) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    selectedRow = row;
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }

                    BurpCallbacks.getInstance().print("BBB2");
                    popup.refreshIndex();
                    popup.getPopup().show(e.getComponent(), e.getX(), e.getY());
                    BurpCallbacks.getInstance().print("BBB3");
                }
            }
        });    
    }
   
    
    SentinelHttpParam getSelectedHttpParam() {
        return origHttpMessage.getReq().getParam(selectedRow);
    }
    
    
    private TableModel getTableModel() {
        return tableModel;
    }
    
    
/*    
                            int newSelectedRow = lsm.getMinSelectionIndex();
                        // Only update if differ
                        if (newSelectedRow != currentSelectedRow) {
                            viewHttpMessage(currentSelectedRow);
                        }
                        currentSelectedRow = newSelectedRow;
                        tableMessages.getSelectionModel().setSelectionInterval(currentSelectedRow, currentSelectedRow);
*/
    
    
    
    public void setMessage(SentinelHttpMessageOrig message) {
        this.origHttpMessage = message;
        tableModel.setMessage(origHttpMessage);
        panelViewMessage.setHttpMessage(origHttpMessage);
        textfieldUrl.setText(message.getReq().getUrl().toString());
        textfieldUrl.setToolTipText(message.getReq().getUrl().toString());
        textfieldUrl.setBackground( Color.lightGray);
        textfieldUrl.setCaretPosition(0);
    }
    
  
    /*
     * Add Attack Message
     * 
     * An attack thread did generate (and send/receive) a new message
     * add to right panel (call our parent, botpanelui, to do this)
     */
    public void addAttackMessage(SentinelHttpMessageAtk attackMessage) {
        panelParent.addAttackMessage(attackMessage);
    }

    
    /*
     * Click on popup menu with special attacklist attack list
     */
    void attackSelectedParam(LinkedList<SentinelHttpParam> attackHttpParams) {
        // add httpmessage attacks to send queue
        Networker.getInstance().addNewMessages(
                attackHttpParams, 
                origHttpMessage, 
                this, 
                checkboxFollowRedirect.isSelected(), 
                //(String) comboboxMainSession.getSelectedItem()
                (String) SentinelMainUi.getMainUi().getPanelTop().getSelectedSession()
                );
    }
    
    /*
     * Click on "Go"
     * Attacks current httpmessage with all selected attacks
     */
    private void attackRessource() {
        // Set session options
        if (comboBoxSession.getSelectedIndex() > 0) {
            tableModel.setSessionAttackMessage(true, (String) comboBoxSession.getSelectedItem());
        }
        
        // Transfer UI attack ticks to HttpMessage attacks
        LinkedList<SentinelHttpParam> attackHttpParams = tableModel.createChangeParam();

        // reset UI attack ticks
        tableModel.resetAttackSelection();
        comboBoxSession.setSelectedIndex(0);
        
        // add httpmessage attacks to send queue
        Networker.getInstance().addNewMessages(
                attackHttpParams, 
                origHttpMessage, 
                this, 
                checkboxFollowRedirect.isSelected(), 
                //(String) comboboxMainSession.getSelectedItem()
                (String) SentinelMainUi.getMainUi().getPanelTop().getSelectedSession()
                );
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        panelTop = new javax.swing.JPanel();
        panelTopHeader = new javax.swing.JPanel();
        buttonAttack = new javax.swing.JButton();
        checkboxFollowRedirect = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        textfieldUrl = new javax.swing.JTextField();
        panelTopBody = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableMessages = new javax.swing.JTable();
        panelBottom = new javax.swing.JPanel();
        panelViewMessage = new gui.viewMessage.PanelViewMessageUi();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        buttonAttack.setText("Go");
        buttonAttack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAttackActionPerformed(evt);
            }
        });

        checkboxFollowRedirect.setSelected(true);
        checkboxFollowRedirect.setText("Follow Redirects");

        jPanel1.setLayout(new java.awt.BorderLayout());

        textfieldUrl.setEditable(false);
        textfieldUrl.setText("jTextField1");
        jPanel1.add(textfieldUrl, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout panelTopHeaderLayout = new javax.swing.GroupLayout(panelTopHeader);
        panelTopHeader.setLayout(panelTopHeaderLayout);
        panelTopHeaderLayout.setHorizontalGroup(
            panelTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTopHeaderLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkboxFollowRedirect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonAttack, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelTopHeaderLayout.setVerticalGroup(
            panelTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopHeaderLayout.createSequentialGroup()
                .addGroup(panelTopHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonAttack)
                    .addComponent(checkboxFollowRedirect))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tableMessages.setModel(getTableModel());
        tableMessages.setToolTipText("");
        tableMessages.setRowHeight(20);
        tableMessages.setSelectionBackground(new java.awt.Color(255, 205, 129));
        tableMessages.setSelectionForeground(new java.awt.Color(0, 0, 0));
        jScrollPane1.setViewportView(tableMessages);

        javax.swing.GroupLayout panelTopBodyLayout = new javax.swing.GroupLayout(panelTopBody);
        panelTopBody.setLayout(panelTopBodyLayout);
        panelTopBodyLayout.setHorizontalGroup(
            panelTopBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
        );
        panelTopBodyLayout.setVerticalGroup(
            panelTopBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelTopLayout = new javax.swing.GroupLayout(panelTop);
        panelTop.setLayout(panelTopLayout);
        panelTopLayout.setHorizontalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelTopHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelTopBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelTopLayout.setVerticalGroup(
            panelTopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopLayout.createSequentialGroup()
                .addComponent(panelTopHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(panelTopBody, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setTopComponent(panelTop);

        javax.swing.GroupLayout panelBottomLayout = new javax.swing.GroupLayout(panelBottom);
        panelBottom.setLayout(panelBottomLayout);
        panelBottomLayout.setHorizontalGroup(
            panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
            .addGroup(panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelViewMessage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE))
        );
        panelBottomLayout.setVerticalGroup(
            panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 147, Short.MAX_VALUE)
            .addGroup(panelBottomLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelViewMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
        );

        jSplitPane1.setBottomComponent(panelBottom);

        jPanel2.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonAttackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAttackActionPerformed
        attackRessource();
    }//GEN-LAST:event_buttonAttackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAttack;
    private javax.swing.JCheckBox checkboxFollowRedirect;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelTop;
    private javax.swing.JPanel panelTopBody;
    private javax.swing.JPanel panelTopHeader;
    private gui.viewMessage.PanelViewMessageUi panelViewMessage;
    private javax.swing.JTable tableMessages;
    private javax.swing.JTextField textfieldUrl;
    // End of variables declaration//GEN-END:variables
    
    public void setPanelParent(PanelBotUi aThis) {
        this.panelParent = aThis;
        panelViewMessage.setLinkManager(panelParent.getLinkManager());
    }

    public void storeUiPrefs() {
        UiUtil.storeSplitLocation(jSplitPane1, this);
        UiUtil.storeTableDimensions(tableMessages, this);
    }
    
    public void externalUpdateUi() {
        sessionComboBoxModel.myupdate();
        tableModel.fireTableDataChanged();
    }

    void myUpdateUI() {
        this.updateUI();
        panelParent.updateUI();
    }
    
    public SentinelHttpMessage getOrigHttpMessage() {
        return origHttpMessage;
    }

}
