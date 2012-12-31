package model;

import burp.IHttpRequestResponse;
import burp.IResponseInfo;
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
        
        for(String header: responseInfo.getHeaders()) {
            String a[] = header.split(": ");
            if (a[0].toLowerCase().equals("Content-Length".toLowerCase()) && a.length == 2) {
                this.size = Integer.parseInt(a[1]);
            }
        }
        
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
            s = "<void>";
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
}
