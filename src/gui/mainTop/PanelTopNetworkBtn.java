/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.mainTop;

import gui.networking.Networker;
import gui.networking.NetworkerLogger;
import gui.networking.NetworkerLogger.Signal;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 *
 * @author dobin
 */
public class PanelTopNetworkBtn extends JToggleButton implements Observer {
    public PanelTopNetworkBtn() {
        super();
    }
    
    public void init() {
        Networker.getInstance().getLogger().addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Signal) {
            Signal signal = (Signal) arg;
            
            switch(signal) {
                case RECV:
                    this.setText("Recv..");
                    break;
                case SEND:
                    this.setText("Send..");
                    break;
                case FINISHED:
                    this.setText("Network");
                    break;
                case CANCEL:
                    this.setText("Cancel..");
                    break;
                    
                default:
                    this.setText("Network");
                    break;                    
            }
        }
    }
    
}
