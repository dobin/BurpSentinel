/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import burp.IHttpService;

/**
 *
 * @author unreal
 */
public class SentinelHttpService implements IHttpService {

    private String host;
    private int port;
    private String protocol;
    
    public SentinelHttpService(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }
    
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }
    
}
