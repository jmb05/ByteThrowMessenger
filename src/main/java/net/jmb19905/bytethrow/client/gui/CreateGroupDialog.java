/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.client.gui;

import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.gui.components.HintTextField;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.util.AsynchronousInitializer;
import net.jmb19905.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CreateGroupDialog extends JDialog {

    private HintTextField groupNameTextField;
    private DefaultListModel<String> memberListModel;

    private ActionListener confirmListener = null;
    private WindowAdapter cancelListener = null;

    private String groupName = "";
    private String[] members = new String[10];

    public CreateGroupDialog(Window window) {
        super(window);
        initUI();
    }

    private void initUI() {
        setModal(true);
        setResizable(true);
        setLayout(new GridBagLayout());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(Localisation.get("create_group"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelActionPerformed(e);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.WEST;

        this.groupNameTextField = new HintTextField(Localisation.get("group_name"));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.insets = new Insets(5, 15, 5, 15);
        add(groupNameTextField, constraints);

        JList<String> memberList = new JList<>();
        this.memberListModel = new DefaultListModel<>();
        memberList.setModel(memberListModel);
        memberList.setCellRenderer(new MemberListRenderer());

        JScrollPane scrollPane = new JScrollPane(memberList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(220, 100));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;
        add(scrollPane, constraints);

        this.memberListModel.addElement(StartClient.manager.name);

        JButton addButton = new JButton(Localisation.get("add_member"));
        addButton.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(Localisation.get("peer_name_input"));
            memberListModel.addElement(name);
        });
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        add(addButton, constraints);

        JButton confirmButton = new JButton(Localisation.get("confirm"));
        confirmButton.addActionListener(this::confirmActionPerformed);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        add(confirmButton, constraints);

        pack();
        setLocationRelativeTo(null);
    }

    protected void cancelActionPerformed(WindowEvent e) {
        groupName = "";
        members = new String[10];
        hideDialog();
        if (cancelListener != null) {
            cancelListener.windowClosing(e);
        }
        clearData();
    }

    protected void confirmActionPerformed(ActionEvent e) {
        groupName = groupNameTextField.getText().strip().replaceAll(" ", "_");
        Object[] objArray = memberListModel.toArray();
        String[] membersArray = new String[objArray.length];
        for (int i = 0; i < objArray.length; i++) {
            membersArray[i] = String.valueOf(objArray[i]);
        }
        members = membersArray;
        hideDialog();
        if (confirmListener != null) {
            confirmListener.actionPerformed(e);
        }
        clearData();
    }

    private void clearData() {
        groupNameTextField.setText("");
        memberListModel.clear();
    }

    private void addConfirmButtonActionListener(ActionListener listener) {
        this.confirmListener = listener;
    }

    public void addCancelListener(WindowAdapter windowAdapter) {
        this.cancelListener = windowAdapter;
    }

    public CreateGroupData showDialog() {
        AsynchronousInitializer<CreateGroupData> initializer = new AsynchronousInitializer<>();
        Logger.debug("Initialized AsynchronousInitializer");
        SwingUtilities.invokeLater(() -> {
            Logger.debug("Invoked by SwingUtilities");
            addConfirmButtonActionListener(e -> initializer.init(new CreateGroupData(groupName, members, false)));
            addCancelListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    initializer.init(new CreateGroupData("", new String[0], true));
                }
            });
            setVisible(true);
        });
        return initializer.get();
    }

    public void hideDialog() {
        setVisible(false);
        groupNameTextField.requestFocus();
    }

    public record CreateGroupData(String groupName, String[] members, boolean cancel) {
    }

    private static class MemberListRenderer extends JLabel implements ListCellRenderer<String> {
        @SuppressWarnings("unchecked")
        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            DefaultListModel<? extends String> model = (DefaultListModel<? extends String>) list.getModel();

            setText(value);
            setEnabled(list.isEnabled());
            setOpaque(true);

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem removeItem = new JMenuItem(Localisation.get("remove"));
            removeItem.addActionListener(l -> list.getSelectedValuesList().forEach(model::removeElement));
            popupMenu.add(removeItem);

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });

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

}
