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
public abstract class SentinelHttpMessage extends Observable implements IHttpRequestResponse {

    private SentinelHttpResponse httpResponse;
    private SentinelHttpRequest httpRequest;
    private String comment;
    private IHttpService httpService;
    private Date createTime;
    private long loadTime = 0;

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

    public Date getCreateTime() {
        return createTime;
    }
    private boolean isRedirected;

    public void setLoadTime(long time) {
        this.loadTime = time;
    }

    public long getLoadTime() {
        return loadTime;
    }

    public void setRedirected(boolean b) {
        isRedirected = b;
    }

    public boolean isRedirected() {
        return isRedirected;
    }

////////////////////////////////////////////////
    @Override
    public String getHighlight() {
        BurpCallbacks.getInstance().print("NOT SUPPORTED");
        return "";
    }

    @Override
    public void setHighlight(String color) {
        BurpCallbacks.getInstance().print("NOT SUPPORTED");
    }

    @Override
    public IHttpService getHttpService() {
        return httpService;
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.httpService = httpService;
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
    /**
     * *****************************************
     */
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
