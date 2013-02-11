package gui.viewMessage;

import gui.mainBot.PanelBotLinkManager;
import gui.session.SessionManager;
import gui.session.SessionUser;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoundedRangeModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import model.SentinelHttpMessage;
import model.SentinelHttpParam;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.w3c.tidy.Tidy;
import util.BurpCallbacks;
import util.UiUtil;
import util.diff.DiffPrint.UnifiedSmallPrint;
import util.diff.GnuDiff;
import util.diff.GnuDiff.change;

/**
 * Displays httpMessages
 *
 * - Response with it's information - and the request - accumulates data to
 * highlight
 *
 *
 * @author Dobin
 */
public class PanelViewMessageUi extends javax.swing.JPanel {

    private SentinelHttpMessage httpMessage = null;
    private boolean showResponse = true;
    private PanelViewMessagePopup messagePopup;
    
    private PanelViewComboboxModel panelViewComboboxModel;
    
    private PanelBotLinkManager linkManager = null;
    
    private int selectIndex = -1;
    private Object currentHighlight;
    private int savedCursor = -1;

    private BoundedRangeModel origScrollbarModel;
    
    
    /**
     * Creates new form PanelResponseUi
     */
    public PanelViewMessageUi() {
        panelViewComboboxModel = new PanelViewComboboxModel();
        
        initComponents();
        messagePopup = new PanelViewMessagePopup(this);
        
        textareaMessage.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        textareaMessage.setEditable(true);
        textareaMessage.setLineWrap(true);
        textareaMessage.setWrapStyleWord(false);
        textareaMessage.setAnimateBracketMatching(false);
        textareaMessage.setAutoIndentEnabled(false);
        textareaMessage.setBracketMatchingEnabled(false);
        textareaMessage.setPopupMenu(messagePopup.getPopup());
        UiUtil.getTheme().apply(textareaMessage);
        textareaMessage.revalidate();
        textareaMessage.requestFocusInWindow();
        textareaMessage.setMarkAllHighlightColor(new Color(0xff, 0xea, 0x00, 100));
   
        labelPosition.setText(" ");
        
        origScrollbarModel = jScrollPane1.getVerticalScrollBar().getModel();

        comboboxView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String selected = (String) cb.getSelectedItem();

                currentView = selected;
                showMessage();
            }
        });

        textfieldSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                initSearchContext(textfieldSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                initSearchContext(textfieldSearch.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                initSearchContext(textfieldSearch.getText());
            }
        });
    }
        

    public void setHttpMessage(SentinelHttpMessage httpMessage) {
        if (httpMessage == null) {
            BurpCallbacks.getInstance().print("setResponse(): HttpMessage NULL");
            return;
        }
        this.httpMessage = httpMessage;
        reInit();

    }

    private void reInit() {
        if (httpMessage.getRes().hasResponse()) {
            labelSize.setText(Integer.toString(httpMessage.getRes().getSize()));
            labelHttpCode.setText(httpMessage.getRes().getHttpCode());
            labelDom.setText(Integer.toString(httpMessage.getRes().getDom()));
            
            if (httpMessage.getParentHttpMessage() == null) {
                panelViewComboboxModel.hasParent(false);
            } else {
                panelViewComboboxModel.hasParent(true);
            }
            
            viewDefaultContent = null;
            viewBeautifyContent = null;
            viewDiffContent = null;
            
            labelRedirected.setText(httpMessage.isRedirected() ? "(R)" : "");
        }

        showMessage();
    }

    
    private String currentView = "Default";
    private String viewDefaultContent = null;
    private String viewBeautifyContent = null;
    private String viewDiffContent = null;

    
    private void showDefaultView() {
        if (viewDefaultContent == null) {
            viewDefaultContent = httpMessage.getRes().getResponseStr();
        }
        
        setMessageText(viewDefaultContent);
        highlightResponse();
    }
    
    private void showBeautifyView() {
        if (viewBeautifyContent == null) {
            
            String res = httpMessage.getRes().getBodyStr();
            
            InputStream is = new ByteArrayInputStream(res.getBytes());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            Tidy tidy = new Tidy();
            tidy.setWraplen(0);
            tidy.setDropEmptyParas(false);
            tidy.setDropFontTags(false);
            tidy.setDropProprietaryAttributes(false);
            tidy.setIndentContent(true);
            
            tidy.parse(is, os);
            try {
                String s = new String (os.toByteArray(), "UTF-8");
                viewBeautifyContent = s;
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(PanelViewMessageUi.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        setMessageText(viewBeautifyContent);
        highlightResponse();
    }
    
    
    private void showDiffView() {
        if (viewDiffContent == null) {
            String origRes = httpMessage.getParentHttpMessage().getRes().getResponseStr();
            String newRes = httpMessage.getRes().getResponseStr();

            String[] origResArr = origRes.split("\n");
            String[] newResArr = newRes.split("\n");

            GnuDiff gnuDiff = new GnuDiff(origResArr, newResArr);
            change changes = gnuDiff.diff(GnuDiff.forwardScript);

            UnifiedSmallPrint unifiedPrint = new UnifiedSmallPrint(origResArr, newResArr);
            StringWriter a = new StringWriter();
            unifiedPrint.setOutput(a);
            unifiedPrint.print_script(changes);
            
            viewDiffContent = a.toString();
        }
        
        setMessageText(viewDiffContent);
        
        highlightResponse();
    }
    
    public void setShowResponse(boolean b) {
        this.showResponse = b;
    }
    
    private void showMessage() {
        textareaMessage.getHighlighter().removeAllHighlights();

        if (showResponse) {
            buttonShowRequest.setText("Response");

            if (currentView.equals("Default")) {
                showDefaultView();
            } else if (currentView.equals("Beautify")) {
                showBeautifyView();
            } else if (currentView.equals("Diff")) {
                showDiffView();
            }
            
            comboboxView.setVisible(true);

            //buttonShowResponse.setBackground(Color.GRAY);
            //buttonShowResponse.setSelected(true);
            //buttonShowRequest.setBackground(Color.LIGHT_GRAY);
            //buttonShowRequest.setSelected(false);
        } else {
            buttonShowRequest.setText("Request");
            setMessageText(httpMessage.getReq().getRequestStr());
            highlightRequest();
            
            comboboxView.setVisible(false);
            //buttonShowResponse.setBackground(Color.LIGHT_GRAY);
            //buttonShowRequest.setBackground(Color.GRAY);
            //buttonShowResponse.setSelected(false);
            //buttonShowRequest.setSelected(true);
        }

        labelPosition.setText("/" + myHighlights.size());
        viewMessagePart(0, true);
        this.invalidate();
        this.updateUI();
        
        //IMessageEditor i = BurpCallbacks.getInstance().getBurp().createMessageEditor(null, false);
    }

    /*
     * Highlight important data in request:
     * - origparam
     * - session
     */
    private void highlightRequest() {
        // Highlight session (original, as defined by cookie name)
        String sessionName = SessionManager.getInstance().getSessionVarName();
        for(SentinelHttpParam param: httpMessage.getReq().getParams()) {
            if (param.getName().equals(sessionName)) {
                //System.out.println("sessionName: " + sessionName + " "+ param.getValueStart());
                Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.orange);
                try {
                    textareaMessage.getHighlighter().addHighlight(param.getValueStart(), param.getValueEnd(), painter);
                } catch (BadLocationException ex) {
                    BurpCallbacks.getInstance().print("ARERRR1");
                }
            }
        }
        
        // Highlight session (defined, as by session value)
        LinkedList<SessionUser> sessionUsers = SessionManager.getInstance().getSessionUsers();
        String content = textareaMessage.getText();
        for(SessionUser sessionUser: sessionUsers) {
            String value = sessionUser.getValue();
            for (int index = content.indexOf(value); index >= 0; index = content.indexOf(value, index + 1)) {
                Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.green);
                try {
                    textareaMessage.getHighlighter().addHighlight(index, index + value.length(), painter);
                } catch (BadLocationException ex) {
                    BurpCallbacks.getInstance().print("ARERRR2");
                }
            }
        }
        
        // Highlight changeparam
        SentinelHttpParam httpParam = httpMessage.getReq().getChangeParam();
        if (httpParam == null) {
            return;
        }
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(0xc0dbff));
        try {
            textareaMessage.getHighlighter().addHighlight(httpParam.getValueStart(), httpParam.getValueEnd(), painter);
        } catch (BadLocationException ex) {
            BurpCallbacks.getInstance().print("ARERRR3");
        }
    }

    
    private LinkedList<SentinelHighlight> myHighlights;

    private void highlightResponse() {
        String response = textareaMessage.getText();
        myHighlights = new LinkedList<SentinelHighlight>();
        SentinelHighlight sh;
        
        // Highlight session (defined, as by session value)
        LinkedList<SessionUser> sessionUsers = SessionManager.getInstance().getSessionUsers();
        for(SessionUser sessionUser: sessionUsers) {
            String value = sessionUser.getValue();
            for (int index = response.indexOf(value); index >= 0; index = response.indexOf(value, index + 1)) {
                sh = new SentinelHighlight(index, index+value.length(), new Color(0xf9, 0x4f, 0x4f, 100));

                Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(sh.getColor());
                try {
                    textareaMessage.getHighlighter().addHighlight(sh.getStart(), sh.getEnd(), painter);
                    myHighlights.add(sh);
                } catch (BadLocationException ex) {
                    BurpCallbacks.getInstance().print("ARERRR2");
                }
            }
        }
        
        //String response = httpMessage.getRes().getResponseStr();
        for (ResponseHighlight h : httpMessage.getResponseHighlights()) {
            for (int index = response.indexOf(h.getStr()); index >= 0; index = response.indexOf(h.getStr(), index + 1)) {
                if (index == -1) {
                    BurpCallbacks.getInstance().print("highlightResponse: index=-1, string not found. catch it damned!");
                }
                
                if (isDuplicateHighlight(index, index + h.getStr().length())) {
                    continue;
                }
                
                sh = new SentinelHighlight(index, index + h.getStr().length(), h.getColor());

                // Also add highlighter to indicate to the user where it is
                Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(h.getColor());

                try {
                    textareaMessage.getHighlighter().addHighlight(sh.getStart(), sh.getEnd(), painter);
                    
                    myHighlights.add(sh);
                } catch (BadLocationException ex) {
                    BurpCallbacks.getInstance().print("ARERRR");
                }
            }
        }
        /*
        Highlighter.HighlightPainter painter;
        try {
            painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(0, 255, 0, 200));
            textareaMessage.getHighlighter().addHighlight(0, 5, painter);
            
                        painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 0, 0, 250));
            textareaMessage.getHighlighter().addHighlight(2, 3, painter);
            

        } catch (BadLocationException ex) {
            Logger.getLogger(PanelViewMessageUi.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    
    private boolean isDuplicateHighlight(int start, int end) {
        for (SentinelHighlight h: myHighlights) {
            if (start >= h.getStart() && end <= h.getEnd()) {
                BurpCallbacks.getInstance().print("Duplicate: " + start +":"+end);
                return true;
            }
        }
        return false;
    }
    
    public void c_sendAgain() {
        BurpCallbacks.getInstance().sendRessource(httpMessage, true);
        this.reInit();
    }
    
    public void c_sendToRepeater() {
        BurpCallbacks.getInstance().sendToRepeater(httpMessage);
    }
    
    public void c_copySmart() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        String s = "Request:\n";
        s += httpMessage.getReq().getRequestStr();
        s += "\n\nResponse:\n";
        s += httpMessage.getRes().getResponseStr();
        
        StringSelection ss = new StringSelection(s);
        
        clipboard.setContents(ss, null);
    }

    
    private ComboBoxModel getPanelViewComboboxModel() {
        return panelViewComboboxModel;
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        labelHttpCode = new javax.swing.JLabel();
        labelSize = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        labelDom = new javax.swing.JLabel();
        labelRedirected = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        checkboxIsFix = new javax.swing.JCheckBox();
        checkboxIsLink = new javax.swing.JCheckBox();
        buttonUp = new javax.swing.JButton();
        buttonDown = new javax.swing.JButton();
        labelPosition = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        buttonShowRequest = new javax.swing.JButton();
        comboboxView = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textareaMessage = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        jPanel4 = new javax.swing.JPanel();
        buttonPrev = new javax.swing.JButton();
        buttonNext = new javax.swing.JButton();
        textfieldSearch = new javax.swing.JTextField();

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setPreferredSize(new java.awt.Dimension(135, 24));

        labelHttpCode.setText("000");
        labelHttpCode.setToolTipText("HTTP Response Code");

        labelSize.setText("00000");
        labelSize.setToolTipText("Size of Response Body in Bytes");

        jLabel1.setText(";");

        labelDom.setText("000");
        labelDom.setToolTipText("Number of Tags in Response");

        labelRedirected.setText(" ");
        labelRedirected.setToolTipText("Indicate if message is result of a 302 redirect");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(labelHttpCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelDom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelRedirected)
                .addContainerGap(44, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelHttpCode)
                    .addComponent(labelSize)
                    .addComponent(jLabel1)
                    .addComponent(labelDom)
                    .addComponent(labelRedirected))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        checkboxIsFix.setText("Fix");
        checkboxIsFix.setToolTipText("Fix Cursor Location - keep view for all messages");
        checkboxIsFix.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkboxIsLink.setText("Link");
        checkboxIsLink.setToolTipText("Link Both Window Togeter - Scroll Together");
        checkboxIsLink.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxIsLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxIsLinkActionPerformed(evt);
            }
        });

        buttonUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/1356984817_arrow_large_up.png"))); // NOI18N
        buttonUp.setToolTipText("Select previous Highlight");
        buttonUp.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonUp.setMaximumSize(new java.awt.Dimension(24, 20));
        buttonUp.setMinimumSize(new java.awt.Dimension(24, 20));
        buttonUp.setPreferredSize(new java.awt.Dimension(32, 22));
        buttonUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonUpActionPerformed(evt);
            }
        });

        buttonDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/1356984823_arrow_large_down.png"))); // NOI18N
        buttonDown.setToolTipText("Select next Highlight");
        buttonDown.setMargin(new java.awt.Insets(1, 1, 1, 1));
        buttonDown.setMaximumSize(new java.awt.Dimension(24, 20));
        buttonDown.setMinimumSize(new java.awt.Dimension(24, 20));
        buttonDown.setPreferredSize(new java.awt.Dimension(32, 22));
        buttonDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDownActionPerformed(evt);
            }
        });

        labelPosition.setText("0");
        labelPosition.setToolTipText("Current Highlight Index / Number of Highlights");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(checkboxIsFix)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkboxIsLink)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPosition)
                .addGap(0, 8, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelPosition)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(buttonUp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkboxIsFix)
                            .addComponent(checkboxIsLink))
                        .addComponent(buttonDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        buttonShowRequest.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        buttonShowRequest.setForeground(new java.awt.Color(229, 137, 0));
        buttonShowRequest.setText("Request");
        buttonShowRequest.setFocusable(false);
        buttonShowRequest.setMargin(new java.awt.Insets(0, 2, 0, 2));
        buttonShowRequest.setPreferredSize(new java.awt.Dimension(86, 24));
        buttonShowRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShowRequestActionPerformed(evt);
            }
        });

        comboboxView.setModel(getPanelViewComboboxModel());

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(comboboxView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonShowRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonShowRequest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboboxView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        textareaMessage.setColumns(20);
        textareaMessage.setRows(5);
        jScrollPane1.setViewportView(textareaMessage);

        buttonPrev.setText("<");
        buttonPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPrevActionPerformed(evt);
            }
        });

        buttonNext.setText(">");
        buttonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(buttonPrev)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonNext)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textfieldSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonPrev)
                    .addComponent(buttonNext)
                    .addComponent(textfieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonShowRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowRequestActionPerformed
        this.showResponse = ! this.showResponse;
        showMessage();
    }//GEN-LAST:event_buttonShowRequestActionPerformed


    
    private void moveCursorDown() {
        if (currentHighlight != null) {
            textareaMessage.getHighlighter().removeHighlight(currentHighlight);
            currentHighlight = null;
        }
        
        try {
            selectIndex++;
            if (selectIndex >= myHighlights.size()) {
                selectIndex = 0;
            }
            SentinelHighlight nextHighlight = myHighlights.get(selectIndex);

            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);
            currentHighlight = textareaMessage.getHighlighter().addHighlight(nextHighlight.getStart()-1, nextHighlight.getEnd()+1, painter);
            
            int o = nextHighlight.getStart();
            viewMessagePart(o, false);
            labelPosition.setText(Integer.toString(selectIndex + 1) + "/" + Integer.toString(myHighlights.size()));
        } catch (BadLocationException ex) {
            BurpCallbacks.getInstance().print("CANT HIGHLIGHT1");
        }
    }

        
    private void moveCursorUp() {
        if (currentHighlight != null) {
            textareaMessage.getHighlighter().removeHighlight(currentHighlight);
            currentHighlight = null;
        }
        
        try {
            selectIndex--;
            if (selectIndex < 0) {
                selectIndex = myHighlights.size() - 1;
            }
            SentinelHighlight nextHighlight = myHighlights.get(selectIndex);
            
            Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);
            currentHighlight = textareaMessage.getHighlighter().addHighlight(nextHighlight.getStart()-1, nextHighlight.getEnd()+1, painter);
            
            int o = nextHighlight.getStart();
            
            viewMessagePart(o, false);
            labelPosition.setText(Integer.toString(selectIndex + 1) + "/" + Integer.toString(myHighlights.size()));
        } catch (BadLocationException ex) {
            BurpCallbacks.getInstance().print("CANT HIGHLIGHT2");
        }
    }
    
    private void viewMessagePart(int n, boolean isNew) {
        if (isNew) {
            if (checkboxIsFix.isSelected()) {
                textareaMessage.setCaretPosition(savedCursor);
            } else {
                textareaMessage.setCaretPosition(n);
            }
        } else {
            textareaMessage.setCaretPosition(n);
            
            if (checkboxIsLink.isSelected() && linkManager != null) {
                linkManager.setPosition(n, this);
            }
        }
    }
    
    public void setPosition(int n) {
        textareaMessage.setCaretPosition(n);
    }
        
    private void setMessageText(String s) {
        savedCursor = textareaMessage.getCaretPosition();
        textareaMessage.setText(s);
    }
    
    public void setLinkManager(PanelBotLinkManager linkManager) {
        this.linkManager = linkManager;
        linkManager.registerViewMessage(this);
    }
    
    
    
    public void setScrollBarModel(BoundedRangeModel model) {
        if (model == null) {
            int pos = jScrollPane1.getVerticalScrollBar().getValue();
            origScrollbarModel.setValue(pos);
            jScrollPane1.getVerticalScrollBar().setModel(origScrollbarModel);
            checkboxIsLink.setSelected(false);
        } else {
            // Set new model
            //origScrollbarModel = jScrollPane2.getVerticalScrollBar().getModel();
            jScrollPane1.getVerticalScrollBar().setModel(model);
            checkboxIsLink.setSelected(true);
        }
    }
    
    private void buttonDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDownActionPerformed
        moveCursorDown();
    }//GEN-LAST:event_buttonDownActionPerformed

    private void buttonUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonUpActionPerformed
        moveCursorUp();
    }//GEN-LAST:event_buttonUpActionPerformed

    private void checkboxIsLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxIsLinkActionPerformed
        if (checkboxIsLink.isSelected()) {
            // Other window should have same model
            linkManager.setScrollModel(jScrollPane1.getVerticalScrollBar().getModel(), this);
        } else {
            // Restore original model on all window
            linkManager.setScrollModel(null, this);
            jScrollPane1.getVerticalScrollBar().setModel(origScrollbarModel);
        }
    }//GEN-LAST:event_checkboxIsLinkActionPerformed

    
    private String lastSearch = "";
    private SearchContext searchContext = null;
    
    private void initSearchContext(String newSearchString) {
        if (lastSearch.equals(newSearchString)) {
            return;
        }
        lastSearch = newSearchString;
        if (newSearchString.equals("")) {
            searchContext = new SearchContext();
            textareaMessage.clearMarkAllHighlights();
            return;
        }

        textareaMessage.setCaretPosition(0);
        searchContext = new SearchContext();
        searchContext.setSearchFor(newSearchString);
        searchContext.setSearchForward(true);
        SearchEngine.find(textareaMessage, searchContext);
        
        textareaMessage.markAll(newSearchString, true, false, false);
    }
    
    private void buttonNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNextActionPerformed
        searchContext.setSearchForward(true);
        SearchEngine.find(textareaMessage, searchContext);
    }//GEN-LAST:event_buttonNextActionPerformed

    private void buttonPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrevActionPerformed
        searchContext.setSearchForward(false);
        SearchEngine.find(textareaMessage, searchContext);
    }//GEN-LAST:event_buttonPrevActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonDown;
    private javax.swing.JButton buttonNext;
    private javax.swing.JButton buttonPrev;
    private javax.swing.JButton buttonShowRequest;
    private javax.swing.JButton buttonUp;
    private javax.swing.JCheckBox checkboxIsFix;
    private javax.swing.JCheckBox checkboxIsLink;
    private javax.swing.JComboBox comboboxView;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelDom;
    private javax.swing.JLabel labelHttpCode;
    private javax.swing.JLabel labelPosition;
    private javax.swing.JLabel labelRedirected;
    private javax.swing.JLabel labelSize;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textareaMessage;
    private javax.swing.JTextField textfieldSearch;
    // End of variables declaration//GEN-END:variables

    public String getRequestContent() {
        if (showResponse) {
            return null;
        }
        
        String s = textareaMessage.getText();
        return s;
    }

    private boolean isRequestEditor = false;
    
    public void setRequestEditor(boolean b) {
        isRequestEditor = b;
        textareaMessage.setEditable(b);
    }


}
