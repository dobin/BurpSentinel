package attacks;

import model.SentinelHttpParam;

/**
 *
 * @author unreal
 */
public class AttackResult {
    private String attackName = "";
    private AttackData.AttackType attackType = null;
    private SentinelHttpParam attackParam = null;
    private boolean success;

    AttackResult(AttackData.AttackType attackType, String attackName, SentinelHttpParam attackParam, boolean success) {
        this.attackName = attackName;
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
   
    public String getAttackName() {
        return attackName;
    }

    public AttackData.AttackType getAttackType() {
        return attackType;
    }
    
    public boolean getSuccess() {
        return success;
    }
    
}
