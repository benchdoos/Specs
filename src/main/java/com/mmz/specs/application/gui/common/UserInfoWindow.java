/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.application.gui.common;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mmz.specs.application.core.server.service.ClientConnection;
import com.mmz.specs.model.UsersEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UserInfoWindow extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel usernameLabel;
    private JLabel userTypeLabel;
    private JLabel connectionAddressLabel;
    private JLabel nameLabel;
    private JLabel surnameLabel;
    private JCheckBox isEditorCheckBox;
    private JCheckBox isAdminCheckBox;
    private JCheckBox isActiveCheckBox;
    private JLabel patronomycLabel;
    private JLabel userIconLabel;
    private ClientConnection clientConnection;

    public UserInfoWindow() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        initGui();

        initListeners();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onOK(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setResizable(false);
    }

    private void initListeners() {
        buttonOK.addActionListener(e -> onOK());


    }

    private void onOK() {
        // add your code here
        dispose();
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
        initGui();

    }

    private void updateTitle() {
        String title = "Информация о пользователе: ";

        if (clientConnection != null) {
            if (isUserEntityAvailable()) {
                setTitle(title + clientConnection.getUserEntity().getUsername());
            } else {
                setTitle(title + clientConnection.getSocket().getInetAddress() + ":" + clientConnection.getSocket().getPort());
            }
        } else {
            setTitle(title + "Нет данных");
        }
    }

    private void updateUserData() {
        if (isUserEntityAvailable()) {
            UsersEntity userEntity = clientConnection.getUserEntity();
            usernameLabel.setText(userEntity.getUsername());
            surnameLabel.setText(userEntity.getSurname());
            nameLabel.setText(userEntity.getName());
            patronomycLabel.setText(userEntity.getPatronymic());
            userTypeLabel.setText(userEntity.getUserType().getName());
            isEditorCheckBox.setSelected(userEntity.isEditor());
            isAdminCheckBox.setSelected(userEntity.isAdmin());
            isActiveCheckBox.setSelected(userEntity.isActive());
        }
    }

    private void initGui() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/user16.png")));

        updateUserIcon();
        updateTitle();
        updateClientConnectionAddressLabel();
        updateUserData();
    }

    private void updateUserIcon() {
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/user128.png")));
        if (isUserEntityAvailable()) {
            UsersEntity entity = clientConnection.getUserEntity();
            updateIcon(icon);

            if (entity.isAdmin()) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/admin128.png")));
                updateIcon(icon);
            }

            if (!entity.isActive()) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/notActive_128.png")));
                updateIcon(icon);

            }
        } else {
            icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/gui/user/notActive_128.png")));
            userIconLabel.setIcon(icon);
        }
    }

    private boolean isUserEntityAvailable() {
        return clientConnection != null && clientConnection.getUserEntity() != null;
    }

    private void updateClientConnectionAddressLabel() {
        if (clientConnection != null) {
            connectionAddressLabel.setText(clientConnection.getSocket().getInetAddress() + ":" + clientConnection.getSocket().getPort());
        }
    }

    private void updateIcon(ImageIcon icon) {
        userIconLabel.setIcon(icon);
        setIconImage(icon.getImage());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        buttonOK.setMnemonic('O');
        buttonOK.setDisplayedMnemonicIndex(0);
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(13, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Имя пользователя:");
        panel3.add(label1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel3.add(spacer3, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        usernameLabel = new JLabel();
        usernameLabel.setText("Не известно");
        panel3.add(usernameLabel, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userTypeLabel = new JLabel();
        userTypeLabel.setText("Не известно");
        panel3.add(userTypeLabel, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Тип пользователя");
        panel3.add(label2, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Имя:");
        panel3.add(label3, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Не известно");
        panel3.add(nameLabel, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Отчество:");
        panel3.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        patronomycLabel = new JLabel();
        patronomycLabel.setText("Не известно");
        panel3.add(patronomycLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Адрес подключения:");
        panel3.add(label5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        connectionAddressLabel = new JLabel();
        connectionAddressLabel.setText("Не известно");
        panel3.add(connectionAddressLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Фамилия:");
        panel3.add(label6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        surnameLabel = new JLabel();
        surnameLabel.setText("Не известно");
        panel3.add(surnameLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        panel3.add(separator1, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        isEditorCheckBox = new JCheckBox();
        isEditorCheckBox.setBorderPainted(false);
        isEditorCheckBox.setBorderPaintedFlat(false);
        isEditorCheckBox.setEnabled(false);
        isEditorCheckBox.setFocusCycleRoot(false);
        isEditorCheckBox.setHorizontalAlignment(10);
        isEditorCheckBox.setHorizontalTextPosition(2);
        isEditorCheckBox.setRolloverEnabled(true);
        isEditorCheckBox.setText("Редактор");
        isEditorCheckBox.setMnemonic('Р');
        isEditorCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(isEditorCheckBox, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isAdminCheckBox = new JCheckBox();
        isAdminCheckBox.setEnabled(false);
        isAdminCheckBox.setHorizontalTextPosition(2);
        isAdminCheckBox.setOpaque(true);
        isAdminCheckBox.setText("Администратор");
        isAdminCheckBox.setMnemonic('А');
        isAdminCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(isAdminCheckBox, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        isActiveCheckBox = new JCheckBox();
        isActiveCheckBox.setEnabled(false);
        isActiveCheckBox.setHorizontalTextPosition(2);
        isActiveCheckBox.setText("Действующий");
        isActiveCheckBox.setMnemonic('Д');
        isActiveCheckBox.setDisplayedMnemonicIndex(0);
        panel3.add(isActiveCheckBox, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userIconLabel = new JLabel();
        userIconLabel.setHorizontalAlignment(0);
        userIconLabel.setIcon(new ImageIcon(getClass().getResource("/img/gui/user/user128.png")));
        userIconLabel.setText("");
        panel3.add(userIconLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel3.add(separator2, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
