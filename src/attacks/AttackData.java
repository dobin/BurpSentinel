package attacks;

/**
 *
 * @author unreal
 */
public class AttackData {

    private String input;
    private String output;
    private Boolean success = false;
    private int index = -1;
    
    public AttackData(int index, String input, String output) {
        this.index = index;
        this.input = input;
        this.output = output;
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
