/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package replayer.gui.ReplayerMain;

import burp.IHttpRequestResponse;
import burp.ITab;
import burp.MainUiInterface;
import gui.mainBot.PanelBotLinkManager;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import model.SentinelHttpMessage;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class ReplayerMainUi extends javax.swing.JPanel implements ITab, MainUiInterface {

    private int currentSelectedRow = 0;
    private ReplayerMainTableModel tableModel;
    private boolean virgin = true;
    private ReplayerMainTablePopup popup;
    private PanelBotLinkManager linkManager;
    private SentinelHttpMessage origHttpMessage;
    
    /**
     * Creates new form ReplayerMainUi
     */
    public ReplayerMainUi() {
        linkManager = new PanelBotLinkManager();
        tableModel = new ReplayerMainTableModel();
        initComponents();
        jSplitPane1.setDividerLocation(0.5f);
        
        panelViewMessageUiLeft.setLinkManager(linkManager);
        panelViewMessageUiRight.setLinkManager(linkManager);
        panelViewMessageUiLeft.setRequestEditor(true);
                
        popup = new ReplayerMainTablePopup(this);
        
        jTable1.getColumnModel().getColumn(0).setMaxWidth(40);
        jTable1.getColumnModel().getColumn(0).setMinWidth(40);
        
        jTable1.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
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

        // Add selection listener
        ListSelectionModel lsm = jTable1.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //
                } else {
                    // Get selected row and tell the main frame to show it 
                    // in the bottom frame
                    currentSelectedRow = lsm.getMinSelectionIndex();
                    viewMessage(currentSelectedRow);
                }
            }
        });
    }

    
    private TableModel getTableModel() {
        return tableModel;
    }
    
    @Override
    public void addNewMessage(IHttpRequestResponse iHttpRequestResponse) {
        origHttpMessage = new SentinelHttpMessage(iHttpRequestResponse);
        SentinelHttpMessage newHttpMessage = new SentinelHttpMessage(origHttpMessage);
        newHttpMessage.setParentHttpMessage(origHttpMessage);

        // Add to table
        tableModel.addHttpMessage(origHttpMessage);

        if (virgin) {
            // Set as main (initially)
            panelViewMessageUiRight.setHttpMessage(origHttpMessage);
            virgin = false;
        }

        // Add to edit window
        //panelViewMessageUiLeft.setHttpMessage(newHttpMessage);
        
        //this.updateUI();
        viewMessage(0);
    }

    void setSelectedMessageAsOriginal() {
        BurpCallbacks.getInstance().print("Selected: " + jTable1.getSelectedRow());
    }
    
    private void sendMessage() {
        String s = panelViewMessageUiLeft.getRequestContent();
        
        if (s != null) {
            //SentinelHttpMessage newMessage = new SentinelHttpMessage(s, origHttpMessage.getHttpService());
            SentinelHttpMessage newMessage = new SentinelHttpMessage(s,
                    origHttpMessage.getHttpService().getHost(),
                    origHttpMessage.getHttpService().getPort(),
                    origHttpMessage.getHttpService().getProtocol().toLowerCase().equals("https") ? true : false);
            newMessage.setParentHttpMessage(origHttpMessage);
            
            tableModel.addHttpMessage(newMessage);
            
            viewLastMessage();
            this.updateUI();
        }
    }
    
    private void viewMessage(int index) {
        SentinelHttpMessage m = tableModel.getMessage(index);
        panelViewMessageUiLeft.setShowResponse(true);
        panelViewMessageUiLeft.setHttpMessage(m);
        jTable1.getSelectionModel().setSelectionInterval(index, index);
        this.currentSelectedRow = index;
        this.updateUI();
    }
    
    private void viewLastMessage() {
        viewMessage(tableModel.getMessageCount()-1);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelLeft = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        panelRight = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        panelMsgOne = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        panelViewMessageUiLeft = new gui.viewMessage.PanelViewMessageUi();
        panelMsgTwo = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        panelViewMessageUiRight = new gui.viewMessage.PanelViewMessageUi();

        jTable1.setModel(getTableModel());
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout panelLeftLayout = new javax.swing.GroupLayout(panelLeft);
        panelLeft.setLayout(panelLeftLayout);
        panelLeftLayout.setHorizontalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        panelLeftLayout.setVerticalGroup(
            panelLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jSplitPane1.setDividerLocation(550);
        jSplitPane1.setResizeWeight(0.5);

        panelMsgOne.setLayout(new java.awt.BorderLayout());

        jButton1.setText("Go");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");

        jButton3.setText("<");

        jButton4.setText(">");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addGap(0, 339, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton1)
                .addComponent(jButton2)
                .addComponent(jButton3)
                .addComponent(jButton4))
        );

        panelMsgOne.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(panelViewMessageUiLeft, java.awt.BorderLayout.CENTER);

        panelMsgOne.add(jPanel2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(panelMsgOne);

        jButton5.setText("jButton5");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton5)
                .addGap(0, 486, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton5)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelViewMessageUiRight, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 480, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelViewMessageUiRight, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelMsgTwoLayout = new javax.swing.GroupLayout(panelMsgTwo);
        panelMsgTwo.setLayout(panelMsgTwoLayout);
        panelMsgTwoLayout.setHorizontalGroup(
            panelMsgTwoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelMsgTwoLayout.setVerticalGroup(
            panelMsgTwoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMsgTwoLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setRightComponent(panelMsgTwo);

        javax.swing.GroupLayout panelRightLayout = new javax.swing.GroupLayout(panelRight);
        panelRight.setLayout(panelRightLayout);
        panelRightLayout.setHorizontalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1115, Short.MAX_VALUE)
        );
        panelRightLayout.setVerticalGroup(
            panelRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        sendMessage();
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel panelLeft;
    private javax.swing.JPanel panelMsgOne;
    private javax.swing.JPanel panelMsgTwo;
    private javax.swing.JPanel panelRight;
    private gui.viewMessage.PanelViewMessageUi panelViewMessageUiLeft;
    private gui.viewMessage.PanelViewMessageUi panelViewMessageUiRight;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getTabCaption() {
        return "Replayer";
    }

    @Override
    public Component getUiComponent() {
        return this;
    }

}
