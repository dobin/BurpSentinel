/*
 * Copyright (C) 2015 DobinRutishauser@broken.ch
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

import gui.viewMessage.PanelViewMessageUi;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

/**
 *
 * @author DobinRutishauser@broken.ch
 */
public class Beautifier {
    private static Beautifier instance = null;
    
    public static Beautifier getInstance() {
        if (instance == null) {
            instance = new Beautifier();
        }
        
        return instance;
    }
    
    private Tidy tidy;
    
    public Beautifier() {
        tidy = new Tidy();
        
        tidy.setWraplen(0);
        //tidy.setDropEmptyParas(false);
        tidy.setDropFontTags(false);
        tidy.setDropProprietaryAttributes(false);
        tidy.setIndentContent(true);

    }
    
    private class myTidyMsgListener implements TidyMessageListener {
        
        private LinkedList<TidyMessage> list;
        
        public myTidyMsgListener(LinkedList<TidyMessage> list) {
            this.list = list;
        }
        
        @Override
        public void messageReceived(TidyMessage tm) {
            list.add(tm);
        }
    }
    
    private class testTidyMsgListener implements TidyMessageListener {

        @Override
        public void messageReceived(TidyMessage tm) {
            BurpCallbacks.getInstance().print(Integer.toString(tm.getErrorCode()) + " - " + tm.getMessage());
        }
        
    }

    
    public LinkedList<TidyMessage> analyze(String input) {
        LinkedList<TidyMessage> msgs = new LinkedList<TidyMessage>();
        TidyMessageListener msgListener = new myTidyMsgListener(msgs);
        tidy.setMessageListener(msgListener);
        
        InputStream is = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        tidy.parse(is, os);
        tidy.setMessageListener(null);
        
        return msgs;
    }
    
    public boolean hasHtmlSyntaxError(LinkedList<TidyMessage> orig, LinkedList<TidyMessage> curr) {
        int diff = curr.size() - orig.size();
        
        if (diff > 0 && diff < 7) {
            return true;
        } else {
            return false;
        }
    }

    public String getMessageDiffString(LinkedList<TidyMessage> origList, LinkedList<TidyMessage> currList) {
        String ret = "";
        
        LinkedList<TidyMessage> diffList = new LinkedList<TidyMessage>();
        
        for(TidyMessage curr: currList) {
            boolean found = false;
            
            for(TidyMessage orig: origList) {
                if (orig.getLine() == curr.getLine()) {
                    found = true;
                    break;
                }
            }
            
            if (! found) {
                diffList.add(curr);
            }
        }
        
        for (TidyMessage diff: diffList) {
            //ret += BurpCallbacks.getInstance().getBurp().getHelpers().urlEncode(diff.getMessage()) + "<br>";
            ret += StringEscapeUtils.escapeHtml4(diff.getMessage()) + "<br>";
        }
        
        return ret;
    }    
    
    public String tidyUp(String input) {
        String res = "";
        
        InputStream is = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();

//        TidyMessageListener msgListener = new testTidyMsgListener();
//        tidy.setMessageListener(msgListener);

        tidy.parse(is, os);
        try {
            String s = new String(os.toByteArray(), "UTF-8");
            res = s;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PanelViewMessageUi.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return res;
    }
    
    
}