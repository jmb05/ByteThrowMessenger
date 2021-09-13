package net.jmb19905.client.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicListUI;
import java.awt.*;

public class PeerList extends JList<String> {

    private final DefaultListModel<String> listModel;

    public PeerList(){
        super(new DefaultListModel<>());
        listModel = (DefaultListModel<String>) getModel();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionModel(new PeerListSelectionModel(this));
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new PeerListUI());
        setCellRenderer(new PeerListRenderer());
    }

    /**
     * empties the Peer list and add new names
     * @param names the names to be put into the list
     */
    public void setPeers(String[] names){
        listModel.removeAllElements();
        for(String name : names) {
            listModel.addElement(name + " ✗");
        }
    }

    public void addPeer(String peerName){
        listModel.addElement(peerName + " ✗");
    }

    public void removePeer(String peerName){
        listModel.removeElement(peerName + " ✗");
        listModel.removeElement(peerName + " ✓");
    }

    public void setPeerStatus(String name, boolean status){
        try {
            int index;
            String modifiedName;
            if (status) {
                modifiedName = name + " ✓";
                index = listModel.indexOf(name + " ✗");
            } else {
                modifiedName = name + " ✗";
                index = listModel.indexOf(name + " ✓");
            }
            listModel.set(index, modifiedName);
        }catch (IndexOutOfBoundsException ignored){}
    }

    @Override
    public String getSelectedValue() {
        return super.getSelectedValue().replace("✓", "").replace("✗", "").strip();
    }

    private static class PeerListRenderer extends JLabel implements ListCellRenderer<String>{

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);
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
