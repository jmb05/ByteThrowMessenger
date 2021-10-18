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

import net.jmb19905.bytethrow.client.gui.chatprofiles.GroupChatProfile;
import net.jmb19905.bytethrow.client.gui.chatprofiles.IChatProfile;
import net.jmb19905.bytethrow.client.gui.chatprofiles.PeerChatProfile;
import net.jmb19905.bytethrow.common.chat.Message;
import net.jmb19905.bytethrow.common.util.ResourceUtility;
import net.jmb19905.util.Logger;

import javax.swing.*;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;

public class PeerList extends JList<IChatProfile<? extends Message>> {

    private final DefaultListModel<IChatProfile<? extends Message>> listModel;

    public PeerList() {
        super(new DefaultListModel<>());
        listModel = (DefaultListModel<IChatProfile<? extends Message>>) getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionModel(new PeerList.PeerListSelectionModel(this));
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new PeerList.PeerListUI());
        setCellRenderer(new PeerList.PeerListRenderer());
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

    public void removeChat(IChatProfile<? extends Message> profile) {
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

    public void addChat(IChatProfile<? extends Message> profile) {
        listModel.addElement(profile);
    }

    private static class PeerListRenderer extends JLabel implements ListCellRenderer<IChatProfile<? extends Message>> {

        private static final ImageIcon crossIcon = new ImageIcon(ResourceUtility.getImageResource("icons/x.png"));
        private static final ImageIcon tickIcon = new ImageIcon(ResourceUtility.getImageResource("icons/tick.png"));
        private static final ImageIcon warningIcon = new ImageIcon(ResourceUtility.getImageResource("icons/warning.png"));

        @Override
        public Component getListCellRendererComponent(JList<? extends IChatProfile<? extends Message>> list, IChatProfile<? extends Message> value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(" " + value.getDisplayName());
            setHorizontalTextPosition(JLabel.LEFT);

            if(value instanceof GroupChatProfile){
                setIcon(warningIcon);
                setToolTipText("Groups are not yet encrypted!");
            }else if(value instanceof PeerChatProfile){
                if(((PeerChatProfile) value).isConnected()){
                    setIcon(tickIcon);
                } else {
                    setIcon(crossIcon);
                }
            }

            setEnabled(list.isEnabled());
            setOpaque(true);
            if (isSelected) {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
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

    private static class PeerListSelectionModel extends DefaultListSelectionModel {

        private final JList<IChatProfile<? extends Message>> list;

        public PeerListSelectionModel(JList<IChatProfile<? extends Message>> list) {
            this.list = list;
        }

        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (index0 == index1) {
                if (isSelectedIndex(index0)) {
                    removeSelectionInterval(index0, index0);
                    list.getParent().requestFocus();
                    return;
                }
            }
            super.setSelectionInterval(index0, index1);
        }
    }

}
