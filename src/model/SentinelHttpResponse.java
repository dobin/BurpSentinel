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
import gui.categorizer.model.ResponseCategory;
import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import util.BurpCallbacks;

/**
 *
 * @author unreal
 */
public class SentinelHttpResponse implements Serializable {
    transient private IResponseInfo responseInfo; // re-init upon deserializing in readObject()
    private byte[] response;
    private String responseStr = null;
    private String responseBodyStr = null;
    
    private int size = 0;
    private int domCount = 0;
    
    private LinkedList<ResponseCategory> categories = new LinkedList<ResponseCategory>();
    private LinkedList<ResponseHighlight> responseHighlights = new LinkedList<ResponseHighlight>();

    SentinelHttpResponse() {
        // Deserializing Constructor
    }
    
    // Deserializing
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // As I dont want to re-implement IResponseInfo, make it transient
        // and redo responseInfo upon deserializing
        if (response != null) {
            responseInfo = BurpCallbacks.getInstance().getBurp().getHelpers().analyzeResponse(response);
        }
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
        if (response == null || response.length <= 0) {
            BurpCallbacks.getInstance().print("SentinelHttpResponse constructor: response null or size 0");
            return;
        }
        
        // Burp Analyze Response
        responseInfo = BurpCallbacks.getInstance().getBurp().getHelpers().analyzeResponse(response);
        
        // Populate domcount
        String mime = responseInfo.getInferredMimeType();
        domCount = 0;
        char domSearch = ' ';
        if (mime.equals("JSON")) {
            domSearch = '{';
        } else if (mime.equals("HTML")) {
            domSearch = '<';
        } else {
            domSearch = '\n';
        }
        for(int n=0; n<response.length; n++) {
            if (response[n] == domSearch) {
                domCount++;
            }
        }
        
        // Get response length
        size = -1;
        for(String header: responseInfo.getHeaders()) {
            String a[] = header.split(": ");
            if (a[0].toLowerCase().equals("Content-Length".toLowerCase()) && a.length == 2) {
                this.size = Integer.parseInt(a[1]);
            }
        }
        // if no content-length is given (for example, HTTP1.0), use default
        // response size. 
        if (size == -1) {
            size = response.length;
        }
        
        // Categorize response
        categorizeResponse();
    }

    
    public void categorizeResponse() {
        categories.clear();
        categories.addAll(CategorizerManager.getInstance().categorize(new String(response)));
        for(ResponseCategory category: categories) {
            ResponseHighlight highlight = new ResponseHighlight(category.getIndicator(), Color.orange);
            addHighlight(highlight);
        }
    }
    
    public LinkedList<ResponseCategory> getCategories() {
        return categories;
    }
        
    public int getDomCount() {
        return domCount;
    }
    
    public boolean hasResponseParam(String value) {
        if (response == null) {
            return false;
        }

        if (this.getResponseStr().contains(value)) {
            return true;
        } else {
            return false;
        }
    }

    public String getResponseStr() {
        if (responseStr == null) {
            if (response == null) {
                responseStr = "Sentinel: Response does not exist";
            } else {
                responseStr = new String(response);
            }
        }
        
        return responseStr;
    }
    
    public String getResponseStrBody() {
        if (responseBodyStr == null) {
            if (response == null) {
                responseBodyStr = "Sentinel: Response does not exist";
            } else {
                responseBodyStr = getResponseStr().substring(responseInfo.getBodyOffset());
            }
        }
        
        return responseBodyStr;
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


    
    /**
     * ************************** Getters*************************************
     * 
     * Note: the following functions are slow, as it extracts on the fly
     * 
     */
    public String extractFirstLine() {
        String http = getResponseStr().substring(0, getResponseStr().indexOf("\r\n"));
        return http;
    }

    public List<String> extractHeaders() {
        return responseInfo.getHeaders();
    }

    public String extractBody() {
        byte[] body = Arrays.copyOfRange(response, responseInfo.getBodyOffset(), response.length);
        String s = BurpCallbacks.getInstance().getBurp().getHelpers().bytesToString(body);
        
        if (s == null) {
            s = "";
        }
        return s;
    }

    
    /**
     * ************************** Highlights**********************************
     */       

    public void addHighlight(ResponseHighlight h) {
        responseHighlights.add(h);
    }

    public Iterable<ResponseHighlight> getResponseHighlights() {
        return responseHighlights;
    }
}
