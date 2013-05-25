package gui.mainTop.networking;

import java.util.Observable;

/**
 *
 * @author dobin
 */
public class NetworkerLogger extends Observable {
    private StringBuffer log = new StringBuffer();

    void append(String start) {
        log.append(start);

        this.setChanged();
        this.notifyObservers(log.toString());
    }

    void newWork() {
        //log = new StringBuffer();
        log.append("\n\n");
    }

    String getText() {
        return log.toString();
    }
}
