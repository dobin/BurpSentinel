package model;

import attacks.AttackResult;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import gui.viewMessage.ResponseHighlight;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import util.BurpCallbacks;

/**
 * HttpMessage Model
 *
 * Capsules IHttpRequestResponse from burp Handles all data modifications of the
 * request and response
 *
 * @author Dobin
 */
public class SentinelHttpMessage extends Observable implements IHttpRequestResponse {
    private AttackResult attackResult;
    private SentinelHttpResponse httpResponse;
    private SentinelHttpRequest httpRequest;
    private long loadTime = 0;
    private SentinelHttpMessage parentHttpMessage = null;
    private String comment;
    private boolean isRedirected;

    private IHttpService httpService;
    private Date createTime;
    
    public SentinelHttpMessage() {
        //httpRequest = new SentinelHttpRequest();
        //httpResponse = new SentinelHttpResponse();
    }
    
    public SentinelHttpMessage(IHttpRequestResponse httpMessage) {
        httpRequest = new SentinelHttpRequest(httpMessage);
        httpService = httpMessage.getHttpService();
        httpResponse = new SentinelHttpResponse(httpMessage);
    
        createTime = new Date(System.currentTimeMillis());
        comment = httpMessage.getComment();
    }
    
    public SentinelHttpMessage(SentinelHttpMessage httpMessage) {
        this((IHttpRequestResponse) httpMessage);
        this.tableIndexMain = httpMessage.getTableIndexMain();
    }
    
    public SentinelHttpMessage(String s, String host, int port, boolean https) {
        httpService = BurpCallbacks.getInstance().getBurp().getHelpers().buildHttpService(host, port, https);
        httpRequest = new SentinelHttpRequest(s, httpService);
        
        httpResponse = new SentinelHttpResponse();
        
        createTime = new Date(System.currentTimeMillis());
    }
    
    public SentinelHttpMessage(String s, IHttpService httpService) {
        this.httpService = httpService;
        httpRequest = new SentinelHttpRequest(s, httpService);
        
        createTime = new Date(System.currentTimeMillis());
    }
    
    public SentinelHttpRequest getReq() {
        return httpRequest;
    }
    
    public SentinelHttpResponse getRes() {
        return httpResponse;
    }

    @Override
    public String getHighlight() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHighlight(String color) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IHttpService getHttpService() {
        return httpService;
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
    }

    public void setLoadTime(long time) {
        this.loadTime = time;
    }
    
    public long getLoadTime() {
        return loadTime;
    }

    public String getInterestingFact() {
        return "";
    }

    public void setRedirected(boolean b) {
        isRedirected = b;
    }
    
    public boolean isRedirected() {
        return isRedirected;
    }

    public enum ObserveResult {
        REQUEST,
        RESPONSE,
        ATTACKRESULT,
        CHILDREN
    };
    
    public void addAttackResult(AttackResult res) {
        // Add result
        this.attackResult = res;
        //attackResults.add(res);

        // Fire update event
        this.setChanged();
        this.parentHttpMessage.setChanged();
        this.parentHttpMessage.notifyObservers(ObserveResult.ATTACKRESULT);
        this.notifyObservers(ObserveResult.ATTACKRESULT);
    }

    public AttackResult getAttackResult() {
        return attackResult;
    }

    private LinkedList<ResponseHighlight> responseHighlights = new LinkedList<ResponseHighlight>();

    public void addHighlight(ResponseHighlight h) {
        responseHighlights.add(h);
    }

    public Iterable<ResponseHighlight> getResponseHighlights() {
        return responseHighlights;
    }

    @Override
    public byte[] getRequest() {
        return httpRequest.getRequestByte();
    }

    @Override
    public void setRequest(byte[] message) {
        httpRequest = new SentinelHttpRequest(message, httpService);
    }

    @Override
    public byte[] getResponse() {
        return httpResponse.getByteResponse();
    }

    @Override
    public void setResponse(byte[] message) {
        httpResponse = new SentinelHttpResponse(message);
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    
    // Children
    private LinkedList<SentinelHttpMessage> httpMessageChildren = new LinkedList<SentinelHttpMessage>();
    private void addChildren(SentinelHttpMessage aThis) {
        this.httpMessageChildren.add(aThis);
    }
    public LinkedList<SentinelHttpMessage> getHttpMessageChildren() {
        return httpMessageChildren;
    }
    
    // Parenting
    public void setParentHttpMessage(SentinelHttpMessage httpMessage) {
        this.parentHttpMessage = httpMessage;
        
        parentHttpMessage.addChildren(this);
        
        this.setChanged();
        this.notifyObservers(ObserveResult.CHILDREN);
    }

    public SentinelHttpMessage getParentHttpMessage() {
        if (parentHttpMessage == null) {
            BurpCallbacks.getInstance().print("getParentHttpMessage: null");
        }
        return parentHttpMessage;
    }

    public Date getCreateTime() {
       return createTime;
    }
    
    public Date getModifyTime() {
        Date newestDate = null;
        for(SentinelHttpMessage child: httpMessageChildren) {
            if (newestDate == null) {
                newestDate = child.getCreateTime();
            } else {
                Date childDate = child.getCreateTime();
                if (childDate.after(newestDate)) {
                    newestDate = childDate;
                }
            }
        }
        
        return newestDate;
    }
    
    /********************************************/
    private int tableIndexMain = -1;
    public int getTableIndexMain() {
        return tableIndexMain;
    }
    public void setTableIndexMain(int index) {
        this.tableIndexMain = index;
    }
    
    private int tableIndexAttack = -1;
    public int getTableIndexAttack() {
        return tableIndexAttack;
    }
    public void setTableIndexAttack(int index) {
        this.tableIndexAttack = index;
    }

}
