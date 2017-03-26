/*
 * Copyright (C) 2016 dobin
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
package attacks;

import attacks.model.AttackData;
import attacks.model.AttackResult;
import burp.IResponseVariations;
import gui.categorizer.model.ResponseCategory;
import gui.networking.AttackWorkEntry;
import java.awt.Color;
import java.util.List;
import model.ResponseHighlight;
import model.SentinelHttpMessageAtk;
import util.BurpCallbacks;

/**
 *
 * @author dobin
 */
public class AttackSqlExtendedAnalyzer {
    final private Color failColor = new Color(0xff, 0xcc, 0xcc, 100);
    
    private boolean emptyResponseOnBreak = false;

    
    public AttackSqlExtendedAnalyzer() {
        
    }
    
    public void attackEndAnalyzer(List<byte[]> analRes) {
        byte[][] a = analRes.toArray(new byte[analRes.size()][]);
               
        IResponseVariations responseVariant = BurpCallbacks.getInstance().getBurp().getHelpers().analyzeResponseVariations(a);
        List<String> variantList = responseVariant.getVariantAttributes();
        for(String variant: variantList) {
            int ret;
            ret = responseVariant.getAttributeValue(variant, 0);
            BurpCallbacks.getInstance().getBurp().printOutput("A: " + variant + " : " + ret);
            ret = responseVariant.getAttributeValue(variant, 1);
            BurpCallbacks.getInstance().getBurp().printOutput("B: " + variant + " : " + ret);

        }
    }
    
    public boolean analyzeSimpleBreak(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        return true;
    }
    
    public boolean analyzeBreak(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        // First check if it already yielded a SQL error
        if (responseContainSqlErrorString(lastHttpMessage)) {
            return false; // No need to continue
        }

        // Same size is usually bad
        if (attackWorkEntry.origHttpMessage.getRes().getSize() == lastHttpMessage.getRes().getSize()) {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.ABORT,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Abort. break request identical to original.",
                "Response is identical to original response, therefore its not possible to identify SQL injection.");
            lastHttpMessage.addAttackResult(res);           
            
            return false;
        }
        
        // check for empty responses
        if (attackWorkEntry.origHttpMessage.getRes().getSize() > 0
                && lastHttpMessage.getRes().getSize() == 0) 
        {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.STATUSGOOD,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Good. break request resulted in empty response.",
                "Response is different (empty) than the original response, therefore there is a chance to identify SQL injection");
            lastHttpMessage.addAttackResult(res);
            emptyResponseOnBreak = true;
            
            return true;
        }
        
        if (areResponseTagsIdentical(attackWorkEntry, lastHttpMessage)) {
            // Identical, but different size
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.STATUSGOOD,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Good. break request resulted in difference.",
                "Response is different size than the original response, therefore there is a chance to identify SQL injection");
            lastHttpMessage.addAttackResult(res);
            
            return true;
        } else {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.STATUSGOOD,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Good. break request resulted in difference.",
                "Response is different than the original response, therefore there is a chance to identify SQL injection");
            lastHttpMessage.addAttackResult(res);
            
            return true;
        }
    }
    
    
    public void analyzeAttackResponse(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        // Check if size is the same
        if (areResponseSizeIdentical(attackWorkEntry, lastHttpMessage)) {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.VULNUNSURE,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Same size as original request",
                "Strong indicator for SQL injection. The request with idempotent modification generated the same output.");
            lastHttpMessage.addAttackResult(res);
            
            return;
        }
        
        // Check if tags are the same
        if (areResponseTagsIdentical(attackWorkEntry, lastHttpMessage)) {
            AttackResult res = new AttackResult(
                AttackData.AttackResultType.VULNUNSURE,
                "SQLE",
                lastHttpMessage.getReq().getChangeParam(),
                true,
                "Same number of tags as original request",
                "Strong indicator for SQL injection. The request with idempotent modification generated a similar output.");
            lastHttpMessage.addAttackResult(res);
            
            return;
        }
        
        // Check empty response case
        // But it at the bottom, so the other checks are performed first
        if (emptyResponseOnBreak) {
            if (lastHttpMessage.getRes().getSize() > 0) {
                AttackResult res = new AttackResult(
                    AttackData.AttackResultType.INFOUNSURE,
                    "SQLE",
                    lastHttpMessage.getReq().getChangeParam(),
                    true,
                    "Non-empty response",
                    "Strong indicator for SQL injection. The request with idempotent modification generated a non-empty response.");
                lastHttpMessage.addAttackResult(res);
            }
            
            return;
        }
        
        // Check if there are any semantic differences in output
        
        // check if it already yielded a SQL error
        // This should not be, as the requests should be OK. Check anyway.
        if (responseContainSqlErrorString(lastHttpMessage)) {
            return;
        }
    }
    
    
    private boolean areResponsesIdentical(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        int responseOnErrorSizeChange = 0;
        int origResponseSize;
        int newResponseSize;
    
        // Check if response sizes are identical
        origResponseSize = attackWorkEntry.origHttpMessage.getRes().getSize();
        newResponseSize = lastHttpMessage.getRes().getSize();
        responseOnErrorSizeChange = origResponseSize - newResponseSize;

        if (responseOnErrorSizeChange == 0) {
            if (attackWorkEntry.origHttpMessage.getRes().getResponseStrBody().equals(lastHttpMessage.getRes().getResponseStrBody())) {
                return true;
            }
        }
        
        return false;
    }
    
    
    private boolean areResponseSizeIdentical(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        boolean foundSomething;
        
        int origResponseSize = attackWorkEntry.origHttpMessage.getRes().getSize();
        int newResponseSize = lastHttpMessage.getRes().getSize();
        
        if (newResponseSize == origResponseSize) {
            foundSomething = true;
        } else {
            foundSomething = false;
        }
        
        return foundSomething;
    }
    
    
    private boolean areResponseTagsIdentical(AttackWorkEntry attackWorkEntry, SentinelHttpMessageAtk lastHttpMessage) {
        int origTagCount = attackWorkEntry.origHttpMessage.getRes().getDomCount();
        int newTagCount = lastHttpMessage.getRes().getDomCount();
        
        if (origTagCount == newTagCount) {
            return true;
        } else {
            return false;
        }
    }
    
    
    private boolean responseContainSqlErrorString(SentinelHttpMessageAtk httpMessage) {
        boolean foundSomething;
        boolean hasError = false;
        ResponseCategory sqlResponseCategory = null;
        
        for(ResponseCategory rc: httpMessage.getRes().getCategories()) {
            if (rc.getCategoryEntry().getTag().equals("sqlerr")) {
                hasError = true;
                sqlResponseCategory = rc;
                break;
            }
        }
        
        if (hasError) {
            AttackResult res = new AttackResult(
                    AttackData.AttackResultType.VULNSURE, 
                    "SQLE", 
                    httpMessage.getReq().getChangeParam(), 
                    true,
                    "SQL error: " + sqlResponseCategory.getIndicator(),
                    "Strong indicator for an SQL injection. Found an SQL error string in response.");
            httpMessage.addAttackResult(res);

            ResponseHighlight h = new ResponseHighlight(sqlResponseCategory.getIndicator(), failColor);
            httpMessage.getRes().addHighlight(h);
            
            foundSomething = true;
        } else {
            /*AttackResult res = new AttackResult(
                    AttackData.AttackResultType.NONE, 
                    "SQLE", 
                    httpMessage.getReq().getChangeParam(), 
                    false,
                    null);
            httpMessage.addAttackResult(res);*/
            foundSomething = false;
        }
        
        return foundSomething;
    }
}
