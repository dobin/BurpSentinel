package attacks;

/**
 *
 * @author unreal
 */
public class AttackData {
    public enum AttackType {
        INFO,
        VULN,
    };

    private String input;
    private String output;
    private Boolean success = false;
    private int index = -1;
    private AttackType attackType;
    
    public AttackData(int index, String input, String output, AttackType attackType) {
        this.index = index;
        this.input = input;
        this.output = output;
        this.attackType = attackType;
    }
    
    public AttackType getAttackType() {
        return attackType;
    }
    
    public int getIndex() {
        return index;
    }
    
    public String getInput() {
        return input;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
}
