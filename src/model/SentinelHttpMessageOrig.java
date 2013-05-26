package model;

import burp.IHttpRequestResponse;
import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author dobin
 */
public class SentinelHttpMessageOrig extends SentinelHttpMessage {
        
    public SentinelHttpMessageOrig(IHttpRequestResponse httpMessage) {
        super(httpMessage);
    }
    
    public SentinelHttpMessageOrig(String s, String host, int port, boolean https) {
        super(s, host, port, https);
    }

    
    // Children
    private LinkedList<SentinelHttpMessageAtk> httpMessageChildren = new LinkedList<SentinelHttpMessageAtk>();

    public void addChildren(SentinelHttpMessageAtk aThis) {
        this.httpMessageChildren.add(aThis);
    }

    public LinkedList<SentinelHttpMessageAtk> getHttpMessageChildren() {
        return httpMessageChildren;
    }

    public Date getModifyTime() {
        Date newestDate = null;
        for (SentinelHttpMessage child : httpMessageChildren) {
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

    void notifyAttackResult() {
        this.setChanged();
        this.notifyObservers(SentinelHttpMessageAtk.ObserveResult.ATTACKRESULT);
    }
    

}
