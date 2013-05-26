package gui.mainTop;

import gui.SentinelMainUi;
import gui.mainTop.networking.NetworkerInfo;
import gui.session.SessionManager;
import gui.session.SessionManagerUi;
import gui.session.categorizer.CategorizerManager;
import gui.session.categorizer.CategorizerUi;
import gui.viewMessage.reporter.ReporterUi;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import model.SentinelHttpMessageOrig;
import util.UiUtil;

/**
 * Display all HttpMessage the user added to sentinel
 * 
 * @author Dobin
 */
public class PanelTopUi extends javax.swing.JPanel {
    private SentinelMainUi mainGuiFrame = null;
    private PanelTopTableModel tableTopModel;
    
    private int currentSelectedRow = -1;
    
    private PanelTopPopup popup;
    private NetworkerInfo info;
    private ReporterUi reporterUi;
    
    /**
     * Creates new form PanelTop
     */
    public PanelTopUi() {
        tableTopModel = new PanelTopTableModel(this);
        popup = new PanelTopPopup(this);
        initComponents();
        ((PanelTopNetworkBtn)btnNetworking).init();
        
        // TODO not necessary if in restoreTableDimensions default?
        tableAllMessages.setAutoCreateRowSorter(true);
        int width = 100;
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
        
        UiUtil.restoreTableDimensions(tableAllMessages, this);
    }
    
    public void init() {
        info = new NetworkerInfo();
        reporterUi = new ReporterUi(mainGuiFrame);
        
        // Add selection listener
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
                        currentSelectedRow = lsm.getMinSelectionIndex();
                        mainGuiFrame.showMessage(currentSelectedRow);
                    }
                }});
        
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
    
        
    public int getSelected() {
        return currentSelectedRow;
    }
    
/*
    
    private void showPopup(MouseEvent me) {
// is this event a popup trigger?
        if (pm.isPopupTrigger(me)) {
            Point p = me.getPoint();
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
// if we've clicked on a row in the second col
            if (row != -1 && col == 1) {
                one.setText("Do something to row " + row + ".");
                two.setText("Do something else to row " + row + ".");
                pm.show(table, p.x, p.y);
            }
        }
    }
  */  
    
    
    void removeMessage() {
        tableTopModel.removeMessage(currentSelectedRow);
        mainGuiFrame.removeMessage(currentSelectedRow);
    }
    
    // This gets called from MainGui
    // If the user sends a new HttpMessage from Burp to Sentinel
    public void addMessage(SentinelHttpMessageOrig httpMessage) {
        tableTopModel.addMessage(httpMessage);
        //this.updateUI();
        
        tableAllMessages.scrollRectToVisible(tableAllMessages.getCellRect(tableTopModel.getRowCount() - 1, 0, true));
        //this.updateUI();
    }

    // Used for swing
    private TableModel getMessageTableModel() {
        return tableTopModel;
    }
    
    
    // set MainGui (parent)
    // So we can interact with other UI parts (especially bot panel)
    public void setMainGui(SentinelMainUi aThis) {
        this.mainGuiFrame = aThis;
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
        jPanel1 = new javax.swing.JPanel();
        btnOptions = new javax.swing.JButton();
        btnNetworking = new PanelTopNetworkBtn();
        btnSessions = new javax.swing.JToggleButton();
        btnCategorizer = new javax.swing.JToggleButton();
        btnReporter = new javax.swing.JToggleButton();

        tableAllMessages.setModel(getMessageTableModel());
        tableAllMessages.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_NEXT_COLUMN);
        tableAllMessages.setRowHeight(20);
        tableAllMessages.setSelectionBackground(new java.awt.Color(255, 205, 129));
        tableAllMessages.setSelectionForeground(new java.awt.Color(0, 0, 0));
        tableAllMessages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(tableAllMessages);

        btnOptions.setText("Storage");
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });

        btnNetworking.setText("Network");
        btnNetworking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNetworkingActionPerformed(evt);
            }
        });

        btnSessions.setText("Sessions");
        btnSessions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSessionsActionPerformed(evt);
            }
        });

        btnCategorizer.setText("Categorize");
        btnCategorizer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCategorizerActionPerformed(evt);
            }
        });

        btnReporter.setText("Reporter");
        btnReporter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReporterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnNetworking, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnCategorizer)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(btnSessions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnReporter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnSessions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCategorizer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReporter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNetworking)
                .addGap(0, 43, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOptionsActionPerformed
   
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
            info.start();
            setRelativePos(btnNetworking, info);
            info.setVisible(true);
        } else {
            info.stop();
            info.setVisible(false);
        }
    }//GEN-LAST:event_btnNetworkingActionPerformed

    private void btnSessionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSessionsActionPerformed
        SessionManagerUi sessionUi = SessionManager.getInstance().getSessionManagerUi();
        
        if (btnSessions.isSelected()) {
            setRelativePos(btnSessions, (JFrame) sessionUi);
            sessionUi.setVisible(true);
        } else {
            sessionUi.setVisible(false);
        }
    }//GEN-LAST:event_btnSessionsActionPerformed

    private void btnCategorizerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCategorizerActionPerformed
        CategorizerUi categorizerUi = CategorizerManager.getInstance().getCategorizerUi();

        if (btnCategorizer.isSelected()) {
            setRelativePos(btnSessions, (JFrame) categorizerUi);
            categorizerUi.setVisible(true);
        } else {
            categorizerUi.setVisible(false);
        }
    }//GEN-LAST:event_btnCategorizerActionPerformed

    private void btnReporterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReporterActionPerformed
        if (btnReporter.isSelected()) {
            setRelativePos(btnReporter, (JFrame) reporterUi);
            reporterUi.setVisible(true);
        } else {
            reporterUi.setVisible(false);
        }    
    }//GEN-LAST:event_btnReporterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnCategorizer;
    private javax.swing.JToggleButton btnNetworking;
    private javax.swing.JButton btnOptions;
    private javax.swing.JToggleButton btnReporter;
    private javax.swing.JToggleButton btnSessions;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tableAllMessages;
    // End of variables declaration//GEN-END:variables

    
    public void storeUiPrefs() {
        UiUtil.storeTableDimensions(tableAllMessages, this);
    }

    public void reset() {
        tableTopModel.reset();
    }

    public void setSelected(int index) {
        tableAllMessages.getSelectionModel().setSelectionInterval(index, index);
        this.currentSelectedRow = index;
    }

    
}
