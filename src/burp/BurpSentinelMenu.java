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
package burp;

import gui.SentinelMainApi;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;



/**
 *
 * @author dobin
 */
public class BurpSentinelMenu implements IContextMenuFactory {
    	private SentinelMainApi sentinelApi;

	public BurpSentinelMenu(SentinelMainApi sentinelApi) {
		this.sentinelApi = sentinelApi;
	}
        
        @Override
	public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
            JMenuItem sendToSentinelMenu = new JMenuItem("Send to Sentinel");

            sendToSentinelMenu.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent arg0) {
                    }

                    @Override
                    public void mouseEntered(MouseEvent arg0) {
                    }

                    @Override
                    public void mouseExited(MouseEvent arg0) {
                    }

                    @Override
                    public void mousePressed(MouseEvent arg0) {
                        IHttpRequestResponse[] selectedMessages = invocation.getSelectedMessages();
                        for (IHttpRequestResponse iReqResp : selectedMessages) {
                            sentinelApi.addNewMessage(iReqResp);
                        }
                    }

                    @Override
                    public void mouseReleased(MouseEvent arg0) {
                    }
            });

            List<JMenuItem> menus = new ArrayList();
            menus.add(sendToSentinelMenu);

            return menus;
	}
}
