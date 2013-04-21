package burp;

import gui.CustomMenuItem;
import gui.SentinelMainUi;
import javax.swing.SwingUtilities;
import replayer.gui.ReplayerMain.ReplayerMainUi;
import util.BurpCallbacks;

/*
 * The main plugin class
 * 
 * - Burp will look for this class in packasge burp
 * - Initializes UI
 * - Initializes Burp connection
 */
public class BurpExtender implements IExtensionStateListener {

    public IBurpExtenderCallbacks mCallbacks;
    private CustomMenuItem sentinelMenuItem;
    private CustomMenuItem replayerMenuItem;

    private SentinelMainUi sentinelMain;
    private ReplayerMainUi replayerMain;
    
    public BurpExtender() {
        // Nothing - everything gets done on registerExtenderCallbacks()
    }

    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        mCallbacks = callbacks;

        if (mCallbacks == null) {
            System.out.println("ARRR");
            return;
        }
        callbacks.registerExtensionStateListener(this);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Init Burp Helper functions
                BurpCallbacks.getInstance().init(mCallbacks);
                
                sentinelMain = new SentinelMainUi();
                replayerMain = new ReplayerMainUi();
                
                callbacks.addSuiteTab(sentinelMain);
                callbacks.addSuiteTab(replayerMain);
                
                // Add burp connections
                sentinelMenuItem = new CustomMenuItem(sentinelMain);
                replayerMenuItem = new CustomMenuItem(replayerMain);
                
                callbacks.registerMenuItem("Send to sentinel", sentinelMenuItem);
                callbacks.registerMenuItem("Send to replayer", replayerMenuItem);

                BurpCallbacks.getInstance().print("Sentinel v0.2");
            }
        });
    }

    // On exit, store UI settings
    @Override
    public void extensionUnloaded() {
        sentinelMain.storeUiPrefs();
    }
}
