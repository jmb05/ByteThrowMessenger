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

import net.jmb19905.bytethrow.common.util.ResourceUtility;

import javax.swing.*;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;

public class PeerList extends JList<String> {

    private final DefaultListModel<String> listModel;

    public PeerList(){
        super(new DefaultListModel<>());
        listModel = (DefaultListModel<String>) getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionModel(new PeerList.PeerListSelectionModel(this));
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new PeerList.PeerListUI());
        setCellRenderer(new PeerList.PeerListRenderer());
    }

    /**
     * empties the Peer list and add new names
     * @param names the names to be put into the list
     */
    public void setPeers(String[] names){
        listModel.removeAllElements();
        for(String name : names) {
            listModel.addElement(name + " x");
        }
    }

    public void addPeer(String peerName){
        listModel.addElement(peerName + " x");
    }

    public void removePeer(String peerName){
        listModel.removeElement(peerName + " x");
        listModel.removeElement(peerName + " v");
    }

    public void setPeerStatus(String name, boolean status){
        try {
            int index;
            String modifiedName;
            if (status) {
                modifiedName = name + " v";
                index = listModel.indexOf(name + " x");
            } else {
                modifiedName = name + " x";
                index = listModel.indexOf(name + " v");
            }
            listModel.set(index, modifiedName);
        }catch (IndexOutOfBoundsException ignored){}
    }

    @Override
    public String getSelectedValue() {
        String selectedValue = super.getSelectedValue();
        if(selectedValue == null){
            return null;
        }
        return selectedValue.replace(" v", "").replace(" x", "").strip();
    }

    private static class PeerListRenderer extends JLabel implements ListCellRenderer<String>{

        private static final ImageIcon crossIcon = new ImageIcon(ResourceUtility.getImageResource("icons/x.png"));
        private static final ImageIcon tickIcon = new ImageIcon(ResourceUtility.getImageResource("icons/tick.png"));

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            String[] parts = value.split(" ");
            setText(" " + parts[0]);
            setHorizontalTextPosition(JLabel.LEFT);

            if(parts[1].equals("v")){
                setIcon(tickIcon);
            }else {
                setIcon(crossIcon);
            }

            setEnabled(list.isEnabled());
            setOpaque(true);
            if(isSelected){
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }else {
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

    private static class PeerListSelectionModel extends DefaultListSelectionModel{

        private final JList<String> list;

        public PeerListSelectionModel(JList<String> list){
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
