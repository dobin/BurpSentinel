package burp;

import gui.CustomMenuItem;
import gui.MainUi;
import javax.swing.SwingUtilities;
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
    private CustomMenuItem customMenuItem;

    private MainUi main;
    
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
                
                // Create window
                main = new MainUi();
                
                // Add burp connections
                customMenuItem = new CustomMenuItem(main);
                callbacks.addSuiteTab(main);
                callbacks.registerMenuItem("Send to sentinel", customMenuItem);

                BurpCallbacks.getInstance().print("Sentinel v0.2");
            }
        });
    }

    // On exit, store UI settings
    @Override
    public void extensionUnloaded() {
        main.storeUiPrefs();
    }
}
