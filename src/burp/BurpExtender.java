/*
 * Copyright (C) 2013 DobinRutishauser@broken.ch
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
                sentinelMain.init();
                //replayerMain = new ReplayerMainUi();
                
                callbacks.addSuiteTab(sentinelMain);
                //callbacks.addSuiteTab(replayerMain);
                
                // Add burp connections
                sentinelMenuItem = new CustomMenuItem(sentinelMain);
                //replayerMenuItem = new CustomMenuItem(replayerMain);
                
                callbacks.registerMenuItem("Send to sentinel", sentinelMenuItem);
                //callbacks.registerMenuItem("Send to replayer", replayerMenuItem);

                callbacks.registerProxyListener(sentinelMain.getProxyListener());
                
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
