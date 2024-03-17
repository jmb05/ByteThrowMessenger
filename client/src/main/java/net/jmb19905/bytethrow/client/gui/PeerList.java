/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.client.gui;

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.chatprofiles.GroupChatProfile;
import net.jmb19905.bytethrow.client.gui.chatprofiles.IChatProfile;
import net.jmb19905.bytethrow.client.gui.chatprofiles.PeerChatProfile;
import net.jmb19905.bytethrow.common.util.ResourceUtility;

import javax.swing.*;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PeerList extends JList<IChatProfile> {

    private final DefaultListModel<IChatProfile> listModel;

    public PeerList() {
        super(new DefaultListModel<>());
        listModel = (DefaultListModel<IChatProfile>) getModel();
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new PeerList.PeerListUI());
        setCellRenderer(new PeerList.PeerListRenderer());

        JPopupMenu menu = new JPopupMenu();
        JMenu membersMenu = new JMenu("Members");
        JMenuItem item = new JMenuItem("Leave");
        item.setForeground(Color.RED);
        item.addActionListener(l -> {
            IChatProfile value = getSelectedValue();
            if(value instanceof PeerChatProfile){
                StartClient.manager.disconnectFromPeer(((PeerChatProfile) value).getPeer());
            }else {
                StartClient.manager.leaveGroup(value.getDisplayName());
            }
        });
        menu.add(item);

        JList<IChatProfile> instance = this;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getX(), e.getY());
                if(index != -1){
                    setSelectedIndex(index);
                    if(e.getButton() == 3) {
                        if(getSelectedValue() instanceof PeerChatProfile){
                            item.setText("Disconnect");
                        }
                        menu.show(instance, e.getX(), e.getY());
                        membersMenu.removeAll();
                    }
                } else clearSelection();
            }
        });
    }

    public int locationToIndex(int x, int y) {
        for(int i=0;i<listModel.getSize();i++){
            Rectangle rectangle = getCellBounds(i, i);
            if(rectangle.contains(x, y)) return i;
        }
        return -1;
    }

    /**
     * empties the Peer list and add new names
     *
     * @param profiles the Profiles to be put into the list
     */
    public void setPeers(PeerChatProfile[] profiles) {
        listModel.elements().asIterator().forEachRemaining(p -> {
            if (p instanceof PeerChatProfile) {
                removeChat(p);
            }
        });
        for (PeerChatProfile profile : profiles) {
            addChat(profile);
        }
    }

    public void removeChat(IChatProfile profile) {
        listModel.removeElement(profile);
    }

    public void setPeerStatus(PeerChatProfile profile, boolean status) {
        profile.setConnected(status);
        repaint();
    }

    public void setGroups(GroupChatProfile[] profiles) {
        listModel.elements().asIterator().forEachRemaining(p -> {
            if (p instanceof GroupChatProfile) {
                removeChat(p);
            }
        });
        for (GroupChatProfile profile : profiles) {
            addChat(profile);
        }
    }

    public void addChat(IChatProfile profile) {
        listModel.addElement(profile);
    }

    private static class PeerListRenderer implements ListCellRenderer<IChatProfile> {

        private static final ImageIcon crossIcon = new ImageIcon(ResourceUtility.getImageResource("icons/x.png"));
        private static final ImageIcon tickIcon = new ImageIcon(ResourceUtility.getImageResource("icons/tick.png"));
        private static final ImageIcon warningIcon = new ImageIcon(ResourceUtility.getImageResource("icons/warning.png"));

        @Override
        public Component getListCellRendererComponent(JList<? extends IChatProfile> list, IChatProfile value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = new JLabel(" " + value.getDisplayName());
            label.setHorizontalTextPosition(JLabel.LEFT);

            if(value instanceof GroupChatProfile){
                label.setIcon(warningIcon);
                label.setToolTipText("Groups are not yet encrypted!");
            }else if(value instanceof PeerChatProfile){
                if(((PeerChatProfile) value).isConnected()){
                    label.setIcon(tickIcon);
                } else {
                    label.setIcon(crossIcon);
                }
            }

            label.setEnabled(list.isEnabled());
            label.setOpaque(true);
            if (isSelected) {
                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            return label;
        }
    }

    private static class PeerListUI extends BasicListUI {

        @Override
        protected void paintCell(Graphics g, int row, Rectangle rowBounds, ListCellRenderer<Object> cellRenderer, ListModel<Object> dataModel, ListSelectionModel selModel, int leadIndex) {
            Object value = dataModel.getElementAt(row);
            boolean cellHasFocus = list.hasFocus() && (row == leadIndex);
            boolean isSelected = selModel.isSelectedIndex(row);

            Component rendererComponent = cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);

            int cx = rowBounds.x;
            int cy = rowBounds.y;
            int cw = rowBounds.width;
            int ch = rowBounds.height;

            rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
        }
    }
}