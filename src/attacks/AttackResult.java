/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package attacks;

import model.SentinelHttpParam;

/**
 *
 * @author unreal
 */
public class AttackResult {
    private String resultStr = "";
    private AttackData.AttackType attackType = null;
    private SentinelHttpParam attackParam = null;
    private boolean success;
    
    

    AttackResult(AttackData.AttackType attackType, String resultStr, SentinelHttpParam attackParam, boolean success) {
        this.resultStr = resultStr;
        this.attackType = attackType;
        this.attackParam = attackParam;
        this.success = success;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public SentinelHttpParam getAttackParam() {
        return attackParam;
    }
   
    public String getResultStr() {
        return resultStr;
    }

    public AttackData.AttackType getAttackType() {
        return attackType;
    }
}
