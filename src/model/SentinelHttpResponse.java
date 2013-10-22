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
package model;

import burp.IHttpRequestResponse;
import burp.IResponseInfo;
import gui.categorizer.CategorizerManager;
import gui.categorizer.ResponseCategory;
import java.util.Arrays;
import java.util.LinkedList;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class SentinelHttpResponse {
    private IResponseInfo responseInfo;
    private byte[] response;
    
    private int size = 0;
    private int domCount = 0;
    
    private LinkedList<ResponseCategory> categories = new LinkedList<ResponseCategory>();

    SentinelHttpResponse() {

    }
    
    SentinelHttpResponse(byte[] response) {
        this.response = response;
        parseResponse();
    }
    
    SentinelHttpResponse(IHttpRequestResponse httpMessage) {
        response = httpMessage.getResponse();
        parseResponse();
    }

    byte[] getByteResponse() {
        return response;
    }
    
    private void parseResponse() {
        if (response == null || response.length< 1) {
            return;
        }
        
        responseInfo = BurpCallbacks.getInstance().getBurp().getHelpers().analyzeResponse(response);
        domCount = 0;
        for(int n=0; n<response.length; n++) {
            if (response[n] == '/') {
                domCount++;
            }
        }
        
        // Get response length
        for(String header: responseInfo.getHeaders()) {
            String a[] = header.split(": ");
            if (a[0].toLowerCase().equals("Content-Length".toLowerCase()) && a.length == 2) {
                this.size = Integer.parseInt(a[1]);
            }
        }
        
        // Categorize response
        categorizeResponse();
        
        /*
        String res = BurpCallbacks.getInstance().getBurp().getHelpers().bytesToString(response);
        String parts[] = res.split("\r\n\r\n");
        String lines[] = parts[0].split("\r\n");
//        String header[] = lines[0].split(" ");
        
        for(String l: lines) {
            String[] s = l.split(": ");
            
            if (s.length == 2) {
                if (s[0].toLowerCase().equals("Content-Length".toLowerCase())) {
                    this.size = Integer.parseInt(s[1]);
                }
            }
        }
        
        if (parts.length == 2) {
            Pattern pattern = Pattern.compile("<.*?>");
            Matcher matcher = pattern.matcher(parts[1]);
            int n = 0;
            while(matcher.find()) {
                n++;
            }
            
            this.domCount = n;
        }*/
    }

    
    public void categorizeResponse() {
        categories.clear();
        categories.addAll(CategorizerManager.getInstance().categorize(new String(response)));
    }
    
    
    public String getCategoriesString() {
        String res = "";
        
        for(ResponseCategory category: categories) {
            res += category.getName() + ",";
        }
        
        if (res.length() == 0) {
            return "";
        } else {
            return res.substring(0, res.length() - 1);
        }
    }

    
    public int getDom() {
        return domCount;
    }
    
    public boolean hasResponseParam(String value) {
        if (response == null) {
            return false;
        }

        String s = new String(response);
        if (s.contains(value)) {
            return true;
        } else {
            return false;
        }
    }

    public String getResponseStr() {
        String s;

        if (response != null) {
            s = new String(response);
        } else {
            s = "Sentinel: Response does not exist";
        }
        return s;
    }

    public boolean hasResponse() {
        if (response == null) {
            return false;
        } else {
            return true;
        }
    }

    public int getSize() {
        return size;
    }

    public String getHttpCode() {
        if (responseInfo != null) {
            return Integer.toString(responseInfo.getStatusCode());
        } else {
            return "";
        }
    }

    public String getBodyStr() {
        byte[] body = Arrays.copyOfRange(response, responseInfo.getBodyOffset(), response.length);
        String s = BurpCallbacks.getInstance().getBurp().getHelpers().bytesToString(body);
        return s;
    }
}
